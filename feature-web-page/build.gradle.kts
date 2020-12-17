import dev.thomasharris.claw.build.conductor
import dev.thomasharris.claw.build.material
import dev.thomasharris.claw.build.testing

plugins {
    id("dev.thomasharris.claw.android")
}

dependencies {
    testing()
    conductor()
    material()

    implementation(project(":lib-navigator"))
    implementation("androidx.browser:browser:1.3.0")
}