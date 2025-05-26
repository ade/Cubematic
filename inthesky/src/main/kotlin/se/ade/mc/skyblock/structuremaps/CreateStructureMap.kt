package se.ade.mc.skyblock.structuremaps

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.event.server.MapInitializeEvent
import org.bukkit.generator.structure.Structure
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.map.MapView
import org.bukkit.util.BoundingBox
import org.bukkit.util.StructureSearchResult
import se.ade.mc.skyblock.CubematicSkyPlugin
import kotlin.math.max

private val supportedStructures = listOf(
	Structure.MONUMENT,
	Structure.FORTRESS,
	Structure.SWAMP_HUT,
	Structure.PILLAGER_OUTPOST
)

fun createStructureMapOrNull(
	loc: Location,
	plugin: CubematicSkyPlugin,
	structure: Structure,
	title: String
): ItemStack? {
	val world = loc.world
	val log = plugin.config.debug

	val info = structureMapRenderDataOrNull(structure = structure, loc = loc, plugin = plugin)
		?: run {
			if (log) plugin.logger.warning { "Structure '${structure.structureType}' not found at: $loc" }
			return null
		}

	// Create and configure the map
	val mapView = Bukkit.createMap(world)

	// NOTE that we don't delete the default renderers on purpose here
	// because if we do then the locator arrow doesn't show up.
	// Instead, we just set the map as locked right away
	// This allows both the locator and our custom renderer to work.
	// (custom renderer IS invoked even though map is locked)

	mapView.centerX = info.centerX
	mapView.centerZ = info.centerZ
	mapView.scale = MapView.Scale.valueOf(info.scale.toByte())!!
	mapView.isTrackingPosition = true
	mapView.isUnlimitedTracking = true
	mapView.isLocked = true

	// Add custom renderer to draw the outline
	mapView.addRenderer(MapViewBoxOutlineRenderer(info))

	// Create the map item and give it to the player
	val mapItem = ItemStack(Material.FILLED_MAP)

	mapItem.itemMeta = (mapItem.itemMeta as MapMeta).also { meta ->
		meta.mapView = mapView
	}

	plugin.database.structureMapSave(
		mapId = mapView.id,
		structureType = structure.structureType.key.toString(),
		x = info.structure.boundingBox.minX,
		z = info.structure.boundingBox.minZ
	)

	mapItem.itemMeta = mapItem.itemMeta.also {
		it.customName(Component.text("$title Schematic", NamedTextColor.GOLD))
		/*
		it.lore(listOf(
			Component.text("Bounding box map with coordinates", TextColor.color(Color(0x13f832).rgb))
		))
		 */
	}

	return mapItem
}

fun reloadStructureMapData(
	e: MapInitializeEvent,
	plugin: CubematicSkyPlugin
) {
	val data = plugin.database.structureMapLoad(e.map.id)

	val info = data?.let {
		val structure = supportedStructures.firstOrNull {
			data.structureTypeKey == it.structureType.key.toString()
		}
		structure?.let {
			structureMapRenderDataOrNull(structure, Location(e.map.world, e.map.centerX.toDouble(), 0.0, e.map.centerZ.toDouble()), plugin)
		}
	}

	info?.let {
		e.map.addRenderer(MapViewBoxOutlineRenderer(info))
	}
}

private fun structureMapRenderDataOrNull(
	structure: Structure,
	loc: Location,
	plugin: CubematicSkyPlugin
): StructureMapDrawData? {
	val log = plugin.config.debug
	val world = loc.world ?: return null

	// Find only unexplored structures: Should always be false. Otherwise, it does NOT return the nearest structure
	val findOnlyUnexplored = false
	val loc = Location(world, loc.x, loc.y, loc.z)
	val searchRadiusChunks = 100

	val result: StructureSearchResult = world.locateNearestStructure(loc, structure, searchRadiusChunks, findOnlyUnexplored)
		?: run {
			if (log) plugin.logger.warning { "Structure '${structure.structureType}' not found in world: ${world.name} searching from $loc" }
			return null
		}

	if(log) plugin.logger.info { "Locate structure: $result" }

	val resultChunkX = result.location.chunk.x
	val resultChunkZ = result.location.chunk.z

	val generatedStructure = world.getStructures(resultChunkX, resultChunkZ)
		.firstOrNull {
			it.structure == structure
		}
		?: run {
			if (log) plugin.logger.warning { "Structure '${structure.structureType}' not found at: ${result.location}" }
			return null
		}

	val bbox: BoundingBox = generatedStructure.boundingBox
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

	// Log params
	if (log) plugin.logger.info {
		"Make Map params: centerX=$centerX, centerZ=$centerZ, scale=$scale, widthX=$widthX, widthZ=$widthZ, bbox=$bbox"
	}

	return StructureMapDrawData(
		scale = scale,
		centerX = centerX,
		centerZ = centerZ,
		structure = generatedStructure
	)
}