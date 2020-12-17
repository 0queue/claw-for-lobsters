package dev.thomasharris.claw.build

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.AndroidBasePlugin
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Properties

/**
 * Technique borrowed from https://quickbirdstudios.com/blog/gradle-kotlin-buildsrc-plugin-android/
 *
 * Seems to only like being the last plugin specified
 */
open class ClawPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = target.run {
        apply {
            plugin("java-library")
            plugin("kotlin")
            plugin("kotlin-kapt")
        }

        configureKotlin()
    }
}

open class ClawAndroidPlugin : Plugin<Project> {
    override fun apply(target: Project) = target.run {
        apply {
            if (!hasAndroid())
                plugin("com.android.library")

            plugin("kotlin-android")

            plugin("kotlin-kapt")
        }

        configureKotlin()
        configureAndroid()
    }

}

internal fun Project.hasAndroid() = plugins.any { it is AndroidBasePlugin }

internal fun Project.configureAndroid() {
    extensions.getByType<BaseExtension>().run {
        compileSdkVersion(29)
        defaultConfig {
            minSdkVersion(23)
            targetSdkVersion(29)
            versionCode = 15
            versionName = "15"
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        signingConfigs {
            create("release") {
                val props = Properties().apply {
                    load(rootProject.file("keystore.properties").inputStream())
                }
                // fine with this all crashing if the types are wrong
                storeFile = rootProject.file(props["storeFile"]!!)
                storePassword = props["storePassword"] as String
                keyAlias = props["keyAlias"] as String
                keyPassword = props["keyPassword"] as String
            }
        }

        buildTypes {
            getByName("release") {
                signingConfig = signingConfigs.getByName("release")
                isMinifyEnabled = true
            }
        }

        buildFeatures.viewBinding = true

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }
    }

    extensions.findByType<LibraryExtension>()?.run {
        buildTypes.getByName("release").consumerProguardFile("proguard-rules.pro")
    }

    extensions.findByType<AppExtension>()?.run {
        buildTypes.getByName("release") {
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

internal fun Project.configureKotlin() = tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}