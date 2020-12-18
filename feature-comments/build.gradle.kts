import dev.thomasharris.claw.build.Deps

plugins {
    id("dev.thomasharris.claw.android")
}

dependencies {
    implementation(Deps.Kotlin.stdlib)
    implementation(project(":core"))
    implementation(project(":lib-better-html"))

    // util
    implementation(Deps.Kotlin.X.coroutinesAndroid)
    implementation(Deps.Android.X.coreKtx)

    // dagger
    implementation(Deps.Dagger.dagger)
    kapt(Deps.Dagger.compiler)

    // conductor
    implementation(Deps.Conductor.conductor)
    implementation(Deps.Conductor.lifecycle)

    // material
    implementation(Deps.Android.X.appCompat)
    implementation(Deps.Android.X.swipeRefreshLayout)
    implementation(Deps.Android.X.constraintLayout)
    implementation(Deps.material)

    // other
    implementation(Deps.coil)
    implementation(Deps.Android.X.browser)
    implementation(Deps.Android.X.lifecycleRuntime)

    // testing
    testImplementation(Deps.junit)
}
