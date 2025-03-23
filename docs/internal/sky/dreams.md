DREAMS
-------------
Sometimes when sleeping, players get sent to a dream world.
- The destination is a new world.
- The destination can contain random content, e.g.:
  - A chest with loot
  - A dungeon
  - A village
  - An adventure map

- The destination should have a way back to the original island, e.g.
    - A portal back
    - A way to build a new portal back
    - Teleporting if you fall off the island
    - Teleporting if you die, but the death is prevented and you return from your dream as you wake up

- Destinations are meant to be ephemeral and not permanent. The player should
  not be able to return to the same destination or just stay there forever.
  - If the player's only way to get back is to die, this can be seen as an interesting
    twist on the hardcore gameplay mode! A player could stay on the new island
    as long as they want, but if they die, they lose everything and are sent back
    to the original island.
  - The generated area should be reset after the player leaves it. This could be done
    by deleting the chunks that were generated for the destination.
  - In multiplayer, the destination could be shared with other players, but the
    destination should still be reset after the player leaves it.
    - If one player dies, the destination is reset for all players.
    - Or alternatively, the destination is reset for all players when the last player leaves.
    - Or alternatively, the destination is reset after a certain amount of time has passed
      after the first player has died.

- Ender chests could be used to transfer items between the islands, if inventories
  are not kept between islands. Ender chests are hard to make though, so this
  would be a late-game feature? Or perhaps the player could find an ender chest in
  the destination, but to use it they would need an ender chest in the origin island.
  
- What if certain "ingredients" trigger certain destinations?
  - This could be a way to add some strategy to the game. The player
      could have to decide what they want to get out of the dream, and then find the
      right ingredients to use. (how to use them is TBD)
  - The player could also try to figure out
      the "recipe" for each destination, by experimenting with different ingredients.
      This could be a fun and challenging puzzle for the player to solve.
 
- Inventory, armor, health, food, etc is snapshot and saved when going to dreamworld.
    - When returning, the player is restored to the state they were in when they left.

Some things to consider:
- Turn off Trader spawning in destination
- Turn off mob spawning in destination?
  

# Technical notes
## Inventory management
- The player's inventory can be saved when they go through the portal by calling
  `player.getInventory().getContents()` and `player.getInventory().getArmorContents()` and then using
  https://jd.papermc.io/paper/1.21.4/org/bukkit/inventory/ItemStack.html#serializeAsBytes()
  https://jd.papermc.io/paper/1.21.4/org/bukkit/inventory/ItemStack.html#deserializeBytes(byte[])
- 