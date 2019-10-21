import dev.thomasharris.claw.build.coil
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
    coil()

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.2.0-alpha05")
    implementation("androidx.browser:browser:1.0.0")
    implementation(project(":core"))
}