package se.ade.mc.cubematic.agent.main

import se.ade.mc.cubematic.agent.config.InferenceProvider

class CubeAgent(val inferenceProvider: InferenceProvider) {
	fun createConvo(): CubeAgentConvo {
		return CubeAgentConvo(inferenceProvider)
	}
}