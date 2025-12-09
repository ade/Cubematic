package se.ade.mc.cubematic.agent.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AgentUi() {
	val viewModel = viewModel { AgentVm() }
	val state by viewModel.state.collectAsState()
	val focusRequester = remember { FocusRequester() }

	var text by remember { mutableStateOf("Describe the full process of making an ender chest") }
	Column(modifier = Modifier.fillMaxSize()) {
		AnimatedVisibility(visible = state.process != null) {
			Column(modifier = Modifier
				.fillMaxWidth()
				.clip(shape = RoundedCornerShape(8.dp))
				.border(2.dp, MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(8.dp))
				.background(MaterialTheme.colorScheme.primaryContainer)
				.padding(10.dp)
			) {
				state.process?.entries?.forEach { entry ->
					Row(verticalAlignment = Alignment.CenterVertically) {
						if (entry.done) Text("✓") else CircularProgressIndicator(modifier = Modifier.size(16.dp))
						Spacer(Modifier.width(8.dp))
						Text(text = entry.text)
					}
				}
			}
			Spacer(Modifier.height(10.dp))
		}
		Text(modifier = Modifier.weight(1f), text = state.text)
		HorizontalDivider()
		Row {
			TextField(
				modifier = Modifier.weight(1f)
					.focusRequester(focusRequester)
					.onPreviewKeyEvent { keyEvent ->
						if (keyEvent.key == Key.Enter && keyEvent.type == KeyEventType.KeyDown) {
							viewModel.submit(text)
							text = ""
							true
						} else {
							false
						}
					},
				onValueChange = { text = it }, value = text)
			Button(onClick = {
				viewModel.submit(text)
				text = ""
			}) {
				Text(">")
			}
		}
	}

	LaunchedEffect(Unit) {
		focusRequester.requestFocus()
	}
}