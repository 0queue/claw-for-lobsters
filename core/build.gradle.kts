import dev.thomasharris.build.androidTesting
import dev.thomasharris.build.conductor
import dev.thomasharris.build.material
import dev.thomasharris.build.testing

plugins {
    id("dev.thomasharris.claw.android")
}

dependencies {
    testing()
    androidTesting()
    conductor()
    material()

    implementation("org.jetbrains.kotlin:kotlin-reflect:1.3.50")

    implementation("androidx.appcompat:appcompat:1.1.0")
    implementation("androidx.core:core-ktx:1.1.0")

    implementation(project(":lib-lobsters"))
}