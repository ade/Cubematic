package se.ade.mc.cubematic.progression.analysis

import org.bukkit.Material
import se.ade.mc.cubematic.progression.analysis.Node.Item
import se.ade.mc.cubematic.progression.analysis.key.NodeKey

class DependencyAnalyzer(
	private val graph: DependencyGraph,
	private val initialItems: Set<NodeKey>
) {
	private val unlocked = mutableSetOf<NodeKey>()
	private val inProgress = mutableSetOf<NodeKey>()
	private val craftingPaths = mutableMapOf<NodeKey, CraftingPath>()

	data class CraftingPath(
		val node: NodeKey,
		val ingredients: List<NodeKey> = emptyList(),
		val tools: List<NodeKey> = emptyList(),
		val isInitial: Boolean = false
	)

	init {
		unlocked.addAll(initialItems)
		// Mark initial items in crafting paths
		initialItems.forEach { nodeId ->
			craftingPaths[nodeId] = CraftingPath(nodeId, isInitial = true)
		}
	}

	fun analyze(): Set<NodeKey> {
		// Keep finding new items until we can't derive any more
		var changed = true
		while (changed) {
			changed = expandDerivableItems()
		}
		return unlocked.toSet()
	}

	private fun expandDerivableItems(): Boolean {
		var newItemFound = false

		for (node in graph.nodes) {
			if (unlocked.contains(node.id))
				continue

			if (canDerive(node.id)) {
				unlocked.add(node.id)
				newItemFound = true
			}
		}

		return newItemFound
	}

	private fun canSatisfyRequirement(req: ProcessRequirement): Boolean {
		return when (req) {
			is ProcessRequirement.Type -> {
				// Either we already have it or we can derive it recursively
				initialItems.contains(req.key) || canDerive(req.key)
			}
			is ProcessRequirement.Any -> {
				// Any of the materials in the group will do
				req.filter.anyOf.any { nodeKey ->
					initialItems.contains(nodeKey) || canDerive(nodeKey)
				}
			}
			is ProcessRequirement.Mechanic -> {
				initialItems.contains(req.mechanic.key) || canDerive(req.mechanic.key)
			}
		}
	}

	fun canDerive(nodeKey: NodeKey): Boolean {
		if (unlocked.contains(nodeKey)) return true
		if (inProgress.contains(nodeKey)) return false

		val node = graph.nodes.find { it.id == nodeKey }
			?: return false

		inProgress.add(node.id)

		for (source in node.sources) {
			for (transform in source.transforms) {
				val inputsMet = transform.input.all { canSatisfyRequirement(it) }
				val toolsMet = transform.tools.all { canSatisfyRequirement(it) }

				if (inputsMet && toolsMet) {
					val input: List<NodeKey> = transform.input.mapNotNull {
						when (it) {
							is ProcessRequirement.Type -> it.key
							is ProcessRequirement.Any -> it.filter.anyOf.find { nodeKey ->
								initialItems.contains(nodeKey) || craftingPaths.containsKey(nodeKey)
							}
							is ProcessRequirement.Mechanic -> {
								it.mechanic.key
							}
						}
					}

					val tools = transform.tools.mapNotNull {
						when (it) {
							is ProcessRequirement.Type -> it.key
							is ProcessRequirement.Any -> it.filter.anyOf.find { m ->
								initialItems.contains(m) || craftingPaths.containsKey(m)
							}
							is ProcessRequirement.Mechanic -> {
								it.mechanic.key
							}
						}
					}

					craftingPaths[node.id] = CraftingPath(
						node = node.id,
						ingredients = input,
						tools = tools
					)

					inProgress.remove(node.id)
					return true
				}
			}
		}

		inProgress.remove(node.id)
		return false
	}

	// New method to print paths
	fun printPathTo(material: NodeKey, indent: String = "") {
		val path = craftingPaths[material] ?: run {
			println("$indent$material - Not derivable")
			return
		}

		if (path.isInitial) {
			println("$indent$material (available)")
			return
		}

		println("$indent$material obtainable with:")

		println("$indent  From:")
		path.ingredients.forEach { ingredient ->
			printPathTo(ingredient, "$indent    ")
		}

		if (path.tools.isNotEmpty()) {
			println("$indent  Tools:")
			path.tools.forEach { tool ->
				printPathTo(tool, "$indent    ")
			}
		}
	}
}

