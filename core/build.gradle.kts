import dev.thomasharris.build.androidTesting
import dev.thomasharris.build.coil
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
    coil()

    implementation("org.jetbrains.kotlin:kotlin-reflect:1.3.50")

    api(project(":lib-lobsters"))
    api(project(":lib-navigator"))
}