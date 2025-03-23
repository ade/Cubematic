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
Skyblock like plugin for Paper-based Minecraft servers.
- Overworld generation
  - Classic starter island
  - Void world
  - Microbiomes (small biomes around the player in a grid)
- Nether generation
  - Void nether
  - Microbiomes
- Progression / Unlocking of more content
  - TBD

## Portals
This plugin creates a custom portal system in Minecraft with the following features:

### Portal Structure
- **Frame Material**: Nether portal shape but made of Crying Obsidian
- **Portal Material**: End Gateway blocks
- **Orientations**: Works in all three planes (XY, ZY, XZ)
- **Size Limit**: Maximum portal side of 21 blocks
- **Uses regular game mechanics for portals**
  - no custom teleportation code
  - works even if plugin is disabled

### Creation Process
1. Player creates a lodestone at the intended destination
2. Player obtains a lodestone compass pointed at the lodestone
3. Enchant the compass with Curse of Vanishing
4. Build a rectangular frame from Crying Obsidian, as if building a nether portal
5. Holding the compass, right-click INSIDE the frame (as if you were lighting a nether portal):
  - Interior fills with End Gateway blocks
  - Gateways are configured to teleport to the compass's destination
  - The compass vanishes

### Teleportation
- Using a portal costs 4 hunger points
- If player has insufficient food, teleportation fails
- Non-player entities can be teleported but are blocked by default (because it would be op
  in farms, but can be enabled (todo: config file))

### Breaking Portals
When breaking a frame block:
- Connected gateway blocks are removed
- The compass drops that tracked the portal's destination
- This compass can be used again to create another linked portal


### Limitations
- Interdimensional travel is not supported
- Moving/destroying the lodestone does not affect the portal once created