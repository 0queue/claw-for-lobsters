plugins {
    id("com.android.application")
    id("dev.thomasharris.claw.android")
}

android.defaultConfig.applicationId = "dev.thomasharris.claw"
// not sure why this is needed
android.packagingOptions.pickFirst("META-INF/kotlinx-coroutines-core.kotlin_module")

dependencies {
    // TODO runtime dependencies only?
    implementation(project(":core"))
    implementation(project(":feature-front-page"))
    implementation(project(":feature-comments"))
    implementation(project(":feature-settings"))
    implementation(project(":feature-web-page"))
    implementation(project(":feature-user-profile"))

    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.5")
}
