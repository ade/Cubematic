import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	id("net.fabricmc.fabric-loom")
	`maven-publish`
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.kotlinx.serialization)
}

loom {
	splitEnvironmentSourceSets()
	//serverOnlyMinecraftJar()

	mods {
		register("cubematic-agent") {
			sourceSet(sourceSets.main.get())
		}
	}
}

// Libraries that are not Fabric mods but need to be bundled into (and resolvable from) the
// final mod jar. Kotlin/kotlinx artifacts are intentionally excluded as they are provided by
// fabric-language-kotlin at runtime.
val bundleRuntime: Configuration by configurations.creating {
	isCanBeResolved = true
	isCanBeConsumed = false
}

// Make the bundled libraries available on the dev compile/runtime classpaths.
configurations.implementation.configure { extendsFrom(bundleRuntime) }

dependencies {
	// To change the versions see the gradle.properties file
	minecraft("com.mojang:minecraft:${providers.gradleProperty("fabric_minecraft_version").get()}")
	
	implementation("net.fabricmc:fabric-loader:${providers.gradleProperty("loader_version").get()}")

	// Fabric API. This is technically optional, but you probably want it anyway.
	implementation("net.fabricmc.fabric-api:fabric-api:${providers.gradleProperty("fabric_api_version").get()}")
	implementation("net.fabricmc:fabric-language-kotlin:${providers.gradleProperty("fabric_kotlin_version").get()}")



	// Shared agent logic. Its (implementation-scoped) transitive runtime deps are present on the
	// dev runtime classpath automatically.
	implementation(project(":core:agent-core"))

	// Third-party libraries required at runtime by the agent. Declared in a dedicated configuration
	// so they can both be compiled/run against in dev and bundled (JiJ) into the published mod jar.
	bundleRuntime(libs.kaml)
	bundleRuntime(libs.koog)
	bundleRuntime(libs.ktor.client.core)
	bundleRuntime(libs.ktor.client.content.negotiation)
	bundleRuntime(libs.ktor.serialization.kotlinx.json)
	bundleRuntime(libs.se.ade.kuri.api)
	bundleRuntime(libs.xmlutil.core)
	bundleRuntime(libs.xmlutil.serialization)

	// Nest the local Cubematic projects directly into the mod jar.
	include(project(":core:agent-core"))
	include(project(":core:wiki-parser"))
}

// Bundle (jar-in-jar) the external agent runtime libraries and their transitive dependencies into
// the final mod jar, skipping anything already provided by Minecraft / Fabric / Kotlin.
afterEvaluate {
	// Groups that are always provided by the platform and must never be bundled.
	val excludedGroups = setOf(
		"org.jetbrains.kotlin", // stdlib + reflect, shipped by fabric-language-kotlin
		"net.fabricmc",
		"net.minecraft",
		"com.mojang",
	)

	// The ONLY org.jetbrains.kotlinx modules that fabric-language-kotlin actually ships.
	// Anything else under that group (e.g. koog's kotlinx-schema-* schema generator, which
	// SchemaGeneratorKt initializes statically) is NOT provided at runtime and MUST be bundled,
	// otherwise tool/schema generation fails with:
	//   NoClassDefFoundError: Could not initialize class ...SchemaGeneratorKt
	val kotlinxProvidedByFlk = setOf(
		"atomicfu",
		"kotlinx-coroutines-core",
		"kotlinx-coroutines-jdk8",
		"kotlinx-datetime",
		"kotlinx-io-bytestring",
		"kotlinx-io-core",
		"kotlinx-serialization-cbor",
		"kotlinx-serialization-core",
		"kotlinx-serialization-json",
	)

	fun isProvided(group: String, name: String): Boolean {
		if (group in excludedGroups) return true
		if (group == "org.jetbrains.kotlinx") {
			// Match both the common metadata name and the resolved `-jvm` variant.
			return kotlinxProvidedByFlk.any { name == it || name == "$it-jvm" }
		}
		return false
	}

	bundleRuntime.resolvedConfiguration.resolvedArtifacts
		.map { it.moduleVersion.id }
		.filterNot { isProvided(it.group, it.name) }
		.map { "${it.group}:${it.name}:${it.version}" }
		.distinct()
		.forEach { coordinate -> dependencies.add("include", coordinate) }
}

tasks.processResources {
	val version = version
	inputs.property("version", version)

	filesMatching("fabric.mod.json") {
		expand("version" to version)
	}
}

tasks.withType<JavaCompile>().configureEach {
	options.release = 25
}

kotlin {
	compilerOptions {
		jvmTarget = JvmTarget.JVM_25
	}
	jvmToolchain(25)
}

java {
	// Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
	// if it is present.
	// If you remove this line, sources will not be generated.
	withSourcesJar()

	sourceCompatibility = JavaVersion.VERSION_25
	targetCompatibility = JavaVersion.VERSION_25
}

tasks.jar {
	archiveBaseName = "cubematic-agent-fabric"
	val projectName = project.name
	inputs.property("projectName", projectName)

	from("LICENSE") {
		rename { "${it}_$projectName" }
	}
}

// configure the maven publication
publishing {
	publications {
		register<MavenPublication>("mavenJava") {
			artifactId = "cubematic-agent-fabric"
			from(components["java"])
		}
	}

	// See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
	repositories {
		// Add repositories to publish to here.
		// Notice: This block does NOT have the same function as the block in the top level.
		// The repositories here will be used for publishing your artifact, not for
		// retrieving dependencies.
	}
}
