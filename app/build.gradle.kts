plugins {
    id("com.android.application")
    id("dev.thomasharris.claw.android")
}

android.defaultConfig.applicationId = "dev.thomasharris.claw"

dependencies {
    implementation(project(":core"))
    implementation(project(":feature-front-page"))
    implementation(project(":feature-comments"))
}