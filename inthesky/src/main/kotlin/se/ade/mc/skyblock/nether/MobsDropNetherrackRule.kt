package se.ade.mc.skyblock.nether

import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.EntityType
import org.bukkit.entity.MagmaCube
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.inventory.ItemStack
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator
import kotlin.random.Random

// Drop tables for different mobs - material to probability mapping
private val mobDropTables = mapOf(
	EntityType.ZOMBIFIED_PIGLIN to mapOf(
		Material.NETHERRACK to 0.5,
	),
)

@EventHandler
fun mobsDropNetherrackRule(event: EntityDeathEvent) {
	val entity = event.entity

	// Only process nether mobs
	if (entity.location.world.environment != World.Environment.NETHER) {
		return
	}

	// Check if this mob type has defined drops
	val dropTable = mobDropTables[entity.type] ?: return

	// Process size multiplier for magma cubes
	val sizeMultiplier = if (entity is MagmaCube) {
		when (entity.size) {
			1 -> 1
			2 -> 2
			else -> 3
		}
	} else 1

	// Roll for normal drops
	for ((material, chance) in dropTable) {
		if (Random.nextDouble() < chance) {
			val amount = Random.nextInt(1, 1 + sizeMultiplier)
			event.drops.add(ItemStack(material, amount))
		}
	}
}