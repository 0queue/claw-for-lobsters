import dev.thomasharris.claw.build.Deps

plugins {
    id("dev.thomasharris.claw.android")
}

dependencies {
    implementation(Deps.Kotlin.stdlib)
    implementation(project(":core"))

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
    implementation(Deps.Android.X.appCompat)
    implementation(Deps.Android.X.swipeRefreshLayout)
    implementation(Deps.Android.X.constraintLayout)
    implementation(Deps.material)

    // other
    implementation(Deps.coil)
    implementation(Deps.Android.X.pagingRuntime)
    implementation(Deps.Android.X.lifecycleRuntime)

    // testing
    testImplementation(Deps.junit)
}