import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.compose)
	alias(libs.plugins.kotlin.compose)
	alias(libs.plugins.kotlinx.serialization)
}

kotlin {
	jvmToolchain(21)

	/*
	compilerOptions {
		freeCompilerArgs.add("-opt-in=kotlin.time.ExperimentalTime")
	}
	 */
}

group = "se.ade.mc.cubematic.agent.ui"
version = "1.0-SNAPSHOT"

dependencies {
	implementation(project(":agent:agent-core"))
	implementation(libs.bundles.plugin.runtime)

	// Note, if you develop a library, you should use compose.desktop.common.
	// compose.desktop.currentOs should be used in launcher-sourceSet
	// (in a separate module for demo project and in testMain).
	// With compose.desktop.common you will also lose @Preview functionality
	implementation(compose.desktop.currentOs)
	implementation(libs.kotlinx.coroutines.core)
	implementation(libs.kotlinx.coroutines.swing)
	implementation(libs.kotlinx.datetime)
	implementation(libs.compose.viewmodel)
	implementation(libs.koog)
	implementation(libs.compose.material3)
}

compose.desktop {
	application {
		mainClass = "se.ade.mc.cubematic.agent.ui.MainKt"

		nativeDistributions {
			targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
			packageName = "ade-llm-test"
			packageVersion = "1.0.0"
		}
	}
}