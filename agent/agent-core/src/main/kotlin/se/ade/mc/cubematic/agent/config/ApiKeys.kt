package se.ade.mc.cubematic.agent.config

object ApiKeys {
	val workstation: String by lazy {
		System.getenv("WORKSTATION_KEY")
	}
	val googleAiStudio: String by lazy {
		System.getenv("GOOGLE_KEY")
	}
	val openRouter: String by lazy {
		System.getenv("OPENROUTER_KEY")
	}
}