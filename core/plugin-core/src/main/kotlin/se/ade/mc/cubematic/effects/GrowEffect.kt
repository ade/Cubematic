package se.ade.mc.cubematic.effects

import org.bukkit.Particle
import org.bukkit.block.Block

/** Bone meal / Grow effect */
fun boneMealGrowEffect(block: Block) {
	block.world.spawnParticle(
		/* particle */ Particle.HAPPY_VILLAGER,
		/* location */ block.location.add(0.5, 1.5, 0.5),
		/* count */ 10,
		/* offsetX */ 0.5,
		/* offsetY */ 0.5,
		/* offsetZ */ 0.5,
		/* extra (speed) */ 0.0,
		/* data */ null
	)
}