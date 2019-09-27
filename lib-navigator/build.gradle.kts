import dev.thomasharris.build.conductor
import dev.thomasharris.build.testing

plugins {
    id("dev.thomasharris.claw.android")
}

dependencies {
    testing()
    conductor()

    implementation("org.jetbrains.kotlin:kotlin-reflect:1.3.50")
}