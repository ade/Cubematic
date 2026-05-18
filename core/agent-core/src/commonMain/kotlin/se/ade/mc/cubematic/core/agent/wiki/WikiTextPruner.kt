package se.ade.mc.cubematic.core.agent.wiki

private val removeSections = listOf(
	"Achievements",
	"Advancements",
	"Data values",
	"Video",
	"Videos",
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

object WikiTextPruner {
	fun prune(text: String): String {
		return removeSections(removeLangLinks(removeGallery(text)))
	}

	private fun removeSections(pageText: String): String {
		var text = pageText
		removeSections.forEach { section ->
			val regex = Regex("==\\s*$section\\s*==[\\s\\S]*?(?=(==|$))", RegexOption.IGNORE_CASE)
			text = text.replace(regex, "")
		}

		return text.trim()
	}

	/**
	 * Removes interlanguage links from the given wiki text.
	 * Interlanguage links are in the format [[xx:PageName]], where xx is a language code.
	 */
	private fun removeLangLinks(text: String): String {
		var prunedText = text
		val langLinkPattern = Regex("\\[\\[[a-z]{2}:[^\\]]+\\]\\]", RegexOption.IGNORE_CASE)

		prunedText = prunedText.replace(langLinkPattern, "")

		return prunedText
	}

	/**
	 * Removes <gallery> tags and their content from the given wiki text.
	 */
	private fun removeGallery(text: String): String {
		var prunedText = text
		val galleryTagStart = "<gallery"
		val galleryTagEnd = "</gallery>"

		while (true) {
			val startIndex = prunedText.indexOf(galleryTagStart, ignoreCase = true)
			if (startIndex == -1) break

			val endIndex = prunedText.indexOf(galleryTagEnd, startIndex, ignoreCase = true)
			if (endIndex == -1) {
				// If there's no closing tag, remove everything from the start tag to the end of the text
				prunedText = prunedText.substring(0, startIndex)
				break
			} else {
				// Remove the entire gallery tag and its content
				prunedText = prunedText.removeRange(startIndex, endIndex + galleryTagEnd.length)
			}
		}

		return prunedText
	}
}