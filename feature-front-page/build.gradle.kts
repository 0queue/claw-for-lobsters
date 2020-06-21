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

    implementation(project(":core"))

    implementation("androidx.paging:paging-runtime:3.0.0-alpha01")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.2.0")
}