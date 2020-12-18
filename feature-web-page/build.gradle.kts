import dev.thomasharris.claw.build.Deps

plugins {
    id("dev.thomasharris.claw.android")
}

dependencies {
    implementation(Deps.Kotlin.stdlib)

    // dagger
    implementation(Deps.Dagger.dagger)
    kapt(Deps.Dagger.compiler)

    // util
    implementation(Deps.Android.X.coreKtx)
    implementation(Deps.Kotlin.X.coroutinesAndroid)

    // conductor
    implementation(Deps.Conductor.conductor)
    implementation(Deps.Conductor.lifecycle)

    // material
    implementation(Deps.material)
    implementation(Deps.Android.X.appCompat)
    implementation(Deps.Android.X.swipeRefreshLayout)
    implementation(Deps.Android.X.constraintLayout)

    // other
    implementation(project(":lib-navigator"))
    implementation(Deps.Android.X.browser)

    // testing
    testImplementation(Deps.junit)
}
