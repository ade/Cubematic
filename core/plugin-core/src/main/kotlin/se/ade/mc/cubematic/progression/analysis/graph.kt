package se.ade.mc.cubematic.progression.analysis

import org.bukkit.Material

val graph = buildGraph {
	item(Material.CRAFTING_TABLE) {
		from {
			craftByHand(4 of anyPlanks())
		}
	}
	item(Material.WOODEN_PICKAXE) {
		from {
			crafting(2 of Material.STICK, 3 of anyPlanks())
		}
	}
	item(Material.BIRCH_PLANKS) {
		from {
			craftByHand(1 of Material.BIRCH_LOG)
		}
	}
	item(Material.OAK_PLANKS) {
		from {
			craftByHand(1 of Material.OAK_LOG)
		}
	}
	item(Material.OAK_LOG) {
		from {
			grow(plant = Material.OAK_SAPLING,
				on = saplingSoils())
		}
	}
	item(Material.BIRCH_LOG) {
		from {
			grow(plant = Material.BIRCH_SAPLING,
				on = saplingSoils())
		}
	}
	item(Material.STONE) {
		from {
			smelting(Material.COBBLESTONE, with = furnace(), yield = exactly(1))
		}
	}
	item(Material.SMOOTH_STONE) {
		from {
			smelting(Material.STONE, with = furnace(), yield = exactly(1))
		}
	}
	item(Material.FURNACE) {
		from {
			crafting(8 of Material.COBBLESTONE)
		}
	}
	item(Material.CHARCOAL) {
		from {
			smelting(Material.BIRCH_LOG, with = furnace(), yield = exactly(1))
		}
	}
	item(Material.BUCKET) {
		from {
			crafting(3 of Material.IRON_INGOT)
		}
	}
	item(Material.EMERALD) {
		from {
			villagerTrading(anyOf(
				Material.IRON_INGOT,
				Material.PAPER
				// TODO: Lots of stuff
			))
		}
	}
	mechanic(MechanicType.VILLAGER_TRADING) {
		from {
			having(
				1 of Material.GOLDEN_APPLE
			)
		}
	}
	item(Material.GOLDEN_APPLE) {
		from {
			crafting(8 of Material.GOLD_NUGGET, 1 of Material.APPLE)
		}
	}
	item(Material.GOLD_INGOT) {
		from {
			smelting(Material.GOLD_ORE, with = furnace(), yield = exactly(1))
		}
		from {
			crafting(9 of Material.GOLD_NUGGET)
		}
	}
	item(Material.APPLE) {
		from {
			grow(plant = Material.OAK_SAPLING,
				on = saplingSoils())
		}
	}
	item(Material.OAK_SAPLING) {
		from {
			grow(plant = Material.OAK_SAPLING,
				on = saplingSoils())
		}
		from {
			having(1 of Material.OAK_LEAVES)
		}
	}
	item(Material.GOLD_NUGGET) {
		from {
			craftByHand(1 of Material.GOLD_INGOT)
		}
		from {
			smelting(Material.GOLDEN_BOOTS, with = furnace(), yield = exactly(1))
		}
		from {
			smelting(Material.GOLDEN_CHESTPLATE, with = furnace(), yield = exactly(1))
		}
		from {
			smelting(Material.GOLDEN_HELMET, with = furnace(), yield = exactly(1))
		}
		from {
			smelting(Material.GOLDEN_LEGGINGS, with = furnace(), yield = exactly(1))
		}
		from {
			smelting(Material.GOLDEN_PICKAXE, with = furnace(), yield = exactly(1))
		}
		from {
			smelting(Material.GOLDEN_SHOVEL, with = furnace(), yield = exactly(1))
		}
		from {
			smelting(Material.GOLDEN_SWORD, with = furnace(), yield = exactly(1))
		}
		from {
			smelting(Material.GOLDEN_AXE, with = furnace(), yield = exactly(1))
		}
		from {
			smelting(Material.GOLDEN_HOE, with = furnace(), yield = exactly(1))
		}
	}
}