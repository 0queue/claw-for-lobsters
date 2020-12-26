plugins {
    id("com.android.application")
    id("dev.thomasharris.claw.android")
}

android.defaultConfig.applicationId = "dev.thomasharris.claw"

dependencies {
    implementation(project(":core"))
    // keep these as implementation so that build variant switching in android studio works properly
    implementation(project(":feature-front-page"))
    implementation(project(":feature-comments"))
    implementation(project(":feature-settings"))
    implementation(project(":feature-web-page"))
    implementation(project(":feature-user-profile"))

    debugRuntimeOnly("com.squareup.leakcanary:leakcanary-android:2.5")
}
