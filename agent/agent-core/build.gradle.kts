plugins {
	kotlin("jvm")
	alias(libs.plugins.kotlinx.serialization)
	alias(libs.plugins.ksp)
}

dependencies {
	compileOnly(kotlin("stdlib"))
	//compileOnly(libs.paper)
	compileOnly(libs.kaml)

	compileOnly(libs.koog)
	compileOnly(libs.kotlinx.coroutines.core)
	compileOnly(libs.kotlinx.datetime)
	compileOnly(libs.ktor.client.core)
	compileOnly(libs.ktor.client.content.negotiation)
	compileOnly(libs.ktor.serialization.kotlinx.json)

	compileOnly(libs.se.ade.kuri.api)
	ksp(libs.se.ade.kuri.processor)
}

kotlin {
	jvmToolchain(21)
}