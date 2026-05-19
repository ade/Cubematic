package se.ade.mc.cubematic.core.agent.wiki

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue

@Serializable
data class ExportedPageXml(
	@XmlElement @XmlSerialName("siteinfo") val siteInfo: SiteInfo,
	@XmlElement @XmlSerialName("page") val page: List<Page>,
	val version: String,
) {
	@Serializable
	data class SiteInfo(
		@XmlElement val sitename: String,
		@XmlElement val base: String,
		@XmlElement val generator: String,
		@XmlElement val case: String,
		@XmlElement @XmlSerialName("namespaces") val namespaces: NamespaceList,
	)

	@Serializable
	data class NamespaceList(
		@XmlElement @XmlSerialName("namespace") val namespace: List<Namespace>
	)

	@Serializable
	data class Namespace(
		@XmlElement(false) val key: Int,
		@XmlElement(false) val case: String,
		@XmlValue val text: String
	)

	@Serializable
	data class Page(
		@XmlElement val title: String,
		@XmlElement val ns: Int,
		@XmlElement val id: Int,
		@XmlElement @XmlSerialName("revision") val revision: Revision
	)

	@Serializable
	data class Revision(
		@XmlElement val id: Int,
		@XmlElement val timestamp: String,
		@XmlElement val comment: String? = null,
		@XmlElement @XmlSerialName("text") val text: Text
	)

	@Serializable
	data class Text(
		@XmlValue val content: String
	)
}