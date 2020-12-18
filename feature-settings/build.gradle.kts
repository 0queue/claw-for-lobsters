plugins {
    id("dev.thomasharris.claw.android")
}

android.buildTypes.all {
    buildConfigField("int", "VERSION_CODE", android.defaultConfig.versionCode.toString())
}

dependencies {
    implementation(dev.thomasharris.claw.build.Deps.Kotlin.stdlib)
    implementation(project(":core"))

    // dagger
    implementation(dev.thomasharris.claw.build.Deps.Dagger.dagger)
    kapt(dev.thomasharris.claw.build.Deps.Dagger.compiler)

    // util
    implementation(dev.thomasharris.claw.build.Deps.Android.X.coreKtx)
    implementation(dev.thomasharris.claw.build.Deps.Kotlin.X.coroutinesAndroid)

    // conductor
    implementation(dev.thomasharris.claw.build.Deps.Conductor.conductor)
    implementation(dev.thomasharris.claw.build.Deps.Conductor.lifecycle)

    // material
    implementation(dev.thomasharris.claw.build.Deps.material)
    implementation(dev.thomasharris.claw.build.Deps.Android.X.appCompat)
    implementation(dev.thomasharris.claw.build.Deps.Android.X.swipeRefreshLayout)
    implementation(dev.thomasharris.claw.build.Deps.Android.X.constraintLayout)

    // testing
    testImplementation(dev.thomasharris.claw.build.Deps.junit)
}
