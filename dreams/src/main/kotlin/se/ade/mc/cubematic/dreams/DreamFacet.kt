package se.ade.mc.cubematic.dreams

import com.onarandombox.MultiverseCore.MultiverseCore
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerBedEnterEvent
import org.bukkit.plugin.java.JavaPlugin
import se.ade.mc.cubematic.dreams.datastore.SkyDb
import se.ade.mc.cubematic.dreams.inventory.PlayerDreamInventories
import se.ade.mc.cubematic.dreams.playerdata.PlayerDreamPhaser


private const val GLOBAL_DREAM_WORLD_NAME = "dreamworld"

class DreamFacet(private val plugin: JavaPlugin) : Listener {
    private var dreamWorld: World? = null

    private val server = plugin.server
    private val logger = plugin.logger
    private val db = SkyDb(plugin)
    private val dreamInventory = PlayerDreamInventories(db, logger)
    private lateinit var phaser: PlayerDreamPhaser
    private lateinit var multiverse: MultiverseCore

    private val overworld by lazy {
        server.getWorld("world")
            server.worlds.first { it.environment == World.Environment.NORMAL }
            ?: throw IllegalStateException("Could not find an overworld (either named 'world' or type NORMAL)")
    }

    fun onEnable() {
        // Register events and commands
        server.pluginManager.registerEvents(this, plugin)

        multiverse = Bukkit.getServer().pluginManager.getPlugin("Multiverse-Core") as MultiverseCore?
            ?: throw IllegalStateException("Multiverse-Core is required but plugin not found")

        phaser = PlayerDreamPhaser(overworld, dreamInventory, db, logger, multiverse)

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

    fun onCreateDreamWorldCommand() {
        createDreamWorld()
    }

    fun onEnterDreamWorldCommand(player: Player) {
        player.sendMessage("Starting dream")
        beginDream(player)
    }

    fun onLeaveDreamWorldCommand(player: Player) {
        player.sendMessage("Ending dream")
        endDream(player)
    }

    fun onDestroyDreamWorldCommand() {
        destroyDreamWorld()
    }

    fun onStashInventoryCommand(player: Player) {
        dreamInventory.stash(player)
        player.sendMessage("Inventory stashed")
    }

    fun onPopInventoryCommand(player: Player) {
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

        val result = multiverse.mvWorldManager.addWorld(
            GLOBAL_DREAM_WORLD_NAME,    /* World Name */
            World.Environment.NORMAL,   /* Environment */
            null,                       /* Seed */
            WorldType.NORMAL,           /* World Type */
            false,                      /* Generate Structures */
            "Cubematic-Dreams"     /* Generator */
        )


        val world = server.getWorld(GLOBAL_DREAM_WORLD_NAME)
        if(!result || world == null) {
            logger.warning("Failed to create dream world (world: $world, result: $result)")
            return
        }

        world.setSpawnLocation(8, 64, 8)
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
        logger.info("Dream world created")
    }

    private fun destroyDreamWorld() {
        if(dreamWorld == null) {
            if(multiverse.mvWorldManager.loadWorld(GLOBAL_DREAM_WORLD_NAME)) {
                dreamWorld = server.getWorld(GLOBAL_DREAM_WORLD_NAME)
            }
        }
        dreamWorld?.let { world ->
            // Make sure no players are in the world
            world.players.forEach { player ->
                teleportPlayerBack(player)
            }

            // Unload the world
            multiverse.mvWorldManager.unloadWorld(GLOBAL_DREAM_WORLD_NAME)
            multiverse.mvWorldManager.deleteWorld(GLOBAL_DREAM_WORLD_NAME)
        }

        dreamWorld = null
    }

    private fun teleportPlayerBack(player: Player) {
        logger.info { "teleportPlayerBack..." }
        val homeLocation = overworld.spawnLocation
        player.teleport(homeLocation)
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