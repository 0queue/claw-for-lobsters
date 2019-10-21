import dev.thomasharris.build.conductor
import dev.thomasharris.build.material
import dev.thomasharris.build.testing

plugins {
    id("dev.thomasharris.claw.android")
}

dependencies {
    testing()
    conductor()
    material()

    implementation(project(":lib-navigator"))
    implementation("androidx.browser:browser:1.0.0")
}