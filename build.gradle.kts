import dev.thomasharris.claw.build.NewModuleTask

plugins {
    id("com.github.ben-manes.versions") version "0.36.0"
    id("org.jlleitschuh.gradle.ktlint") version "9.4.1"
}

buildscript {
    repositories {
        google()
        jcenter()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:4.1.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.21")
        classpath("com.squareup.sqldelight:gradle-plugin:1.4.4")
        classpath("com.github.ben-manes:gradle-versions-plugin:0.36.0")
    }
}

allprojects {
    repositories {
        google()
        jcenter()
    }
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
}

tasks.register("clean", Delete::class.java) {
    delete(rootProject.buildDir)
}

tasks.register("newModule", NewModuleTask::class.java)