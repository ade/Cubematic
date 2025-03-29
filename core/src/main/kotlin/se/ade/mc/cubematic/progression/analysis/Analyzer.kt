package se.ade.mc.cubematic.progression.analysis

import org.bukkit.Material

class DependencyAnalyzer(
	private val graph: DependencyGraph,
	private val initialItems: Map<Material, Int>
) {
	private val derivable = mutableSetOf<Material>()
	private val inProgress = mutableSetOf<Material>()
	private val craftingPaths = mutableMapOf<Material, CraftingPath>()

	data class CraftingPath(
		val material: Material,
		val ingredients: List<Material> = emptyList(),
		val tools: List<Material> = emptyList(),
		val isInitial: Boolean = false
	)

	init {
		derivable.addAll(initialItems.keys)
		// Mark initial items in crafting paths
		initialItems.keys.forEach { material ->
			craftingPaths[material] = CraftingPath(material, isInitial = true)
		}
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

	private fun canSatisfySource(source: Source): Boolean {
		return source.transforms.any { transform ->
			transform.input.all { req -> canSatisfyRequirement(req) } &&
					transform.tools.all { tool -> canSatisfyRequirement(tool) }
		}
	}

	// Modified to track the path when a material can be derived
	fun canDerive(material: Material): Boolean {
		if (derivable.contains(material)) return true
		if (inProgress.contains(material)) return false

		val node = graph.nodes.find { it is Node.Item && it.material == material } as? Node.Item
			?: return false

		inProgress.add(material)

		for (source in node.sources) {
			for (transform in source.transforms) {
				val inputsMet = transform.input.all { canSatisfyRequirement(it) }
				val toolsMet = transform.tools.all { canSatisfyRequirement(it) }

				if (inputsMet && toolsMet) {
					val inputMaterials = transform.input.mapNotNull {
						when (it) {
							is ProcessRequirement.Type -> it.material
							is ProcessRequirement.Any -> it.group.anyOf.find { m ->
								initialItems.containsKey(m) || craftingPaths.containsKey(m)
							}
						}
					}

					val toolMaterials = transform.tools.mapNotNull {
						when (it) {
							is ProcessRequirement.Type -> it.material
							is ProcessRequirement.Any -> it.group.anyOf.find { m ->
								initialItems.containsKey(m) || craftingPaths.containsKey(m)
							}
						}
					}

					craftingPaths[material] = CraftingPath(
						material = material,
						ingredients = inputMaterials,
						tools = toolMaterials
					)

					inProgress.remove(material)
					return true
				}
			}
		}

		inProgress.remove(material)
		return false
	}

	// New method to print paths
	fun printPathTo(material: Material, indent: String = "") {
		val path = craftingPaths[material] ?: run {
			println("$indent$material - Not derivable")
			return
		}

		if (path.isInitial) {
			println("$indent$material - Initial item")
			return
		}

		println("$indent$material can be created with:")

		println("$indent  Ingredients:")
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

