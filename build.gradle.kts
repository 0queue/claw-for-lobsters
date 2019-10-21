import dev.thomasharris.claw.build.NewModuleTask

// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
//    ext.kotlin_version = '1.3.50'
    repositories {
        google()
        jcenter()
        
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.5.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.50")
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files

        classpath("com.squareup.sqldelight:gradle-plugin:1.2.0")
    }
}

allprojects {
    repositories {
        google()
        jcenter()
    }
}

tasks.register("clean", Delete::class.java) {
    delete(rootProject.buildDir)
}

tasks.register("newModule", NewModuleTask::class.java)