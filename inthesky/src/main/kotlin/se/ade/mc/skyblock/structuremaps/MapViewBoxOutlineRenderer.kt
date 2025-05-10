package se.ade.mc.skyblock.structuremaps

import org.bukkit.entity.Player
import org.bukkit.map.MapCanvas
import org.bukkit.map.MapPalette
import org.bukkit.map.MapRenderer
import org.bukkit.map.MapView
import org.bukkit.map.MinecraftFont
import java.awt.Color

class MapViewBoxOutlineRenderer(
	private val drawData: StructureOutlineData.Box,
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

		renderText(canvas, drawData.title, 1, 0)
		renderText(canvas, "x: ${drawData.minX}...${drawData.maxX}", 1, 9)
		renderText(canvas, "y: ${drawData.minY}...${drawData.maxY}", 1, 18)
		renderText(canvas, "z: ${drawData.minZ}...${drawData.maxZ}", 1, 27)

		map.isTrackingPosition = true
		map.isUnlimitedTracking = true
		map.isLocked = true

		hasRendered = true
	}

	private fun renderText(canvas: MapCanvas, text: String, x: Int, y: Int) {
		// Have not found a replacement for this yet
		@Suppress("DEPRECATION", "removal")
		val foregroundColorIndex = MapPalette.matchColor(Colors.foreground)

		canvas.drawText(x, y, MinecraftFont.Font, "§$foregroundColorIndex;$text")
	}

	private fun drawBoxEdges(canvas: MapCanvas, color: Color) {
		// Convert all coordinates to pixels first
		val minXpx = worldToPixel(drawData.minX, drawData.centerX)
		val maxXpx = worldToPixel(drawData.maxX, drawData.centerX)
		val minZpx = worldToPixel(drawData.minZ, drawData.centerZ)
		val maxZpx = worldToPixel(drawData.maxZ, drawData.centerZ)

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

	private fun worldToPixel(worldCoord: Int, center: Int): Int {
		val blockPerPixel = 1 shl drawData.scale
		return ((worldCoord - center) / blockPerPixel) + 64
	}

	private fun Int.coerceInCanvas() = coerceIn(0, 127)
}