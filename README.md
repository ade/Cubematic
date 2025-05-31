# Cubematic
A few of my implementations of Minecraft plugins for Paper-based servers.

## Automation
Automation plugin for Paper-based Minecraft servers.
- Block breaker
  - Breaks block in front of it and drops block as item
  - Uses tool(s)
  - Drains durability on tools used
  - One break per redstone activation
  - Uses correct speed depending on block and tool type
- Block placer
  - Places block from dispenser.

## InTheSky
Skyblock-like plugin for Paper-based Minecraft servers.
- Overworld generation
  - Generates a starter island with dirt and an acacia tree.
  - Classic starter island can optionally be generated (skips obtaining grass, lava, water)
  - Void world
  - (inactive) Microbiomes (small biomes around the player in a grid)
  - Structure boxes generation from vanilla generator (technical only - no blocks!)
- Nether generation
  - Void nether
  - (inactive) Microbiomes
  - Structure boxes generation from vanilla generator (technical only - no blocks!)
- Wandering trader spawns much more often
  - Sleeping in a bed grants a chance to spawn a wandering trader.
  - The trader has a spawn chance based on the player with the highest level in the world.
  - Each level gives a 1% chance to spawn the trader, all the way up to a max of 100% at level 100+.
  - The trader will spawn next to the player with the highest level, within a 16 block radius
  - The trader will only spawn if there isn't already one in the world.
  - Trader will despawn after 2 days (as a naturally spawned one)
  - This trader does not spawn with llamas
  - Vanilla trader spawning has not been modified, but might not happen if a trader is already in the world.
  - Pushing cobblestone into lava with a piston will create deepslate in the overworld or netherrack in the nether.
- Grass block
  - Turn dirt into grass by right-clicking it with short grass
- Charcoal
  - Set a log block on fire and wait for it to burn up. It will drop a charcoal item.
  - Campfire allows burning logs to get charcoal.
- Lava
  - Wandering Trader sells lava buckets for 10 echo shards
  - Wandering Trader sells echo shards for random items.
    - These items are hard to obtain and require some effort to get.
    - The same trades are offered on every wandering trader until one is completed.
    - There are two of these trades available at a time, each usable once.
- Structures for special spawns
  - Wandering Trader sells schematics to get location and see bounds of structures (for farms).
    - Monument
    - Swamp Hut
    - Pillager Outpost
  - Nether Fortress
    - Piglins have a small chance to add a map of a nether fortress when bartering.
- Sand
  - Cut glass blocks and glass bottles into sand blocks on the stonecutter.
- Deep slate
  - When cobblestone is pushed into lava with a piston, it turns into deepslate.
- Netherrack
  - When cobblestone is pushed into lava with a piston in the nether, it turns into netherrack.
- 

### Enabling
Edit bukkit.yml, set the generator for the world to CubematicSkyPlugin for both overworld and nether.
```yaml
worlds:
  world:
    generator: Cubematic-InTheSky
  world_nether:
    generator: Cubematic-InTheSky:nether
```

## Portals
This plugin creates a custom portal system in Minecraft with the following features:

### Breakable End Frame blocks
End Frame blocks can be broken and picked up by the player with at least an iron pickaxe.
When broken, the portal inside, if any, is destroyed. To reactivate the portal, a new frame block
must be placed (in the correct direction) and be fed with an Eye of Ender. 
  - To enable, set in config.yml: ```breakableEndFrames: true```


### Custom Portal Structures
Player-craftable portals that can be used to teleport between custom locations.

- **Frame Material**: Nether portal shape but made of Crying Obsidian
- **Portal Material**: End Gateway blocks (added automatically upon activation and removed upon deactivation)
- **Orientations**: Works in all three planes (XY, ZY, XZ)
- **Size Limit**: Maximum portal side of 21 blocks
- **Uses regular game mechanics for portals**
  - no custom teleportation code
  - works even if plugin is disabled

#### Creation Process
1. Player creates a lodestone at the intended destination
2. Player obtains a lodestone compass pointed at the lodestone
3. Enchant the compass with Curse of Vanishing
4. Build a rectangular frame from Crying Obsidian, as if building a nether portal
5. Holding the compass, right-click INSIDE the frame (as if you were lighting a nether portal):
  - Interior fills with End Gateway blocks
  - Gateways are configured to teleport to the compass's destination
  - The compass vanishes

#### Teleportation
- Using a portal costs 4 hunger points
- If player has insufficient food, teleportation fails
- Non-player entities can be teleported but are blocked by default (because it would be op
  in farms, but can be enabled (todo: config file))

#### Breaking Portals
When breaking a frame block:
- Connected gateway blocks are removed
- The compass drops that tracked the portal's destination
- This compass can be used again to create another linked portal


#### Limitations
- Interdimensional travel is not supported
- Moving/destroying the lodestone does not affect the portal once created