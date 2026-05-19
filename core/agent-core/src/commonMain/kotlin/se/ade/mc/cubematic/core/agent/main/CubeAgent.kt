package se.ade.mc.cubematic.core.agent.main

import se.ade.mc.cubematic.core.agent.config.InferenceProvider

class CubeAgent(val inferenceProvider: InferenceProvider) {
	fun createConvo(): CubeAgentConvo {
		return CubeAgentConvo(inferenceProvider)
	}
}