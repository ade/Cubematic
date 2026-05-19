

plugins {
	alias(libs.plugins.kotlin.jvm)
	`java-library`
	alias(libs.plugins.ksp)
	alias(libs.plugins.kotlinx.serialization)
}

kotlin {
	compilerOptions {
		jvmToolchain(21)
	}
}

dependencies {
	implementation(project(":core:wiki-parser"))

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

	ksp(libs.se.ade.kuri.processor)
}
