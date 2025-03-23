package se.ade.mc.skyblock.generator

import org.bukkit.generator.ChunkGenerator
import se.ade.mc.skyblock.CubeInTheSkyPlugin
import se.ade.mc.skyblock.generator.nether.NetherGenerator
import se.ade.mc.skyblock.generator.overworld.OverworldGenerator

object GeneratorSelector {
	fun selectGenerator(plugin: CubeInTheSkyPlugin, worldName: String, id: String?): ChunkGenerator {
		plugin.logger.info { "$worldName id $id" }
		return when {
			worldName == "world_nether" || id == "nether" -> NetherGenerator(plugin)
			else -> OverworldGenerator(plugin)
		}
	}
}