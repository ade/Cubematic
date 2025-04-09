package se.ade.mc.cubematic.progression.analysis

import org.bukkit.Material
import org.bukkit.block.Biome
import org.bukkit.entity.EntityType
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.plugin.java.JavaPlugin
import se.ade.mc.cubematic.progression.analysis.key.ItemTag
import se.ade.mc.cubematic.progression.analysis.key.NodeKey

@DslMarker
annotation class DependencyGraphDsl

@DependencyGraphDsl
interface DependencyGraphBuilderScope {
	fun item(id: Material, block: NodeItemBuilder.() -> Unit)
	fun item(id: Material, tags: Set<ItemTag>, block: NodeItemBuilder.() -> Unit)
	fun item(key: NodeKey.Item, block: NodeItemBuilder.() -> Unit)
	fun mechanic(id: MechanicType, block: NodeMechanicBuilder.() -> Unit)
}

@DependencyGraphDsl
interface NodeBuilderScope {
	val id: NodeKey
	fun from(description: String? = null, block: SourcesBuilder.() -> Unit)

	fun fromOverworldEntity(entity: EntityType) {
		from("Spawning overworld entity: $entity") {
			spawningEntity()
		}
	}
}

@DependencyGraphDsl
interface SourcesBuilderScope {
	fun crafting(vararg requirement: ProcessRequirement)
	fun craftByHand(vararg requirement: ProcessRequirement)
	fun spawnsIn(biome: Biome)
	fun grow(item: Material, on: MaterialFilter)
	fun smelting(item: Material, with: MaterialFilter, yield: ProcessYield)
	fun smelting(item: MaterialFilter, with: MaterialFilter)
	fun villagerTrading(item: MaterialFilter = anyOf(Material.EMERALD))
	fun having(vararg requirement: ProcessRequirement)
	fun having(vararg materials: Material)
	fun brewing(vararg materials: Material)
	fun brewing(vararg requirement: ProcessRequirement)
	fun spawningEntity()
}

enum class MechanicType(val key: NodeKey) {
	VILLAGER_TRADING(key = NodeKey.Custom("villager_trading", "Villager Trading")),
}

data class DependencyGraph(
	val nodes: List<Node>
)

sealed class Node {
	abstract val id: NodeKey
	abstract val sources: List<Source>

	data class Item(
		override val id: NodeKey,
		val material: Material,
		override val sources: List<Source>
	): Node()

	data class Mechanic(
		override val id: NodeKey,
		override val sources: List<Source>
	): Node()
}

data class Source(
	val transforms: List<Transformable>,
	val spawnsInBiomes: List<Biome>
)

data class Transformable(
	val input: List<ProcessRequirement>,
	val tools: List<ProcessRequirement>,
	val yield: ProcessYield
)

class NodeItemBuilder(override val id: NodeKey, val material: Material): NodeBuilderScope {
	val sources = mutableListOf<Source>()

	override fun from(description: String?, block: SourcesBuilder.() -> Unit) {
		val source = SourcesBuilder(description)
		source.block()
		sources.add(source.build())
	}

	fun build(): Node {
		return Node.Item(id, material, sources)
	}
}

class NodeMechanicBuilder(override val id: NodeKey): NodeBuilderScope {
	val sources = mutableListOf<Source>()

	override fun from(description: String?, block: SourcesBuilder.() -> Unit) {
		val source = SourcesBuilder(description)
		source.block()
		sources.add(source.build())
	}

	fun build(): Node {
		return Node.Mechanic(id, sources)
	}
}

class DependencyGraphBuilder: DependencyGraphBuilderScope {
	private val nodes = mutableListOf<Node>()

	override fun item(
		material: Material,
		block: NodeItemBuilder.() -> Unit
	) {
		val node = NodeItemBuilder(NodeKey.Item(material), material)
		node.block()
		nodes.add(node.build())
	}

	override fun item(
		material: Material,
		tags: Set<ItemTag>,
		block: NodeItemBuilder.() -> Unit
	) {
		val node = NodeItemBuilder(NodeKey.Item(material, tags), material)
		node.block()
		nodes.add(node.build())
	}

	override fun item(
		key: NodeKey.Item,
		block: NodeItemBuilder.() -> Unit
	) {
		val node = NodeItemBuilder(key, key.material)
		node.block()
		nodes.add(node.build())
	}

	override fun mechanic(
		mechanic: MechanicType,
		block: NodeMechanicBuilder.() -> Unit
	) {
		nodes.add(NodeMechanicBuilder(mechanic.key).also(block).build())
	}

	fun build(): DependencyGraph {
		return DependencyGraph(nodes)
	}
}

fun buildGraph(block: DependencyGraphBuilderScope.() -> Unit): DependencyGraph {
	return DependencyGraphBuilder().also { it.block() }.build()
}

class SourcesBuilder(val description: String? = null): SourcesBuilderScope {
	val spawnsInBiomes = mutableListOf<Biome>()
	val transformable = mutableListOf<Transformable>()

	override fun crafting(vararg requirement: ProcessRequirement) {
		transformable.add(
			Transformable(
				input = requirement.toList(),
				tools = listOf(ProcessRequirement.Type(
					key = NodeKey.Item(Material.CRAFTING_TABLE),
					quantity = 1
				)),
				yield = ProcessYield.Fixed(1)
			)
		)
	}

	override fun craftByHand(vararg requirement: ProcessRequirement) {
		transformable.add(
			Transformable(
				input = requirement.toList(),
				tools = emptyList(),
				yield = ProcessYield.Fixed(1)
			)
		)
	}

	override fun having(vararg requirement: ProcessRequirement) {
		transformable.add(
			Transformable(
				input = requirement.toList(),
				tools = emptyList(),
				yield = ProcessYield.Fixed(0)
			)
		)
	}

	override fun having(vararg materials: Material) {
		transformable.add(
			Transformable(
				input = materials(*materials).toList(),
				tools = emptyList(),
				yield = ProcessYield.Fixed(0)
			)
		)
	}

	override fun spawnsIn(biome: Biome) {
		spawnsInBiomes.add(biome)
	}

	override fun grow(plant: Material, on: MaterialFilter) {
		transformable.add(
			Transformable(
				input = listOf(ProcessRequirement.Type(NodeKey.Item(plant), 1)),
				tools = listOf(ProcessRequirement.Any(on, 1)),
				yield = ProcessYield.Undefined
			)
		)
	}

	override fun smelting(item: Material, with: MaterialFilter, yield: ProcessYield) {
		transformable.add(
			Transformable(
				input = listOf(ProcessRequirement.Type(NodeKey.Item(item), 1)),
				tools = listOf(ProcessRequirement.Any(with, 1)),
				yield = yield
			)
		)
	}

	override fun smelting(item: MaterialFilter, with: MaterialFilter) {
		transformable.add(
			Transformable(
				input = listOf(ProcessRequirement.Any(item, 1)),
				tools = listOf(ProcessRequirement.Any(with, 1)),
				yield = ProcessYield.Undefined
			)
		)
	}

	override fun villagerTrading(item: MaterialFilter) {
		transformable.add(
			Transformable(
				input = listOf(ProcessRequirement.Any(item, 1),
					ProcessRequirement.Mechanic(MechanicType.VILLAGER_TRADING)),
				tools = emptyList(),
				yield = ProcessYield.Fixed(1)
			)
		)
	}

	override fun brewing(vararg materials: Material) {
		transformable.add(
			Transformable(
				input = materials(*materials).toList() + (1 of Material.GLASS_BOTTLE) + (1 of Material.BLAZE_POWDER),
				tools = listOf(ProcessRequirement.Type(NodeKey.Item(Material.BREWING_STAND), 1)),
				yield = ProcessYield.Undefined
			)
		)
	}

	override fun brewing(vararg requirement: ProcessRequirement) {
		transformable.add(
			Transformable(
				input = requirement.toList() + (1 of Material.GLASS_BOTTLE) + (1 of Material.BLAZE_POWDER),
				tools = listOf(ProcessRequirement.Type(NodeKey.Item(Material.BREWING_STAND), 1)),
				yield = ProcessYield.Undefined
			)
		)
	}

	override fun spawningEntity() {
		transformable.add(
			Transformable(
				input = emptyList(),
				tools = emptyList(),
				yield = ProcessYield.Undefined
			)
		)
	}

	fun wanderingTrader(requirement: ProcessRequirement) {
		transformable.add(
			Transformable(
				input = listOf(requirement),
				tools = emptyList(),
				yield = ProcessYield.Undefined
			)
		)
	}

	fun build(): Source {
		return Source(transformable, spawnsInBiomes)
	}
}

fun materials(vararg materials: Material): Array<ProcessRequirement.Type> {
	return materials.map { ProcessRequirement.Type(NodeKey.Item(it), 1) }.toTypedArray()
}

sealed interface ProcessRequirement {
	data class Any(val filter: MaterialFilter, val quantity: Int): ProcessRequirement
	data class Type(val key: NodeKey, val quantity: Int): ProcessRequirement
	data class Mechanic(val mechanic: MechanicType): ProcessRequirement
}

infix fun Int.of(group: MaterialFilter): ProcessRequirement {
	return ProcessRequirement.Any(group, this)
}

infix fun Int.of(node: NodeKey.Item): ProcessRequirement {
	return ProcessRequirement.Type(node, this)
}

infix fun Int.of(material: Material): ProcessRequirement {
	return 1 of NodeKey.Item(material)
}

data class MaterialFilter(val anyOf: Set<NodeKey> = setOf())

fun furnace() = MaterialFilter(anyOf = setOf(NodeKey.Item(Material.FURNACE)))

sealed interface ProcessYield {
	data class Fixed(val amount: Int): ProcessYield
	data class Random(val min: Int, val max: Int, val avg: Int = (max + min) / 2): ProcessYield
	data object Undefined: ProcessYield
}

fun exactly(fixed: Int) = ProcessYield.Fixed(fixed)

fun anyOf(vararg materials: Material)
	= MaterialFilter(anyOf = materials.map { NodeKey.Item(it) }.toSet())

operator fun MaterialFilter.plus(other: Set<Material>) = MaterialFilter(
	anyOf = this.anyOf + other.map { NodeKey.Item(it) }.toSet()
)

operator fun MaterialFilter.plus(other: MaterialFilter): MaterialFilter {
	return MaterialFilter(
		anyOf = this.anyOf + other.anyOf
	)
}

fun saplingSoils() = anyOf(
	Material.DIRT,
	Material.GRASS_BLOCK,
	Material.COARSE_DIRT,
	Material.PODZOL,
	Material.MYCELIUM,
	Material.ROOTED_DIRT,
	Material.MOSS_BLOCK,
	Material.PALE_MOSS_BLOCK,
	Material.FARMLAND,
	Material.MUD,
	Material.MUDDY_MANGROVE_ROOTS
)

fun anyPlanks() = anyOf(
	Material.OAK_PLANKS,
	Material.SPRUCE_PLANKS,
	Material.BIRCH_PLANKS,
	Material.JUNGLE_PLANKS,
	Material.ACACIA_PLANKS,
	Material.DARK_OAK_PLANKS,
	Material.CRIMSON_PLANKS,
	Material.WARPED_PLANKS,
	Material.BAMBOO_PLANKS,
	Material.PALE_OAK_PLANKS,
	Material.CHERRY_PLANKS,
	Material.MANGROVE_PLANKS,
)

val overworldLogTypes = setOf<Material>(
	Material.OAK_LOG,
	Material.SPRUCE_LOG,
	Material.BIRCH_LOG,
	Material.JUNGLE_LOG,
	Material.ACACIA_LOG,
	Material.DARK_OAK_LOG,
	Material.BAMBOO_BLOCK,
	Material.PALE_OAK_LOG,
	Material.CHERRY_LOG,
	Material.MANGROVE_LOG
)

val netherLogTypes = setOf<Material>(
	Material.CRIMSON_STEM,
	Material.WARPED_STEM
)

val logTypes = overworldLogTypes + netherLogTypes