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

    // testing
    testImplementation(Deps.junit)
}
