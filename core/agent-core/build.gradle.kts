import org.jetbrains.kotlin.gradle.ExternalKotlinTargetApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import kotlin.jvm.java

plugins {
	alias(libs.plugins.kotlin.multiplatform)
	alias(libs.plugins.android.library)
	alias(libs.plugins.ksp)
	alias(libs.plugins.kotlinx.serialization)
}

kotlin {
	androidTarget()
	jvm()

	sourceSets {
		val commonMain by getting {
			dependencies {
				implementation(libs.koog)
				implementation(libs.kotlinx.coroutines.core)
				implementation(libs.ktor.client.core)
				implementation(libs.ktor.client.content.negotiation)
				implementation(libs.ktor.serialization.kotlinx.json)
				//implementation(libs.ktor.serialization.kotlinx.xml)
				implementation(libs.se.ade.kuri.api)
				implementation(libs.xmlutil.core)
				implementation(libs.xmlutil.serialization)
				implementation("org.slf4j:slf4j-simple:2.0.16")
			}
		}
		val jvmMain by getting
		val androidMain by getting
	}

	configureCommonMainKsp()
}

dependencies {
	// Run KSP on [commonMain] code
	add("kspCommonMainMetadata", libs.se.ade.kuri.processor)
}

@OptIn(ExternalKotlinTargetApi::class)
fun KotlinMultiplatformExtension.configureCommonMainKsp() {
	sourceSets.named("commonMain").configure {
		kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
	}

	project.tasks.withType(KotlinCompilationTask::class.java).configureEach {
		if(name != "kspCommonMainKotlinMetadata") {
			dependsOn("kspCommonMainKotlinMetadata")
		}
	}

	project.afterEvaluate {
		val sourceTasks = listOf(
			"sourcesJar", "jvmSourcesJar", "androidReleaseSourcesJar", "iosArm64SourcesJar", "iosSimulatorArm64SourcesJar",
			"watchosArm64SourcesJar", "watchosDeviceArm64SourcesJar", "watchosSimulatorArm64SourcesJar"
		)
		sourceTasks.forEach {
			tasks.findByName(it)?.dependsOn("kspCommonMainKotlinMetadata")
		}
	}
}

android {
	namespace = "se.ade.mc.cubematic.core"
	compileSdk = 36
	defaultConfig {
		minSdk = 26
	}
}