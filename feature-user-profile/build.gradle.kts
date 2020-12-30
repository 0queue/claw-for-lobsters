import dev.thomasharris.claw.build.Deps

plugins {
    id("dev.thomasharris.claw.android")
}

dependencies {
    implementation(Deps.Kotlin.stdlib)
    implementation(project(":core"))
    implementation(project(":lib-swipe-back"))

    // coroutines
    implementation(Deps.Kotlin.X.coroutinesAndroid)
    implementation(Deps.Android.X.lifecycleRuntime)

    // dagger
    implementation(Deps.Dagger.dagger)
    kapt(Deps.Dagger.compiler)

    // conductor
    implementation(Deps.Conductor.conductor)
    implementation(Deps.Conductor.lifecycle)

    // material
    implementation(Deps.Android.X.appCompat)
    implementation(Deps.Android.X.constraintLayout)
    implementation(Deps.Android.X.coreKtx)
    implementation(Deps.material)

    // other
    implementation(Deps.coil)
    implementation(Deps.commonmark)
}
