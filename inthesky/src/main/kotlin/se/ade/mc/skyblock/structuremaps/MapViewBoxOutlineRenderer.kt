package se.ade.mc.skyblock.structuremaps

import org.bukkit.entity.Player
import org.bukkit.map.MapCanvas
import org.bukkit.map.MapRenderer
import org.bukkit.map.MapView
import java.awt.Color
import java.awt.Font
import java.awt.Image
import java.awt.RenderingHints
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import kotlin.collections.forEach

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

		val minXpx = worldToMapCoord(drawData.minX, drawData.centerX)
		val maxXpx = worldToMapCoord(drawData.maxX, drawData.centerX)
		val minZpx = worldToMapCoord(drawData.minZ, drawData.centerZ)
		val maxZpx = worldToMapCoord(drawData.maxZ, drawData.centerZ)

		val mapImageBuilder = MapImageBuilder(colors)
		mapImageBuilder.fill(colors.background)
		mapImageBuilder.title(drawData.title)
		mapImageBuilder.axisLabelX("${drawData.minX} < x < ${drawData.maxX}")
		mapImageBuilder.axisLabelY("${drawData.minY} < y < ${drawData.maxY}")
		mapImageBuilder.axisLabelZ("${drawData.minZ} < z < ${drawData.maxZ}")
		mapImageBuilder.boundingBox(minXpx, minZpx, maxXpx, maxZpx)

		val chunkX = drawData.centerX shr 4
		val chunkZ = drawData.centerZ shr 4
		val parts = map.world
			?.getStructures(chunkX, chunkZ)
			?.firstOrNull { drawData.centerY >= it.boundingBox.minY && drawData.centerY <= it.boundingBox.maxY }
			?.pieces

		parts?.forEach { part ->
			val box = part.boundingBox
			val partMinXpx = worldToMapCoord(box.minX.toInt(), drawData.centerX)
			val partMaxXpx = worldToMapCoord(box.maxX.toInt(), drawData.centerX)
			val partMinZpx = worldToMapCoord(box.minZ.toInt(), drawData.centerZ)
			val partMaxZpx = worldToMapCoord(box.maxZ.toInt(), drawData.centerZ)

			mapImageBuilder.boundingBox(partMinXpx, partMinZpx, partMaxXpx, partMaxZpx)
		}

		canvas.drawImage(0, 0, mapImageBuilder.build())

		map.isTrackingPosition = true
		map.isUnlimitedTracking = true
		map.isLocked = true

		hasRendered = true
	}

	private fun worldToMapCoord(worldCoord: Int, center: Int): Int {
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

private class MapImageBuilder(private val colors: Colors) {
	private val fontStream = this::class.java.classLoader.getResourceAsStream("fonts/mapfont.bin")
		?: throw IllegalStateException("Font file not found")

	private val font = Font.createFont(Font.TRUETYPE_FONT, fontStream)
		.deriveFont(8f)

	private val image = BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB)
	private val graphics = image.createGraphics().also { graphics ->
		graphics.font = font
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF)
		graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF)
	}

	fun fill(color: Color) {
		graphics.color = color
		graphics.fillRect(0, 0, image.width, image.height)
	}

	fun title(text: String) {
		val bounds = font.getStringBounds(text, graphics.fontRenderContext)
		graphics.color = colors.foregroundText
		graphics.transform = AffineTransform()
		graphics.drawString(text, (64 - bounds.width/2).toInt(), bounds.height.toInt())
	}

	fun axisLabelX(text: String) {
		val bounds = font.getStringBounds(text, graphics.fontRenderContext)
		graphics.color = colors.foregroundText
		graphics.transform = AffineTransform()
		graphics.drawString(text, (64 - bounds.width/2).toInt(), 126)
	}

	fun axisLabelY(text: String) {
		val bounds = font.getStringBounds(text, graphics.fontRenderContext)
		graphics.color = colors.foregroundText
		graphics.transform = AffineTransform()
		graphics.translate(
			(127 - bounds.height + 4).toInt().coerceInCanvas(),
			(64 - bounds.width/2).toInt()
		)
		graphics.rotate(Math.toRadians(90.0))
		graphics.drawString(text, 0, 0)
	}

	fun textTopLeft(text: String, left: Int, top: Int) {
		val bounds = font.getStringBounds(text, graphics.fontRenderContext)
		graphics.color = colors.foregroundText
		graphics.transform = AffineTransform()
		graphics.transform = graphics.transform.apply {
			translate(left.toDouble(), (top + bounds.height.toInt()).toDouble())
		}
		graphics.drawString(text, 0, 0)
	}

	fun axisLabelZ(text: String) {
		val bounds = font.getStringBounds(text, graphics.fontRenderContext)
		graphics.color = colors.foregroundText
		graphics.transform = AffineTransform()
		graphics.translate(1.0, 64 - bounds.width/2)
		graphics.rotate(Math.toRadians(90.0))
		graphics.drawString(text, 0, 0)
	}

	fun boundingBox(
		minXpx: Int,
		minZpx: Int,
		maxXpx: Int,
		maxZpx: Int,
	) {
		graphics.color = colors.foregroundLine

		var dash = 2
		// Horizontal lines (Z edges)
		(minXpx..maxXpx).forEach { x ->
			if(dash > 0 || x == maxXpx) {
				paintLinePixel(x.coerceInCanvas(), minZpx.coerceInCanvas())
				paintLinePixel(x.coerceInCanvas(), maxZpx.coerceInCanvas())
				dash--
			} else {
				dash = 2
			}
		}

		dash = 2
		// Vertical lines (X edges)
		(minZpx..maxZpx).forEach { z ->
			if(dash > 0 || z == maxZpx) {
				paintLinePixel(minXpx.coerceInCanvas(), z.coerceInCanvas())
				paintLinePixel(maxXpx.coerceInCanvas(), z.coerceInCanvas())
				dash--
			} else {
				dash = 2
			}
		}
	}

	private fun paintLinePixel(x: Int, y: Int) {
		val current = image.getRGB(x, y)
		when (current) {
			colors.background.rgb -> {
				image.setRGB(x, y, colors.foregroundLine.rgb)
			}
			colors.foregroundText.rgb -> {
				image.setRGB(x, y, colors.foregroundMixed.rgb)
			}
		}
	}

	fun build(): Image {
		graphics.dispose()
		return image
	}
}