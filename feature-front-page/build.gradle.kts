import dev.thomasharris.build.coil
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
    coil()

    implementation(project(":core"))

    implementation("androidx.paging:paging-runtime-ktx:2.1.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.2.0-alpha05")
}