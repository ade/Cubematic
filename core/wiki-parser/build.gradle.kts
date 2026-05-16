plugins {
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.kotlinx.serialization)
}

kotlin {
	jvmToolchain(21)
}

group = "se.ade.mc.wikimd"
version = "1.0-SNAPSHOT"

dependencies {
	implementation("org.sweble.wikitext:swc-parser-lazy:3.1.9")
	implementation("org.sweble.wikitext:swc-engine:3.1.9")
	implementation("javax.xml.bind:jaxb-api:2.3.1")
}