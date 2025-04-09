package se.ade.mc.cubematic.progression.analysis

import org.bukkit.Material

fun DependencyGraphBuilderScope.villagerTradingRules() {

}

private fun DependencyGraphBuilderScope.blacksmithTradeTable() {
	item(Material.DIAMOND_PICKAXE) {
		from {
			villagerTrading()
		}
	}
}