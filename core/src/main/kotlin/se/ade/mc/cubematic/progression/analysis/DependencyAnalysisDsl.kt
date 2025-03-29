package se.ade.mc.cubematic.progression.analysis

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Biome

@DslMarker
annotation class DependencyGraphDsl

@DependencyGraphDsl
interface DependencyGraphBuilderScope {
	fun item(id: Material, block: NodeItemBuilder.() -> Unit)
}

@DependencyGraphDsl
interface NodeBuilderScope {
	val id: NamespacedKey
	fun sources(block: SourcesBuilder.() -> Unit)
}

@DependencyGraphDsl
interface SourcesBuilderScope {
	fun crafting(vararg requirement: ProcessRequirement)
	fun craftByHand(vararg requirement: ProcessRequirement)
	fun spawnsIn(biome: Biome)
	fun grow(item: Material, on: MaterialFilter, yield: ProcessYield)
	fun smelting(item: Material, with: MaterialFilter, yield: ProcessYield)
}

data class DependencyGraph(
	val nodes: List<Node>
)

sealed class Node {
	abstract val id: NamespacedKey

	data class Item(
		override val id: NamespacedKey,
		val material: Material,
		val sources: List<Source>
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

class NodeItemBuilder(override val id: NamespacedKey, val material: Material): NodeBuilderScope {
	val sources = mutableListOf<Source>()

	override fun sources(block: SourcesBuilder.() -> Unit) {
		val source = SourcesBuilder()
		source.block()
		sources.add(source.build())
	}

	fun build(): Node {
		return Node.Item(id, material, sources)
	}
}

class DependencyGraphBuilder: DependencyGraphBuilderScope {
	private val nodes = mutableListOf<Node>()

	override fun item(
		material: Material,
		block: NodeItemBuilder.() -> Unit
	) {
		val node = NodeItemBuilder(material.key, material)
		node.block()
		nodes.add(node.build())
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
					material = Material.CRAFTING_TABLE,
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

	override fun spawnsIn(biome: Biome) {
		spawnsInBiomes.add(biome)
	}

	override fun grow(plant: Material, on: MaterialFilter, yield: ProcessYield) {
		transformable.add(
			Transformable(
				input = listOf(ProcessRequirement.Type(plant, 1)),
				tools = listOf(ProcessRequirement.Any(on, 1)),
				yield = yield
			)
		)
	}

	override fun smelting(item: Material, with: MaterialFilter, yield: ProcessYield) {
		transformable.add(
			Transformable(
				input = listOf(ProcessRequirement.Type(item, 1)),
				tools = listOf(ProcessRequirement.Any(with, 1)),
				yield = yield
			)
		)
	}

	fun build(): Source {
		return Source(transformable, spawnsInBiomes)
	}
}

sealed interface ProcessRequirement {
	data class Any(val group: MaterialFilter, val quantity: Int): ProcessRequirement
	data class Type(val material: Material, val quantity: Int): ProcessRequirement
}

infix fun Int.of(material: Material): ProcessRequirement {
	return ProcessRequirement.Type(material, this)
}

infix fun Int.of(group: MaterialFilter): ProcessRequirement {
	return ProcessRequirement.Any(group, this)
}

class MaterialFilter(val anyOf: Set<Material> = setOf())

fun furnace() = MaterialFilter(anyOf = setOf(Material.FURNACE))

sealed interface ProcessYield {
	data class Fixed(val amount: Int): ProcessYield
	data class Random(val min: Int, val max: Int, val avg: Int = (max + min) / 2): ProcessYield
}

fun exactly(fixed: Int) = ProcessYield.Fixed(fixed)

fun saplingSoils() = MaterialFilter(anyOf = setOf(
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
))

fun anyPlanks() = MaterialFilter(
	anyOf = setOf(
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
)
