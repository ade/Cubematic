package se.ade.mc.cubematic.progression.analysis

import org.bukkit.Material

fun DependencyGraphBuilderScope.villagerTradingRules() {
	armorerTrades()
	butcherTrades()
	cartographerTrades()
	clericTrades()
	farmerTrades()
	fisherTrades()
	fletcherTrades()
	leatherworkerTrades()
	librarianTrades()
	masonTrades()
	shepherdTrades()
	toolsmithTrades()
	weaponsmithTrades()
}

private fun DependencyGraphBuilderScope.armorerTrades() {
	trade(
		"Trading with Armorer",
		Material.EMERALD,
		Material.IRON_HELMET,
		Material.IRON_CHESTPLATE,
		Material.IRON_LEGGINGS,
		Material.IRON_BOOTS,
		Material.DIAMOND_HELMET,
		Material.DIAMOND_CHESTPLATE,
		Material.DIAMOND_LEGGINGS,
		Material.DIAMOND_BOOTS,
		Material.BELL,
		Material.CHAINMAIL_LEGGINGS,
		Material.CHAINMAIL_BOOTS,
		Material.CHAINMAIL_HELMET,
		Material.CHAINMAIL_CHESTPLATE,
		Material.SHIELD
	)
}

private fun DependencyGraphBuilderScope.butcherTrades() {
	trade(
		"Trading with Butcher",
		Material.EMERALD,
		Material.COOKED_CHICKEN,
		Material.COOKED_PORKCHOP,
		Material.RABBIT_STEW
	)
}

private fun DependencyGraphBuilderScope.cartographerTrades() {
	trade("Trading with Cartographer",
		Material.EMERALD,
		Material.MAP,
		Material.ITEM_FRAME,
		Material.BLACK_BANNER,
		Material.BLUE_BANNER,
		Material.BROWN_BANNER,
		Material.CYAN_BANNER,
		Material.GRAY_BANNER,
		Material.GREEN_BANNER,
		Material.LIGHT_BLUE_BANNER,
		Material.LIGHT_GRAY_BANNER,
		Material.LIME_BANNER,
		Material.MAGENTA_BANNER,
		Material.ORANGE_BANNER,
		Material.PINK_BANNER,
		Material.PURPLE_BANNER,
		Material.RED_BANNER,
		Material.WHITE_BANNER,
		Material.YELLOW_BANNER,
		Material.GLOBE_BANNER_PATTERN
	)
}

private fun DependencyGraphBuilderScope.clericTrades() {
	trade("Trading with Cleric",
		Material.EMERALD,
		Material.REDSTONE,
		Material.LAPIS_LAZULI,
		Material.GLOWSTONE,
		Material.ENDER_PEARL,
		Material.EXPERIENCE_BOTTLE
	)
}

private fun DependencyGraphBuilderScope.farmerTrades() {
	trade("Trading with Farmer",
		Material.EMERALD,
		Material.BREAD,
		Material.CAKE,
		Material.COOKIE,
		Material.PUMPKIN_PIE,
		Material.GOLDEN_CARROT,
		Material.APPLE,
		Material.GLISTERING_MELON_SLICE
	)
}

private fun DependencyGraphBuilderScope.fisherTrades() {
	trade("Trading with Fisher",
		Material.EMERALD,
		Material.COD_BUCKET,
		Material.COOKED_COD,
		Material.COOKED_SALMON,
		Material.FISHING_ROD,
		Material.CAMPFIRE
	)
}

private fun DependencyGraphBuilderScope.fletcherTrades() {
	trade("Trading with Fletcher",
		Material.EMERALD,
		Material.ARROW,
		Material.BOW,
		Material.CROSSBOW,
		Material.FLINT
	)
}

private fun DependencyGraphBuilderScope.leatherworkerTrades() {
	trade("Trading with Leatherworker",
		Material.EMERALD,
		Material.LEATHER_HELMET,
		Material.LEATHER_CHESTPLATE,
		Material.LEATHER_LEGGINGS,
		Material.LEATHER_BOOTS,
		Material.LEATHER_HORSE_ARMOR,
		Material.SADDLE
	)
}

private fun DependencyGraphBuilderScope.librarianTrades() {
	trade("Trading with Librarian",
		Material.EMERALD,
		Material.BOOKSHELF,
		Material.ENCHANTED_BOOK,
		Material.GLASS,
		Material.NAME_TAG,
		Material.LANTERN,
		Material.COMPASS,
		Material.CLOCK,
	)
}

private fun DependencyGraphBuilderScope.masonTrades() {
	trade(
		"Trading with Mason",
		Material.EMERALD,
		Material.BRICK,
		Material.CHISELED_STONE_BRICKS,
		Material.DRIPSTONE_BLOCK,
		Material.POLISHED_ANDESITE,
		Material.POLISHED_DIORITE,
		Material.POLISHED_GRANITE,
		Material.QUARTZ_PILLAR,
		Material.QUARTZ_BLOCK,

		Material.BLACK_TERRACOTTA,
		Material.BLUE_TERRACOTTA,
		Material.BROWN_TERRACOTTA,
		Material.CYAN_TERRACOTTA,
		Material.GRAY_TERRACOTTA,
		Material.GREEN_TERRACOTTA,
		Material.LIGHT_BLUE_TERRACOTTA,
		Material.LIGHT_GRAY_TERRACOTTA,
		Material.LIME_TERRACOTTA,
		Material.MAGENTA_TERRACOTTA,
		Material.ORANGE_TERRACOTTA,
		Material.PINK_TERRACOTTA,
		Material.PURPLE_TERRACOTTA,
		Material.RED_TERRACOTTA,
		Material.WHITE_TERRACOTTA,
		Material.YELLOW_TERRACOTTA,

		Material.BLACK_GLAZED_TERRACOTTA,
		Material.BLUE_GLAZED_TERRACOTTA,
		Material.BROWN_GLAZED_TERRACOTTA,
		Material.CYAN_GLAZED_TERRACOTTA,
		Material.GRAY_GLAZED_TERRACOTTA,
		Material.GREEN_GLAZED_TERRACOTTA,
		Material.LIGHT_BLUE_GLAZED_TERRACOTTA,
		Material.LIGHT_GRAY_GLAZED_TERRACOTTA,
		Material.LIME_GLAZED_TERRACOTTA,
		Material.MAGENTA_GLAZED_TERRACOTTA,
		Material.ORANGE_GLAZED_TERRACOTTA,
		Material.PINK_GLAZED_TERRACOTTA,
		Material.PURPLE_GLAZED_TERRACOTTA,
		Material.RED_GLAZED_TERRACOTTA,
		Material.WHITE_GLAZED_TERRACOTTA,
		Material.YELLOW_GLAZED_TERRACOTTA,
	)
}

private fun DependencyGraphBuilderScope.shepherdTrades() {
	trade("Trading with Shepherd",
		Material.EMERALD,
		Material.SHEARS,
		Material.PAINTING,

		Material.BLACK_WOOL,
		Material.BLUE_WOOL,
		Material.BROWN_WOOL,
		Material.CYAN_WOOL,
		Material.GRAY_WOOL,
		Material.GREEN_WOOL,
		Material.LIGHT_BLUE_WOOL,
		Material.LIGHT_GRAY_WOOL,
		Material.LIME_WOOL,
		Material.MAGENTA_WOOL,
		Material.ORANGE_WOOL,
		Material.PINK_WOOL,
		Material.PURPLE_WOOL,
		Material.RED_WOOL,
		Material.WHITE_WOOL,
		Material.YELLOW_WOOL,

		Material.BLACK_CARPET,
		Material.BLUE_CARPET,
		Material.BROWN_CARPET,
		Material.CYAN_CARPET,
		Material.GRAY_CARPET,
		Material.GREEN_CARPET,
		Material.LIGHT_BLUE_CARPET,
		Material.LIGHT_GRAY_CARPET,
		Material.LIME_CARPET,
		Material.MAGENTA_CARPET,
		Material.ORANGE_CARPET,
		Material.PINK_CARPET,
		Material.PURPLE_CARPET,
		Material.RED_CARPET,
		Material.WHITE_CARPET,
		Material.YELLOW_CARPET,

		Material.BLACK_BED,
		Material.BLUE_BED,
		Material.BROWN_BED,
		Material.CYAN_BED,
		Material.GRAY_BED,
		Material.GREEN_BED,
		Material.LIGHT_BLUE_BED,
		Material.LIGHT_GRAY_BED,
		Material.LIME_BED,
		Material.MAGENTA_BED,
		Material.ORANGE_BED,
		Material.PINK_BED,
		Material.PURPLE_BED,
		Material.RED_BED,
		Material.WHITE_BED,
		Material.YELLOW_BED,

		Material.BLACK_BANNER,
		Material.BLUE_BANNER,
		Material.BROWN_BANNER,
		Material.CYAN_BANNER,
		Material.GRAY_BANNER,
		Material.GREEN_BANNER,
		Material.LIGHT_BLUE_BANNER,
		Material.LIGHT_GRAY_BANNER,
		Material.LIME_BANNER,
		Material.MAGENTA_BANNER,
		Material.ORANGE_BANNER,
		Material.PINK_BANNER,
		Material.PURPLE_BANNER,
		Material.RED_BANNER,
		Material.WHITE_BANNER,
		Material.YELLOW_BANNER,
	)
}

private fun DependencyGraphBuilderScope.toolsmithTrades() {
	trade("Trading with Toolsmith",
		Material.EMERALD,
		Material.BELL,

		Material.STONE_AXE,
		Material.STONE_PICKAXE,
		Material.STONE_SHOVEL,
		Material.STONE_HOE,

		Material.IRON_AXE,
		Material.IRON_SHOVEL,
		Material.IRON_PICKAXE,
		Material.IRON_HOE,

		Material.DIAMOND_PICKAXE,
		Material.DIAMOND_AXE,
		Material.DIAMOND_SHOVEL,
		Material.DIAMOND_HOE
	)
}

private fun DependencyGraphBuilderScope.weaponsmithTrades() {
	trade(
		"Trading with Weaponsmith",
		Material.EMERALD,
		Material.BELL,

		Material.IRON_SWORD,
		Material.IRON_AXE,

		Material.DIAMOND_SWORD,
		Material.DIAMOND_AXE
	)
}

private fun DependencyGraphBuilderScope.trade(description: String, vararg items: Material) {
	items.forEach {
		item(it) {
			from(description) {
				villagerTrading()
			}
		}
	}
}
