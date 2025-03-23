package se.ade.mc.skyblock.dream

import net.kyori.adventure.util.TriState
import org.bukkit.ChatColor
import org.bukkit.GameRule
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.WorldType
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerBedEnterEvent
import se.ade.mc.skyblock.CubeInTheSkyPlugin
import se.ade.mc.skyblock.DreamCommandHandler
import se.ade.mc.skyblock.datastore.SkyDb
import se.ade.mc.skyblock.dream.inventory.PlayerDreamInventories
import se.ade.mc.skyblock.dream.playerdata.PlayerDreamPhaser
import java.io.File

private const val GLOBAL_DREAM_WORLD_NAME = "dreamworld"

class DreamFacet(
	private val plugin: CubeInTheSkyPlugin,
	private val overworld: World
) : Listener, DreamCommandHandler {
    private var dreamWorld: World? = null

    private val server = plugin.server
    private val logger = plugin.logger
    private val db = SkyDb(plugin)
    private val dreamInventory = PlayerDreamInventories(db, logger)
    private val phaser = PlayerDreamPhaser(overworld, dreamInventory, db, logger)

    fun onEnable() {
        // Register events and commands
        server.pluginManager.registerEvents(this, plugin)

        logger.info("DreamFacet enabled for world: '${overworld.name}'")
    }

    fun onDisable() {
        // Teleport all players back to the main world
        if (dreamWorld != null) {
            val world = dreamWorld!!
            world.players.forEach { player ->
                teleportPlayerBack(player)
            }

            // Unload and delete the dream world
            destroyDreamWorld()
        }

        logger.info("DreamBlock plugin disabled")
    }

    override fun onCreateDreamWorldCommand() {
        createDreamWorld()
    }

    override fun onEnterDreamWorldCommand(player: Player) {
        player.sendMessage("Starting dream")
        beginDream(player)
    }

    override fun onLeaveDreamWorldCommand(player: Player) {
        player.sendMessage("Ending dream")
        endDream(player)
    }

    override fun onDestroyDreamWorldCommand() {
        destroyDreamWorld()
    }

    override fun onStashInventoryCommand(player: Player) {
        dreamInventory.stash(player)
        player.sendMessage("Inventory stashed")
    }

    override fun onPopInventoryCommand(player: Player) {
        dreamInventory.pop(player)
        player.sendMessage("Inventory restored")
    }

    @EventHandler
    fun onBed(event: PlayerBedEnterEvent) {
        event.setUseBed(Event.Result.ALLOW)
        if(event.player.world.uid == overworld.uid) {
            beginDream(event.player)
        }
    }

    @EventHandler
    fun onPlayerDamage(event: EntityDamageEvent) {
        val entity = event.entity

        // Check if it's a player in the dream world
        if (entity is Player && entity.isDreaming()) {
            // Check if this damage would be fatal
            if (entity.health - event.finalDamage <= 0) {
                // Cancel the damage event to prevent death
                event.isCancelled = true

                // Send message to the player
                entity.sendMessage("${ChatColor.ITALIC}You awake abruptly")

                // End the dream and return them to the real world
                endDream(entity)
            }
        }
    }

    private fun Player.isDreaming(): Boolean {
        return this.world.uid == dreamWorld?.uid
    }

    private fun createDreamWorld() {
        // Clean up any existing dream world
        destroyDreamWorld()

        // Create a new world
        val worldName = GLOBAL_DREAM_WORLD_NAME

        val world = WorldCreator(worldName)
            .environment(World.Environment.NORMAL)
            .type(WorldType.FLAT)
            .generator(DreamChunkGenerator())
            .generateStructures(false)
            .keepSpawnLoaded(TriState.FALSE)
            .createWorld()!!


        world.setSpawnLocation(0, 100, 0)
        world.time = 1000 // Early morning
        world.setStorm(false)
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false)
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false)
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
        world.setGameRule(GameRule.DO_INSOMNIA, false)
        world.setGameRule(GameRule.DO_PATROL_SPAWNING, false)
        world.setGameRule(GameRule.DO_TRADER_SPAWNING, false)
        world.setGameRule(GameRule.DISABLE_RAIDS, true)

        world.isAutoSave = false

        dreamWorld = world
    }

    private fun destroyDreamWorld() {
        if(dreamWorld == null) {
            dreamWorld = server.getWorld(GLOBAL_DREAM_WORLD_NAME)
        }
        dreamWorld?.let { world ->
            // Make sure no players are in the world
            world.players.forEach { player ->
                teleportPlayerBack(player)
            }

            // Unload the world
            server.unloadWorld(world, false)

            // Delete the world directory
            try {
                val worldFolder = world.worldFolder
                if (worldFolder.exists() && worldFolder.isDirectory) {
                    deleteRecursively(worldFolder)
                }
            } catch (e: Exception) {
                logger.warning("Failed to delete dream world: ${e.message}")
            }
        }

        dreamWorld = null
    }

    private fun teleportPlayerBack(player: Player) {
        logger.info { "teleportPlayerBack..." }
        val homeLocation = overworld.spawnLocation
        player.teleport(homeLocation)
    }

    private fun deleteRecursively(file: File) {
        if (file.isDirectory) {
            file.listFiles()?.forEach { deleteRecursively(it) }
        }
        logger.info {
            "Deleting file: ${file.name}"
        }
        file.delete()
    }

    private fun beginDream(player: Player) {
        if(dreamWorld == null) {
            createDreamWorld()
        }
        dreamWorld?.let {
            phaser.begin(player, it)
        }
    }

    private fun endDream(player: Player) {
        phaser.end(player)

        if(dreamWorld != null && dreamWorld?.players?.isEmpty() == true) {
            destroyDreamWorld()
        }
    }
}