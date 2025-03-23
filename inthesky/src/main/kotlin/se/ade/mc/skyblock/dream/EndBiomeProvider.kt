package se.ade.mc.skyblock.dream

import org.bukkit.block.Biome
import org.bukkit.generator.BiomeProvider
import org.bukkit.generator.WorldInfo

internal class EndBiomeProvider : BiomeProvider() {
    override fun getBiome(worldInfo: WorldInfo, x: Int, y: Int, z: Int): Biome {
        return Biome.THE_END
    }

    override fun getBiomes(worldInfo: WorldInfo): List<Biome> {
        return listOf(Biome.THE_END)
    }
}