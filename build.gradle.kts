// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    val kotlin_version by extra("1.3.72")
    val kotlinVersion = "1.3.50"

    repositories {
        google()
        jcenter()
        maven { url = uri("https://maven.google.com/") }
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://maven.fabric.io/public") }
    }

    dependencies {
        classpath("com.android.tools.build:gradle:4.0.1")
        classpath(kotlin("gradle-plugin", kotlinVersion))
        classpath("io.fabric.tools:gradle:1.31.2")  // Crashlytics plugin
        classpath("com.google.gms:google-services:4.3.3")
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

plugins {
    id("de.fayard.buildSrcVersions") version "0.4.2"
}

allprojects {
    repositories {
        google()
        jcenter()
    }
}

tasks {
    val clean by registering(Delete::class) {
        delete(buildDir)
    }
}

buildSrcVersions {
    indent = "  "
    renameLibs = "Libs"
    renameVersions = "Versions"
    rejectedVersionKeywords("alpha", "beta", "rc", "cr", "m", "preview", "eap")
    useFdqnFor() // nothing
}