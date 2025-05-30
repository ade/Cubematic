package se.ade.mc.cubematic.progression.analysis

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.runBlocking
import org.bukkit.Material
import org.bukkit.plugin.java.JavaPlugin
import se.ade.mc.cubematic.progression.analysis.key.NodeKey
import kotlin.time.measureTimedValue

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

	val initialItems = setOf(
		MechanicType.OVERWORLD.key,
		MechanicType.VILLAGER_TRADING.key,
		NodeKey.Item(Material.OAK_LOG),
		NodeKey.Item(Material.OAK_LEAVES),
		NodeKey.Item(Material.DIRT),
		NodeKey.Item(Material.LAVA_BUCKET),
		NodeKey.Item(Material.WATER_BUCKET)
	)

	val items = runBlocking {
		val timed = measureTimedValue {
			deriveObtainableItems(graph, initialItems)
		}

		println("Analyzed graph in ${timed.duration.inWholeMilliseconds}ms")

		timed.value
	}.sortedBy {
		(it.nodeKey as? NodeKey.Item)?.material?.name ?: it.nodeKey.id
	}

	val notUnlockedMats = (Material.entries - items.mapNotNull {
		(it.nodeKey as? NodeKey.Item)?.material
	}).sortedBy {
		it.name
	}

	items.forEach {
		plugin.logger.info { "Unlocked: ${it.nodeKey}" }
		//it.trace().split("\n").forEach { println(it) }
	}
	notUnlockedMats.forEach { plugin.logger.info { "Not unlocked $it" } }
	plugin.logger.info { "${items.size} unlocked, ${notUnlockedMats.size} remain, total of ${Material.entries.size}" }
}