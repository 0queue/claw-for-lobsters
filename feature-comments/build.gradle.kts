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

    // TODO REMOVE!!!
    implementation("com.squareup.retrofit2:retrofit:2.6.1")

    implementation(project(":core"))
}