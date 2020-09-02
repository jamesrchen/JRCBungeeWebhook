import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import kr.entree.spigradle.kotlin.bungeecord
import kr.entree.spigradle.kotlin.sonatype

plugins {
    kotlin("jvm") version "1.4.0"
    id("com.github.johnrengelman.shadow") version "4.0.4"
    id("kr.entree.spigradle.bungee") version "2.2.0"
}

group = "ninja.jrc"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
    maven("https://jitpack.io")
    sonatype()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.github.jkcclemens:khttp:0.1.0")
    compileOnly(bungeecord())
}

bungee {
    description = "Simple Bungeecord plugin to log joining/leaving"
    author = "James Chen"
}

tasks {
    jar {
        enabled = false
        dependsOn(shadowJar)
    }
}


