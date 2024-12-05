plugins {
    kotlin("jvm") version "1.9.10" // Kotlin JVM Plugin
    id("com.google.gms.google-services") version "4.4.2" apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://www.jitpack.io") }
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.1.1") // Ensure the latest Gradle plugin
        classpath ("com.google.gms:google-services:4.4.2")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://www.jitpack.io") }
    }
}
