import dev.thomasharris.build.conductor
import dev.thomasharris.build.glide
import dev.thomasharris.build.material
import dev.thomasharris.build.testing

plugins {
    id("dev.thomasharris.claw.android")
}

dependencies {
    testing()
    conductor()
    material()
    glide()

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.2.0-alpha05")
    implementation("androidx.browser:browser:1.0.0")
    implementation(project(":core"))
}