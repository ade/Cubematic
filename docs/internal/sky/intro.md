# Minecraft SkyBlock with Progression fixes

## Introduction
The goal of this project is to create a Minecraft Paper server plugin
The game mode is based on the popular skyblock game mode.
The purpose of this project is to fix the progression of the game mode.
Some items are not renewable and/or obtainable in a skyblock world.
This project aims to fix that by adding recipes or other means to obtain
these items.

Classic skyblock games start with a small island with a tree and a chest
with a few basic resources. Players must use these resources to expand
their island and gather more resources. The tech tree will guide players
through the progression of the game, unlocking new resources and
crafting recipes as they progress. Eventually, players will be able to
build a large island with many resources and automated farms.

## Design goals
- Fix progression: Make all items obtainable in a balanced way
- Intuitive and classic gameplay: Keep the classic skyblock gameplay
and add new features that fit the game mode.
- Discoverability: Make it easy for players to discover new features
  and progression steps. Must not rely on commands or external guides.
- Reduce grind: Make the game less grindy and more fun to play
- Roguelike elements: Add some roguelike elements to the game, such as
giving players a choice of what to unlock next.
- Automation: Add automation features to the game, such as automated
block breaking and placement
- Use as much of classic rules as possible. Knowledge of the classic
skyblock game mode should be useful in this game mode.


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
- Nether portal: The nether portal can be used to access the nether.
  (we need to find a good way to deal with the imbalance of the nether)

### Late game
- Automated farms: Automated farms can be built to produce resources
- Enchanting: Enchanting can be used to improve items
- End portal: The end portal can be used to access the end dimension
  (again, we need to find a good way to deal with the imbalance of the end)

## Research of progression: What leads to what
It is not simple to create a map of all possible progression steps in minecraft.
The progression also changes from version to version as new mechanics are added.
It would be useful to have an automated way of creating a map of progression.
This could be done by analyzing the crafting recipes and other mechanics,
and creating a graph of all possible progression steps.

All crafting recipes are stored in the game files, so it should be possible
to extract this information and create a graph of progression steps for the crafting
part of the mechanics.

We could also analyze the other mechanics, such as mob drops, and create a graph
of progression steps for these mechanics, or ideally, include all mechanics in one
big graph/map, that is machine readable and queryable.

To facilitate this, we should define some terms and concepts for progression.
Some examples of terms that could be useful:
- CRAFT: A crafting recipe that can be used to create an item.
  - for example, planks can be crafted from logs
- DROP: A mob drop that can be used to obtain an item.
  - for example, bones can be dropped by skeletons
- TRADE: A trade that can be used to obtain an item.
  - for example, a villager can trade iron for emeralds
- SPAWN: A mob spawn that can be used to obtain an mob.
  - for example, skeletons spawn anywhere in the dark
- GROW: A crop that can be grown to obtain an item or block
  - for example, wheat can be grown to obtain more wheat
- BUILD: A block that can be placed to obtain an item.
  - for example, a cobblestone generator can be built to obtain cobblestone.
  these are usually machines or structures that can be built, which is pretty
  rare in vanilla minecraft. More examples include a mob farm or an infinite water source
- USE: An item that can be used to obtain an item.
  - for example, a bucket can be used to obtain water and an empty bucket
- BREAK: A block that can be broken to obtain an item.
  - for example, a melon block can be broken to obtain melon slices

These are all verbs that describe actions that can be taken to obtain items.
There are also prerequisites for these actions, such as having the right materials,
or being in the right place. These prerequisites can be described as conditions
that must be met before the action can be taken. For example, to craft planks,
you need to have unlocked growing trees. To grow trees, you need to have a sapling
and dirt. To get dirt and a sapling, well that's included in the starter pack.

Prerequisities can be classified into different categories, such as:
- ITEM: A material that is needed to perform an action, e.g.
  - a sapling is needed to grow a tree
  - a bucket is needed to obtain water
  - a diamond pickaxe is needed to mine obsidian
- BLOCK: A block type that is needed to perform an action.
  - e.g. a melon block is needed to break it to obtain melon slices
- STRUCTURE: A structure is a certain placement and availability of blocks
  - e.g. cobblestone generator, infinite water source, mob farm
- LOCATION: A location that is needed to perform an action.
  - for example, slime mobs spawn in certain Y-coordinates and chunks
- MOB: A mob that is needed to perform an action.
  - for example, a villager is needed to trade items, and you need to
    have a two villagers to breed them

These are all nouns that describe things that are needed to perform actions.
There are other types of prerequisites, such as time, luck, or skill
conditions. These are mostly a product of playing the game long enough and are
not really part of the progression tree.

The progression tree can be represented as a directed graph, where the nodes
are items, blocks, structures, locations, mobs, and the edges are the actions
that can be taken to obtain these nodes. The graph can be queried to find
all possible progression steps from a given starting point, or to find
the shortest path to a certain goal. The graph can also be used to generate
a tech tree that guides players through the progression of the game.

Ideally, the graph should be generated automatically from the game files,
so that it is always up to date with the latest version of the game.
This would require a tool that can extract the crafting recipes and other
mechanics from the game files and create a graph of progression steps
from this information. The graph should be machine readable and queryable,
so that it can be used to generate a tech tree or other game content.

