import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.21"
    id("org.jetbrains.compose") version "1.2.0-alpha01-dev686"
}

group = "club.eridani"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    implementation("io.github.humbleui:jwm:0.4.2")
    implementation(compose.desktop.currentOs)
    testImplementation(kotlin("test"))
}


tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}