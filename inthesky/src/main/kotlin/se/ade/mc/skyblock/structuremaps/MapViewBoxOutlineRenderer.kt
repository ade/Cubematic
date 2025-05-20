package se.ade.mc.skyblock.structuremaps

import org.bukkit.entity.Player
import org.bukkit.map.MapCanvas
import org.bukkit.map.MapPalette
import org.bukkit.map.MapRenderer
import org.bukkit.map.MapView
import org.bukkit.map.MinecraftFont
import java.awt.Color
import java.awt.Font
import java.awt.image.BufferedImage

class MapViewBoxOutlineRenderer(
	private val drawData: StructureOutlineData.Box,
) : MapRenderer() {
	private var hasRendered = false

	override fun render(map: MapView, canvas: MapCanvas, player: Player) {
		if (hasRendered) return

		// Initialize the palette by attempting to set the colors and then retrieving the actual mapped colors
		canvas.setPixelColor(0, 0, Colors.default.background)
		canvas.setPixelColor(1, 0, Colors.default.foregroundLine)
		canvas.setPixelColor(2, 0, Colors.default.foregroundMixed)
		canvas.setPixelColor(3, 0, Colors.default.foregroundText)

		val colors = Colors(
			background = canvas.getPixelColor(0, 0)!!,
			foregroundLine = canvas.getPixelColor(1, 0)!!,
			foregroundMixed = canvas.getPixelColor(2, 0)!!,
			foregroundText = canvas.getPixelColor(3, 0)!!,
		)

		// Fill the map with a color
		for (x in 0..127) {
			for (y in 0..127) {
				canvas.setPixelColor(x, y, colors.background)
			}
		}

		renderText(canvas, drawData.title, 1, 0)
		renderText(canvas, "${drawData.minX} < x < ${drawData.maxX}", 1, 9)
		renderText(canvas, "${drawData.minY} < y < ${drawData.maxY}", 1, 18)
		renderText(canvas, "${drawData.minZ} < z < ${drawData.maxZ}", 1, 27)

		// Draw all four edges
		drawBoxEdges(canvas, colors)

		map.isTrackingPosition = true
		map.isUnlimitedTracking = true
		map.isLocked = true

		hasRendered = true
	}

	private fun renderText(canvas: MapCanvas, text: String, x: Int, y: Int) {
		// Have not found a replacement for this yet
		@Suppress("DEPRECATION", "removal")
		val foregroundColorIndex = MapPalette.matchColor(Colors.default.foregroundText)
		canvas.drawText(x, y, MinecraftFont.Font, "§$foregroundColorIndex;$text")
	}

	private fun drawBoxEdges(canvas: MapCanvas, colors: Colors) {
		// Convert all coordinates to pixels first
		val minXpx = worldToPixel(drawData.minX, drawData.centerX)
		val maxXpx = worldToPixel(drawData.maxX, drawData.centerX)
		val minZpx = worldToPixel(drawData.minZ, drawData.centerZ)
		val maxZpx = worldToPixel(drawData.maxZ, drawData.centerZ)

		var dash = 2
		// Horizontal lines (Z edges)
		(minXpx..maxXpx).forEach { x ->
			if(dash > 0 || x == maxXpx) {
				paintLinePixel(x.coerceInCanvas(), minZpx.coerceInCanvas(), canvas, colors)
				paintLinePixel(x.coerceInCanvas(), maxZpx.coerceInCanvas(), canvas, colors)
				dash--
			} else {
				dash = 2
			}
		}

		dash = 2
		// Vertical lines (X edges)
		(minZpx..maxZpx).forEach { z ->
			if(dash > 0 || z == maxZpx) {
				paintLinePixel(minXpx.coerceInCanvas(), z.coerceInCanvas(), canvas, colors)
				paintLinePixel(maxXpx.coerceInCanvas(), z.coerceInCanvas(), canvas, colors)
				dash--
			} else {
				dash = 2
			}
		}
	}

	private fun paintLinePixel(x: Int, y: Int, canvas: MapCanvas, colors: Colors) {
		val current = canvas.getPixelColor(x, y)
		when (current) {
			null, colors.background -> {
				canvas.setPixelColor(x, y, colors.foregroundLine)
			}
			colors.foregroundText -> {
				canvas.setPixelColor(x, y, colors.foregroundMixed)
			}
		}
	}

	private fun worldToPixel(worldCoord: Int, center: Int): Int {
		val blockPerPixel = 1 shl drawData.scale
		return ((worldCoord - center) / blockPerPixel) + 64
	}
}

private data class Colors(
	val background: Color,
	val foregroundLine: Color,
	val foregroundMixed: Color,
	val foregroundText: Color,
) {
	companion object {
		val default = Colors(
			background = Color(0xD1B1A1),
			foregroundLine = Color(0xD87F33),
			foregroundMixed = Color(0x72431B),
			foregroundText = Color(0x3B3B3B),
		)
	}
}

private fun Int.coerceInCanvas() = coerceIn(0, 127)