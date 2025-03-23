Minecraft Spawn Chunks
Spawn chunks are special chunks at world spawn that remain loaded even when no players are nearby, unlike normal chunks.  

Configurable via gamerule spawnChunkRadius (default: 2, creates 48×48 block area)
Legacy size can be restored with value 10, but reduces performance
Allows continuous processing of game mechanics (farms, redstone) without player presence
Friendly mobs in spawn chunks affect mob cap, potentially limiting (friendly) mob spawns elsewhere

Technical Implementation
For a given `spawnChunkRadius` value of N, the following areas are created:

| Area Type      | Size                 | Functionality                                                  |
|----------------|----------------------|----------------------------------------------------------------|
| Entity Ticking | (2N-1)×(2N-1) chunks | Full functionality - entities move, redstone works, crops grow |
| Ticking        | (2N+1)×(2N+1) chunks | Everything active except entity movement and chunk ticks       |
| Border         | (2N+3)×(2N+3) chunks | Limited functionality - no redstone or command blocks          |
| Inaccessible   | Beyond               | Only world generation occurs                                   |

### Examples:
- With default setting (`spawnChunkRadius: 2`):
    - Entity Ticking: 3×3 chunk area (48×48 blocks)
    - Ticking: 5×5 chunk area
    - Border: 7×7 chunk area

- With legacy setting (`spawnChunkRadius: 10`):
    - Entity Ticking: 19×19 chunk area
    - Ticking: 21×21 chunk area
    - Border: 23×23 chunk area

Farms and redstone contraptions built in spawn chunks will run continuously, making them ideal for automated resource collection.