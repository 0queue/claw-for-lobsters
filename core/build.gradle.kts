import dev.thomasharris.build.androidTesting
import dev.thomasharris.build.testing

plugins {
    id("dev.thomasharris.claw.android")
}

dependencies {
    testing()
    androidTesting()

    implementation("androidx.appcompat:appcompat:1.1.0")
    implementation("androidx.core:core-ktx:1.1.0")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")

    implementation(project(":lib-lobsters"))
}