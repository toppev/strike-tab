import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlin_version: String by project

plugins {
    kotlin("jvm") version "1.4.20"
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

group = "ga.strikepractice.striketab"
version = "0.3.13-SNAPSHOT"

repositories {
    jcenter()
    mavenCentral()
    maven { url = uri("https://repo.viaversion.com") }
    maven { url = uri("https://maven.toppe.dev/repo") }
    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
    maven { url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/") }
    maven { url = uri("https://maven.citizensnpcs.co/repo") }
    flatDir { dirs("libs") }
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://repo.dmulloy2.net/nexus/repository/public/") }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version")
    testImplementation(kotlin("test-junit"))
    testImplementation("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")
    compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")
    compileOnly("ga.strikepractice:strikepractice-api")
    compileOnly("me.clip:placeholderapi:2.11.3")
    compileOnly("net.citizensnpcs:citizensapi:2.0.30-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.1.0")
    compileOnly("com.viaversion:viaversion-api:5.0.0")
    // If tabbed is updated, might need to trigger a new build on jitpack:
    // https://jitpack.io/#toppev/tabbed/master-SNAPSHOT
    implementation("com.github.toppev:tabbed:master-SNAPSHOT")
    implementation("org.bstats:bstats-bukkit:2.2.1")
}

configurations.all {
    // Don't try to update the tabbed fork from GitHub with jitpack every time (to reduce compilation time in subsequent builds)
    resolutionStrategy.cacheChangingModulesFor(10, "minutes")
}

tasks {
    // Download the jar so we don't need any NMS dependencies (there's no API repository)
    register("download-viaversion") {
        val dest = "./libs/viaversion.jar"
        val source = "https://repo.viaversion.com/everything/com/viaversion/viaversion/5.0.0/viaversion-5.0.0.jar"
        if (!File(dest).exists()) {
            download(source, dest)
        }
    }
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("StrikeTab")
        relocate("org.bstats", "ga.strikepractice.striketab.bstats")
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

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

fun download(url: String, path: String) {
    val destFile = File(path)
    ant.invokeMethod("get", mapOf("src" to url, "dest" to destFile))
}
