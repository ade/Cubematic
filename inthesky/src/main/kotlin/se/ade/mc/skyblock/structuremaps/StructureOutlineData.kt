package se.ade.mc.skyblock.structuremaps

import kotlinx.serialization.Serializable

@Serializable
sealed interface StructureOutlineData {
	/** Map ID as assigned to map */
	val id: Int

	val title: String

	/** Map scale (0-4) */
	val scale: Int

	/** Center X as assigned to map */
	val centerX: Int

	/** Center Z as assigned to map */
	val centerZ: Int

	@Serializable
	data class Box(
		override val id: Int,
		override val title: String,
		override val scale: Int,
		override val centerX: Int,
		override val centerZ: Int,
		val minX: Int,
		val maxX: Int,
		val minY: Int,
		val maxY: Int,
		val minZ: Int,
		val maxZ: Int,
		val parts: List<Part> = listOf(),
	): StructureOutlineData
}

@Serializable
data class Part(
	val minX: Int,
	val maxX: Int,
	val minY: Int,
	val maxY: Int,
	val minZ: Int,
	val maxZ: Int,
)