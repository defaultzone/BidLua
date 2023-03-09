plugins {
    kotlin("jvm") version "1.8.0"
    application
}

group = "me.dzone"
version = "0.1-PREVIEW"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

application {
    mainClass.set("MainKt")
}