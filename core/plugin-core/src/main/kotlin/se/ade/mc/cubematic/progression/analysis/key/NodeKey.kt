package se.ade.mc.cubematic.progression.analysis.key

import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.potion.PotionType

sealed interface NodeKey {
	val id: String
	val description: String

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

	data class Entity(val entityType: EntityType): NodeKey {
		override val id: String = entityType.key.toString()
		override val description: String = "Entity($entityType)"
		override fun toString() = "[$description]"
	}

	data class Custom(val key: String, override val description: String): NodeKey {
		override val id: String = key
		override fun toString() = "[$description]"
	}
}

sealed interface ItemTag {
	data class Potion(val type: PotionType): ItemTag
}