package se.ade.mc.skyblock.structuremaps

import org.bukkit.generator.structure.GeneratedStructure

data class StructureMapDrawData(
	/** Map scale (0-4) */
	val scale: Int,

	/** Center X as assigned to map */
	val centerX: Int,

	/** Center Z as assigned to map */
	val centerZ: Int,

	/** The generated structure to draw */
	val structure: GeneratedStructure,
)