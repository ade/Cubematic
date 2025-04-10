# Cubematic InTheSky - SkyBlock with more progression

## Introduction
This is a Minecraft Paper server plugin that facilitates a skyblock-like
game mode with more progression. Some items are not renewable and/or
obtainable in a skyblock world. This plugin aims to fix that by adding
recipes or other means to obtain these items.

The game starts (like classic skyblock) with a small island with a tree
and a chest with a few basic resources. Players must use these resources
to expand their island and gather more resources.

## Design goals
- Fix progression: Make as many items obtainable as possible, but in a balanced way
- Intuitive and classic gameplay: Keep the classic skyblock gameplay
and add new features that fit the game mode.
- Discoverability: Make it easy for players to discover new features
  and progression steps. Must not rely on commands or external guides.
- Reduce grind: Make the game less grindy and more fun to play
- Roguelike elements: Add some roguelike elements to the game, such as
giving players a choice of what to unlock next.
- Use as much of classic rules as possible. Knowledge of the classic
skyblock game mode should be useful in this game mode.
- Automation: Integrate with Cubematic automation features, such as automated
    block breaking and placement

  
## Non-goals
- Add new game modes: This project is focused on fixing the progression
- Add new features that don't fit the skyblock game mode
- Make the game too easy: The game should still be challenging
- Changing core game mechanics
- Command-based interaction. All features should be accessible in-game

## Implementation
The project will be implemented as a Minecraft Paper server plugin.
The plugin may add new crafting recipes, blocks, items, and other
features to the game.

## Reference of current mechanics
There are multiple ways a player can progress in a skyblock world.
Some examples of classic progression:

### Early game
- Cobblestone generator: The initial cobblestone generator is the
main source of resources in the early game.
- Tree farm: Trees are a good source of wood and saplings.
- Trader: The trader can be used to trade items for other items.

### Mid game
- Mob farm: Mobs drop useful items such as bones, string, and gunpowder.
- Crop farm: Crops can be grown to produce food and other resources.
- Villager: Villagers can be used to trade items for other items.
- Nether portal: The nether portal can be used to access the nether, which
  should also be an empty void world, but allows spawning certain mobs etc

### Late game
- Automated farms: Automated farms can be built to produce resources
- Enchanting: Enchanting can be used to improve items
- End portal: The end portal is not accessible in a normal skyblock game.

## Progression enhancements
- The End: The end is normally not accessible, but the plugin
  can add a way to access the end and defeat the ender dragon, get elytra, etc.
- More traders: Plugin could make more types of traders spawn.