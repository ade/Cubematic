package se.ade.mc.cubematic.progression.analysis

import se.ade.mc.cubematic.progression.analysis.key.NodeKey
import kotlin.collections.remove
import kotlin.text.set

class DependencyAnalyzer(
	private val graph: DependencyGraph,
	private val initialItems: Set<NodeKey>
) {
	private val unlocked = mutableSetOf<NodeKey>()
	private val inProgress = mutableSetOf<NodeKey>()
	private val craftingPaths = mutableMapOf<NodeKey, CraftingPath>()

	// Track items we've attempted but couldn't derive in this analysis pass
	private val attemptedThisPass = mutableSetOf<NodeKey>()

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
		var itemsAddedInPass: Int
		do {
			attemptedThisPass.clear() // Reset for new pass
			itemsAddedInPass = 0

			for (node in graph.nodes) {
				if (unlocked.contains(node.id))
					continue

				if (canDerive(node.id)) {
					unlocked.add(node.id)
					itemsAddedInPass++
				}
			}
		} while (itemsAddedInPass > 0)

		return unlocked.toSet()
	}

	private fun canSatisfyRequirement(req: ProcessRequirement, currentPath: Set<NodeKey> = emptySet()): Boolean {
		return when (req) {
			is ProcessRequirement.Type -> {
				initialItems.contains(req.key) ||
						unlocked.contains(req.key) ||
						canDerive(req.key, currentPath + req.key)
			}
			is ProcessRequirement.Any -> {
				// First check directly available items
				req.filter.anyOf.any { nodeKey ->
					initialItems.contains(nodeKey) || unlocked.contains(nodeKey)
				} ||
						// Then try each alternative, but avoid those in the current path
						req.filter.anyOf.any { nodeKey ->
							!currentPath.contains(nodeKey) && canDerive(nodeKey, currentPath + nodeKey)
						}
			}
			is ProcessRequirement.Mechanic -> {
				initialItems.contains(req.mechanic.key) ||
						unlocked.contains(req.mechanic.key) ||
						canDerive(req.mechanic.key, currentPath + req.mechanic.key)
			}
		}
	}

	fun canDerive(nodeKey: NodeKey, currentPath: Set<NodeKey> = emptySet()): Boolean {
		// Quick checks
		if (unlocked.contains(nodeKey)) return true
		if (inProgress.contains(nodeKey)) return false
		if (currentPath.contains(nodeKey) && currentPath.size > 1) return false

		val node = graph.nodes.find { it.id == nodeKey } ?: return false

		inProgress.add(node.id)
		attemptedThisPass.add(node.id)

		var canBeCreated = false
		var ingredientsUsed = listOf<NodeKey>()
		var toolsUsed = listOf<NodeKey>()

		try {
			sourceLoop@ for (source in node.sources) {
				transformLoop@ for (transform in source.transforms) {
					// Check inputs
					val satisfiedInputs = mutableListOf<NodeKey>()
					val allInputsMet = transform.input.all { req ->
						val met = canSatisfyRequirement(req, currentPath)
						if (met && req is ProcessRequirement.Type) {
							satisfiedInputs.add(req.key)
						}
						met
					}

					// Check tools
					val satisfiedTools = mutableListOf<NodeKey>()
					val allToolsMet = transform.tools.all { req ->
						val met = canSatisfyRequirement(req, currentPath)
						if (met && req is ProcessRequirement.Type) {
							satisfiedTools.add(req.key)
						}
						met
					}

					if (allInputsMet && allToolsMet) {
						canBeCreated = true
						ingredientsUsed = satisfiedInputs
						toolsUsed = satisfiedTools
						break@sourceLoop
					}
				}
			}

			// Record path if successful
			if (canBeCreated) {
				craftingPaths[node.id] = CraftingPath(
					node = node.id,
					ingredients = ingredientsUsed,
					tools = toolsUsed,
					isInitial = false
				)
			}
		} finally {
			inProgress.remove(node.id)
		}

		return canBeCreated
	}

	// Debug method to find issues in the crafting tree
	fun debugDerivation(nodeKey: NodeKey, indent: String = ""): Boolean {
		println("${indent}Trying to derive: $nodeKey")
		if (unlocked.contains(nodeKey)) {
			println("${indent}✓ Already unlocked")
			return true
		}

		if (inProgress.contains(nodeKey)) {
			println("${indent}✗ Cycle detected")
			return false
		}

		val node = graph.nodes.find { it.id == nodeKey }
		if (node == null) {
			println("${indent}✗ Node not found")
			return false
		}

		inProgress.add(nodeKey)
		try {
			for (source in node.sources) {
				println("${indent}Source: ${source}")
				for (transform in source.transforms) {
					println("${indent}  Transform requirements:")

					var allMet = true
					for (req in transform.input) {
						val met = canSatisfyRequirement(req)
						println("${indent}    Input $req: ${if (met) "✓" else "✗"}")
						if (!met) allMet = false
					}

					for (req in transform.tools) {
						val met = canSatisfyRequirement(req)
						println("${indent}    Tool $req: ${if (met) "✓" else "✗"}")
						if (!met) allMet = false
					}

					if (allMet) {
						println("${indent}✓ Can create $nodeKey")
						return true
					}
				}
			}

			println("${indent}✗ Cannot create $nodeKey")
			return false
		} finally {
			inProgress.remove(nodeKey)
		}
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

