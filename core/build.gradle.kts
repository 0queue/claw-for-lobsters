import dev.thomasharris.build.*

plugins {
    id("dev.thomasharris.claw.android")
}

dependencies {
    testing()
    androidTesting()
    conductor()
    material()
    glide()

    implementation("org.jetbrains.kotlin:kotlin-reflect:1.3.50")

    api(project(":lib-lobsters"))
    api(project(":lib-navigator"))
}