package se.ade.mc.cubematic.progression.analysis

import org.bukkit.Material

class DependencyAnalyzer(
	private val graph: DependencyGraph,
	private val initialItems: Map<Material, Int>
) {
	private val derivable = mutableSetOf<Material>()
	private val inProgress = mutableSetOf<Material>() // Prevents cycles

	init {
		// Start with items we already have
		derivable.addAll(initialItems.keys)
	}

	fun analyze(): Set<Material> {
		// Keep finding new items until we can't derive any more
		var changed = true
		while (changed) {
			changed = expandDerivableItems()
		}
		return derivable.toSet()
	}

	private fun expandDerivableItems(): Boolean {
		var newItemFound = false

		for (node in graph.nodes) {
			if (node !is Node.Item || derivable.contains(node.material))
				continue

			if (canDerive(node.material)) {
				derivable.add(node.material)
				newItemFound = true
			}
		}

		return newItemFound
	}

	fun canDerive(material: Material): Boolean {
		// Already have it or previously derived it
		if (derivable.contains(material)) return true

		// Prevent infinite recursion
		if (inProgress.contains(material)) return false

		// Find node for this material
		val node = graph.nodes.find { it is Node.Item && it.material == material } as? Node.Item
			?: return false

		inProgress.add(material)

		// Check each possible way to create this item
		val canMake = node.sources.any { source ->
			canSatisfySource(source)
		}

		inProgress.remove(material)
		return canMake
	}

	private fun canSatisfySource(source: Source): Boolean {
		// Check if any transformation can be performed
		return source.transforms.any { transform ->
			// Check all inputs are available
			transform.input.all { req -> canSatisfyRequirement(req) } &&
					// Check all tools are available
					transform.tools.all { tool -> canSatisfyRequirement(tool) }
		}
	}

	private fun canSatisfyRequirement(req: ProcessRequirement): Boolean {
		return when (req) {
			is ProcessRequirement.Type -> {
				// Either we already have it or we can derive it recursively
				initialItems.containsKey(req.material) || canDerive(req.material)
			}
			is ProcessRequirement.Any -> {
				// Any of the materials in the group will do
				req.group.anyOf.any { material ->
					initialItems.containsKey(material) || canDerive(material)
				}
			}
		}
	}
}

fun main() {
	val initialItems = mapOf(
		Material.COBBLESTONE to 64,
		Material.BIRCH_LOG to 64,
	)

	val analyzer = DependencyAnalyzer(graph, initialItems)
	val derivableItems = analyzer.analyze()

	println("Derivable items:")
	derivableItems.forEach { material ->
		println("- $material")
	}

	// Check if specific item is derivable
	val canDeriveSmoothStone = analyzer.canDerive(Material.SMOOTH_STONE)
	println("\nCan derive SMOOTH_STONE: $canDeriveSmoothStone")
}