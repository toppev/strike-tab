import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlin_version: String by project

plugins {
    kotlin("jvm") version "1.4.20"
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

group = "ga.strikepractice.striketab"
version = "0.3.5-SNAPSHOT"

repositories {
    jcenter()
    mavenCentral()
    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
    maven { url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/") }
    maven { url = uri("https://repo.citizensnpcs.co/") }
    flatDir { dirs("libs") }
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://repo.dmulloy2.net/nexus/repository/public/") }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version")
    testImplementation(kotlin("test-junit"))
    compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")
    compileOnly("ga.strikepractice:strikepractice-api-1.1.0")
    compileOnly("me.clip:placeholderapi:2.10.4")
    compileOnly("net.citizensnpcs:citizens:2.0.13-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.5.0")
    implementation("com.github.toppev:tabbed:master-SNAPSHOT")
    compileOnly("us.myles:viaversion:viaversion")
}

configurations.all {
    resolutionStrategy.cacheChangingModulesFor(10, "minutes")
}

tasks {
    // Download the jar so we don't need any NMS dependencies (there's no API repository)
    register("download-viaversion") {
        val dest = "./libs/viaversion.jar"
        val source = "https://repo.viaversion.com/everything/us/myles/viaversion/3.2.1/viaversion-3.2.1.jar"
        if (!File(dest).exists()) download(source, dest)
    }
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("StrikeTab")
        mergeServiceFiles()
    }
}

tasks {
    compileKotlin {
        dependsOn("download-viaversion")
    }
    build {
        dependsOn(shadowJar)
    }
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

fun download(url: String, path: String) {
    val destFile = File(path)
    ant.invokeMethod("get", mapOf("src" to url, "dest" to destFile))
}
