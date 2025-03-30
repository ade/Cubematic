package se.ade.mc.cubematic.progression.analysis.key

import org.bukkit.Material
import org.bukkit.potion.PotionType

sealed interface NodeKey {
	abstract val id: String
	abstract val description: String

	data class Item(val material: Material, val tags: Set<ItemTag> = setOf()): NodeKey {
		override val id: String = material.key.toString()
		override val description: String = "Item($material)"
		override fun toString(): String {
			if(tags.isEmpty()) {
				return "$material"
			} else {
				return "($material, tags=${tags.joinToString(", ")})"
			}
		}
	}

	data class Custom(val key: String, override val description: String): NodeKey {
		override val id: String = key
		override fun toString() = "[$description]"
	}
}

sealed interface ItemTag {
	data class Potion(val type: PotionType): ItemTag
}