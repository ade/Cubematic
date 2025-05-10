package se.ade.mc.skyblock.structuremaps

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.generator.structure.StructureType
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.map.MapView
import org.bukkit.util.BoundingBox
import org.bukkit.util.StructureSearchResult
import se.ade.mc.skyblock.CubematicSkyPlugin
import kotlin.math.max


fun mapExperiment(
	player: Player,
	plugin: CubematicSkyPlugin,
	structureType: StructureType = StructureType.OCEAN_MONUMENT
) {
	val world = player.world
	val loc = player.location
	val structureKey = structureType.key
	val log = plugin.config.debug

	// Locate the nearest structure
	val findOnlyUnexplored = false
	val result: StructureSearchResult = world.locateNearestStructure(loc, structureType, 10000, findOnlyUnexplored)
		?: return

	if(log) plugin.logger.info { "Locate structure: $result" }

	val resultChunkX = result.location.chunk.x
	val resultChunkZ = result.location.chunk.z

	val struct = world.getStructures(resultChunkX, resultChunkZ)
		.firstOrNull { it.structure.structureType == structureType }
		?: run {
			if (log) plugin.logger.warning { "Structure $structureKey not found at: ${result.location}" }
			return
		}

	val bbox: BoundingBox = struct.boundingBox
	val centerX = bbox.centerX.toInt()
	val centerZ = bbox.centerZ.toInt()

	// Determine the appropriate map scale
	val widthX = bbox.widthX + 1
	val widthZ = bbox.widthZ + 1
	val requiredCoverage = max(widthX, widthZ)

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

	mapView.centerX = centerX
	mapView.centerZ = centerZ
	mapView.scale = MapView.Scale.valueOf(scale.toByte())!!
	mapView.isTrackingPosition = true
	mapView.isUnlimitedTracking = true
	mapView.isLocked = true

	// Log params
	if (log) plugin.logger.info {
		"Make Map params: centerX=$centerX, centerZ=$centerZ, scale=$scale, widthX=$widthX, widthZ=$widthZ, bbox=$bbox"
	}

	val info = StructureOutlineData.Box(
		id = mapView.id,
		title = structureKey.key,
		scale = scale,
		centerX = centerX,
		centerZ = centerZ,
		minX = bbox.minX.toInt(),
		maxX = bbox.maxX.toInt(),
		minZ = bbox.minZ.toInt(),
		maxZ = bbox.maxZ.toInt()
	)

	// Add custom renderer to draw the outline
	mapView.addRenderer(MapViewBoxOutlineRenderer(info))

	// Create the map item and give it to the player
	val mapItem = ItemStack(Material.FILLED_MAP)

	mapItem.itemMeta = (mapItem.itemMeta as MapMeta).also { meta ->
		meta.mapView = mapView
	}

	plugin.config = plugin.config.copy(
		structureMapData = plugin.config.structureMapData.toMutableMap().also {
			it[mapView.id] = info
		}
	)
	player.inventory.addItem(mapItem)
}

