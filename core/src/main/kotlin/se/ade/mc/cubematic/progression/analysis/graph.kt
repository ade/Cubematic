package se.ade.mc.cubematic.progression.analysis

import org.bukkit.Material

val graph = buildGraph {
	item(Material.CRAFTING_TABLE) {
		sources {
			craftByHand(4 of anyPlanks())
		}
	}
	item(Material.WOODEN_PICKAXE) {
		sources {
			crafting(2 of Material.STICK, 3 of anyPlanks())
		}
	}
	item(Material.BIRCH_PLANKS) {
		sources {
			craftByHand(1 of Material.BIRCH_LOG)
		}
	}
	item(Material.BIRCH_LOG) {
		sources {
			grow(plant = Material.BIRCH_SAPLING,
				on = saplingSoils(),
				yield = ProcessYield.Random(5, 7))
		}
	}
	item(Material.STONE) {
		sources {
			smelting(Material.COBBLESTONE, with = furnace(), yield = exactly(1))
		}
	}
	item(Material.SMOOTH_STONE) {
		sources {
			smelting(Material.STONE, with = furnace(), yield = exactly(1))
		}
	}
	item(Material.FURNACE) {
		sources {
			crafting(8 of Material.COBBLESTONE)
		}
	}
	item(Material.CHARCOAL) {
		sources {
			smelting(Material.BIRCH_LOG, with = furnace(), yield = exactly(1))
		}
	}
	item(Material.BUCKET) {
		sources {
			crafting(3 of Material.IRON_INGOT)
		}
	}

}