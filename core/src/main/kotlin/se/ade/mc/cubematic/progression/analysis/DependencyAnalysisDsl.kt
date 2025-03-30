package se.ade.mc.cubematic.progression.analysis

import org.bukkit.Material
import org.bukkit.block.Biome
import se.ade.mc.cubematic.progression.analysis.key.NodeKey

@DslMarker
annotation class DependencyGraphDsl

@DependencyGraphDsl
interface DependencyGraphBuilderScope {
	fun item(id: Material, block: NodeItemBuilder.() -> Unit)
	fun mechanic(id: MechanicType, block: NodeMechanicBuilder.() -> Unit)
}

@DependencyGraphDsl
interface NodeBuilderScope {
	val id: NodeKey
	fun from(block: SourcesBuilder.() -> Unit)
}

@DependencyGraphDsl
interface SourcesBuilderScope {
	fun crafting(vararg requirement: ProcessRequirement)
	fun craftByHand(vararg requirement: ProcessRequirement)
	fun spawnsIn(biome: Biome)
	fun grow(item: Material, on: MaterialFilter, yield: ProcessYield)
	fun smelting(item: Material, with: MaterialFilter, yield: ProcessYield)
	fun villagerTrading(item: MaterialFilter = anyOf(Material.EMERALD))
	fun having(vararg requirement: ProcessRequirement)
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

	override fun from(block: SourcesBuilder.() -> Unit) {
		val source = SourcesBuilder()
		source.block()
		sources.add(source.build())
	}

	fun build(): Node {
		return Node.Item(id, material, sources)
	}
}

class NodeMechanicBuilder(override val id: NodeKey): NodeBuilderScope {
	val sources = mutableListOf<Source>()

	override fun from(block: SourcesBuilder.() -> Unit) {
		val source = SourcesBuilder()
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

class SourcesBuilder: SourcesBuilderScope {
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

	override fun spawnsIn(biome: Biome) {
		spawnsInBiomes.add(biome)
	}

	override fun grow(plant: Material, on: MaterialFilter, yield: ProcessYield) {
		transformable.add(
			Transformable(
				input = listOf(ProcessRequirement.Type(NodeKey.Item(plant), 1)),
				tools = listOf(ProcessRequirement.Any(on, 1)),
				yield = yield
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

	fun build(): Source {
		return Source(transformable, spawnsInBiomes)
	}
}

sealed interface ProcessRequirement {
	data class Any(val filter: MaterialFilter, val quantity: Int): ProcessRequirement
	data class Type(val key: NodeKey, val quantity: Int): ProcessRequirement
	data class Mechanic(val mechanic: MechanicType): ProcessRequirement
}

infix fun Int.of(material: Material): ProcessRequirement {
	return ProcessRequirement.Type(NodeKey.Item(material), this)
}

infix fun Int.of(group: MaterialFilter): ProcessRequirement {
	return ProcessRequirement.Any(group, this)
}

class MaterialFilter(val anyOf: Set<NodeKey> = setOf())

fun furnace() = MaterialFilter(anyOf = setOf(NodeKey.Item(Material.FURNACE)))

sealed interface ProcessYield {
	data class Fixed(val amount: Int): ProcessYield
	data class Random(val min: Int, val max: Int, val avg: Int = (max + min) / 2): ProcessYield
}

fun exactly(fixed: Int) = ProcessYield.Fixed(fixed)

fun anyOf(vararg materials: Material)
	= MaterialFilter(anyOf = materials.map { NodeKey.Item(it) }.toSet())

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
