package se.ade.mc.cubematic.progression.analysis

import org.bukkit.Material
import org.bukkit.plugin.java.JavaPlugin
import se.ade.mc.cubematic.progression.analysis.key.NodeKey

fun main() {
	val initialItems = setOf(
		NodeKey.Item(Material.COBBLESTONE),
		NodeKey.Item(Material.OAK_SAPLING),
		NodeKey.Item(Material.DIRT),
		NodeKey.Item(Material.GOLD_INGOT),
		NodeKey.Item(Material.IRON_INGOT)
	)

	val analyzer = DependencyAnalyzer(graph, initialItems)
	val derivableItems = analyzer.analyze()

	println("Derivable items:")
	derivableItems.forEach { material ->
		println("- $material")
	}

	// Check if specific item is derivable
	val key = NodeKey.Item(Material.EMERALD)
	val canDeriveIt = analyzer.canDerive(key)
	println("\nCan derive $key: $canDeriveIt")

	println("\nPath to $key:")
	analyzer.printPathTo(key)
}

fun testGraphWithPlugin(plugin: JavaPlugin) {
	val graph = buildGraph {
		standardRules()
		importAll(plugin)
	}

	val analyzer = DependencyAnalyzer(graph, setOf(
		NodeKey.Item(Material.OAK_LOG),
		NodeKey.Item(Material.OAK_LEAVES),
		NodeKey.Item(Material.DIRT),
		NodeKey.Item(Material.LAVA_BUCKET),
		NodeKey.Item(Material.WATER_BUCKET)
	))

	analyzer.analyze().forEach { plugin.logger.info { "Unlocked $it" } }
	analyzer.printPathTo(NodeKey.Item(Material.TNT))
}