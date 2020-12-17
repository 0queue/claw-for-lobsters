package dev.thomasharris.claw.build

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.AndroidBasePlugin
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Properties

/**
 * Technique borrowed from https://quickbirdstudios.com/blog/gradle-kotlin-buildsrc-plugin-android/
 */
open class ClawPlugin : Plugin<Project> {
    override fun apply(target: Project) = target.run {
        apply {
            plugin("java-library")
            plugin("kotlin")
            plugin("kotlin-kapt")
        }

        configureKotlin()
        configureDependencies()
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
        configureDependencies()
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

        dependencies.add("implementation", "androidx.core:core-ktx:1.3.2")
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

/**
 * Dependencies that everyone has, guaranteed (kotlin, dagger)
 */
internal fun Project.configureDependencies() {
    dependencies.run {
        add("implementation", "org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.4.21")
        add("implementation", "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.4.2")
        add("implementation", "com.google.dagger:dagger:2.30.1")
        add("kapt", "com.google.dagger:dagger-compiler:2.30.1")
    }
}

internal fun Project.configureKotlin() = tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

fun DependencyHandler.testing() {
    add("testImplementation", "junit:junit:4.13")
}

fun DependencyHandler.androidTesting() {
    add("androidTestImplementation", "androidx.test:runner:1.2.0")
    add("androidTestImplementation", "androidx.test.espresso:espresso-core:3.2.0")
}

fun DependencyHandler.conductor() {
    add("implementation", "com.bluelinelabs:conductor:3.0.0-rc6")
    add("implementation", "com.bluelinelabs:conductor-archlifecycle:3.0.0-rc6")
}

fun DependencyHandler.material() {
    add("implementation", "androidx.appcompat:appcompat:1.2.0")
    add("implementation", "androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    add("implementation", "com.google.android.material:material:1.3.0-beta01")
    add("implementation", "androidx.constraintlayout:constraintlayout:2.0.4")
}

fun DependencyHandler.coil() {
    add("implementation", "io.coil-kt:coil:1.1.0")
}