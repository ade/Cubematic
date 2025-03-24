package se.ade.mc.cubematic.dreams.inventory

data class PlayerHibernation(
	val uuid: String,
	val inventory: ByteArray,
	val armor: ByteArray
) {
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as PlayerHibernation

		if (uuid != other.uuid) return false
		if (!inventory.contentEquals(other.inventory)) return false
		if (!armor.contentEquals(other.armor)) return false

		return true
	}

	override fun hashCode(): Int {
		var result = uuid.hashCode()
		result = 31 * result + inventory.contentHashCode()
		result = 31 * result + armor.contentHashCode()
		return result
	}
}