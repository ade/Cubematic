package se.ade.mc.cubematic.progression.analysis

import org.bukkit.Material

fun main() {
	val initialItems = mapOf(
		Material.COBBLESTONE to 64,
		Material.BIRCH_SAPLING to 64,
		Material.DIRT to 64
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

	println("\nPath to SMOOTH_STONE:")
	analyzer.printPathTo(Material.SMOOTH_STONE)
}