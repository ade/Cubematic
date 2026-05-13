package se.ade.mc.cubematic.core.agent.wiki

class WikiParser {
	val removeSections = listOf(
		"Achievements",
		"Advancements",
		"Data values",
		"Video",
		"Sounds",
		"History",
		"Issues",
		"Trivia",
		"Gallery",
		"See also",
		"Notes",
		"References",
		"External links",
		"Navigation",
		"Data history",
		"Mojang screenshots",
		"Screenshots",
		"In other media"
	)
	fun pruneSections(pageText: String): String {
		var text = pageText
		removeSections.forEach { section ->
			val regex = Regex("==\\s*$section\\s*==[\\s\\S]*?(?=(==|$))", RegexOption.IGNORE_CASE)
			text = text.replace(regex, "")
		}
		return text.trim()
	}
}