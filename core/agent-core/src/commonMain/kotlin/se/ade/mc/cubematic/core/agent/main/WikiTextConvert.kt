package se.ade.mc.cubematic.core.agent.main

expect object WikiTextConvert {
	fun optimizeForAgent(pageTitle: String, pageContent: String): String
}