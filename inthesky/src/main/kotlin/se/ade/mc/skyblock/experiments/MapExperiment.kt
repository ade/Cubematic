package se.ade.mc.skyblock.experiments

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.generator.structure.StructureType
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.map.MapCanvas
import org.bukkit.map.MapPalette
import org.bukkit.map.MapRenderer
import org.bukkit.map.MapView
import org.bukkit.map.MinecraftFont
import org.bukkit.util.BoundingBox
import org.bukkit.util.StructureSearchResult
import se.ade.mc.skyblock.CubematicSkyPlugin
import java.awt.Color
import kotlin.math.max


fun mapExperiment(player: Player, plugin: CubematicSkyPlugin) {
	val world = player.world
	val loc = player.location
	val structureType = StructureType.OCEAN_MONUMENT
	val structureKey = structureType.key

	// Locate the nearest structure
	val findOnlyUnexplored = false
	val result: StructureSearchResult = world.locateNearestStructure(loc, structureType, 10000, findOnlyUnexplored)
		?: return

	plugin.logger.info { "Locate structure: $result" }

	val resultChunkX = result.location.chunk.x
	val resultChunkZ = result.location.chunk.z

	val struct = world.getStructures(resultChunkX, resultChunkZ)
		.firstOrNull { it.structure.structureType == structureType }
		?: run {
			plugin.logger.warning { "Structure $structureKey not found at: ${result.location}" }
			return
		}

	val bbox: BoundingBox = struct.boundingBox
	val centerX = bbox.centerX
	val centerZ = bbox.centerZ

	// Determine the appropriate map scale
	val width = (bbox.maxX - bbox.minX + 1).toInt()
	val depth = (bbox.maxZ - bbox.minZ + 1).toInt()
	val requiredCoverage = max(width, depth)

	// Calculate the scale based on the required coverage.
	// Map scale is a range 0...4 where 1 pixel:
	// scale 0: 1 block, scale 1: 2 blocks
	// scale 2: 4 blocks, scale 3: 8 blocks
	// scale 4: 16 blocks (one chunk!) == 1 pixel
	val scale = (0..4).firstOrNull { 128 * (1 shl it) >= requiredCoverage } ?: 4

	// Create and configure the map
	val mapView = Bukkit.createMap(world)

	// Need to remove the default renderer to avoid the default map rendering
	val renderers = mapView.renderers.toList()
	renderers.forEach { mapView.removeRenderer(it) }

	mapView.centerX = centerX.toInt()
	mapView.centerZ = centerZ.toInt()
	mapView.scale = MapView.Scale.valueOf(scale.toByte())!!

	// Setting locked before assigning it to an item meta makes a difference it seems ...
	mapView.isLocked = true

	// Log params
	plugin.logger.info {
		"Make Map params: centerX=$centerX, centerZ=$centerZ, scale=$scale, width=$width, depth=$depth, bbox=$bbox"
	}

	// Add custom renderer to draw the outline
	mapView.addRenderer(BoxOutlineRenderer(bbox, centerX, centerZ, scale, SubjectInfo(structureKey.key)))

	// Create the map item and give it to the player
	val mapItem = ItemStack(Material.FILLED_MAP)

	mapItem.itemMeta = (mapItem.itemMeta as MapMeta).also { meta ->
		meta.mapView = mapView
	}

	player.inventory.addItem(mapItem)
}

private data class SubjectInfo(
	val structureKey: String,
)

private class BoxOutlineRenderer(
	private val bbox: BoundingBox,
	private val centerX: Double, // Use Double for precision
	private val centerZ: Double,
	private val scale: Int,
	private val subject: SubjectInfo
) : MapRenderer() {
	private var hasRendered = false

	private class Colors {
		companion object {
			val background = Color(0x486C98)
			val foreground = Color(0xB4B1AC)
		}
	}

	override fun render(map: MapView, canvas: MapCanvas, player: Player) {
		if (hasRendered) return

		// Fill the map with a color
		for (x in 0..127) {
			for (y in 0..127) {
				canvas.setPixelColor(x, y, Colors.background)
			}
		}

		// Draw all four edges
		drawBoxEdges(canvas, Colors.foreground)

		// Have not found a replacement for this yet
		@Suppress("DEPRECATION", "removal")
		val foregroundColorIndex = MapPalette.matchColor(Colors.foreground)

		canvas.drawText(0, 0, MinecraftFont.Font, "§$foregroundColorIndex;${subject.structureKey}")

		hasRendered = true
		map.isTrackingPosition = true
		map.isUnlimitedTracking = true
		map.isLocked = true
	}

	private fun drawBoxEdges(canvas: MapCanvas, color: Color) {
		// Convert all coordinates to pixels first
		val minXpx = worldToPixel(bbox.minX, centerX)
		val maxXpx = worldToPixel(bbox.maxX, centerX)
		val minZpx = worldToPixel(bbox.minZ, centerZ)
		val maxZpx = worldToPixel(bbox.maxZ, centerZ)

		// Horizontal lines (Z edges)
		(minXpx..maxXpx).forEach { x ->
			canvas.setPixelColor(x.coerceInCanvas(), minZpx.coerceInCanvas(), color)
			canvas.setPixelColor(x.coerceInCanvas(), maxZpx.coerceInCanvas(), color)
		}

		// Vertical lines (X edges)
		(minZpx..maxZpx).forEach { z ->
			canvas.setPixelColor(minXpx.coerceInCanvas(), z.coerceInCanvas(), color)
			canvas.setPixelColor(maxXpx.coerceInCanvas(), z.coerceInCanvas(), color)
		}
	}

	private fun worldToPixel(worldCoord: Double, center: Double): Int {
		val blockPerPixel = 1 shl scale
		return ((worldCoord - center) / blockPerPixel).toInt() + 64
	}

	private fun Int.coerceInCanvas() = coerceIn(0, 127)
}