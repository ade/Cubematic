package se.ade.mc.cubematic.agent.ui

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
	val windowState = rememberWindowState()
	windowState.position = WindowPosition.Aligned(Alignment.Center)
	windowState.size = DpSize(1200.dp, 800.dp)

	Window(onCloseRequest = ::exitApplication, state = windowState) {
		App()
	}
}