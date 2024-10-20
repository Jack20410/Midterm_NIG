plugins {
    kotlin("jvm") version "1.9.10" // Apply the Kotlin plugin at the top level
}

buildscript {
    repositories {
        google()
        mavenCentral()
        repositories {
            maven {url = uri("https://www.jitpack.io")}
        }
    }
    dependencies {
        classpath(libs.gradle)
        classpath(libs.google.services)
    }
}

allprojects {
    repositories {
        google()

        mavenCentral()
        maven {url = uri("https://www.jitpack.io")}
    }
}
