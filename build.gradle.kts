plugins {
    kotlin("jvm") version "1.8.0"
    application
}

group = "me.dzone"
version = "1.1.1"

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
