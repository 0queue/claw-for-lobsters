import dev.thomasharris.build.conductor
import dev.thomasharris.build.material
import dev.thomasharris.build.testing

plugins {
    id("dev.thomasharris.claw.android")
}

dependencies {
    testing()
    material()
    conductor()

    implementation(project(":core"))
}