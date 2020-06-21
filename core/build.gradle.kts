import dev.thomasharris.claw.build.androidTesting
import dev.thomasharris.claw.build.coil
import dev.thomasharris.claw.build.conductor
import dev.thomasharris.claw.build.material
import dev.thomasharris.claw.build.testing

plugins {
    id("dev.thomasharris.claw.android")
}

dependencies {
    testing()
    androidTesting()
    conductor()
    material()
    coil()

    implementation("org.jetbrains.kotlin:kotlin-reflect:1.3.72")

    api("com.michael-bull.kotlin-result:kotlin-result:1.1.6")

    api(project(":lib-lobsters"))
    api(project(":lib-navigator"))
}