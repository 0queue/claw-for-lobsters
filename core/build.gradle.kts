import dev.thomasharris.claw.build.Deps

plugins {
    id("dev.thomasharris.claw.android")
}

dependencies {
    implementation(Deps.Kotlin.stdlib)

    // not sure when I can remove this, the build still complains about java.time.*
    api(Deps.threetenAbp)

    // api
    api(Deps.Kotlin.result)
    api(project(":lib-lobsters"))
    api(project(":lib-navigator"))

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
    implementation(Deps.material)
    implementation(Deps.Android.X.constraintLayout)

    // other
    implementation(Deps.coil)
    implementation(Deps.Kotlin.reflect)
    implementation(Deps.jsoup)


    // testing
    testImplementation(Deps.junit)
    androidTestImplementation(Deps.Android.Testing.runner)
    androidTestImplementation(Deps.Android.Testing.espresso)
}