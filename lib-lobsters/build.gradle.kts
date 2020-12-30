import dev.thomasharris.claw.build.Deps

plugins {
    id("com.squareup.sqldelight")
    id("dev.thomasharris.claw.android")
}

android.buildTypes.getByName("release").consumerProguardFile("proguard-rules.pro")

kapt {
    correctErrorTypes = true
}

dependencies {
    implementation(Deps.Kotlin.stdlib)

    // util
    implementation(Deps.Android.X.coreKtx)
    implementation(Deps.Kotlin.X.coroutinesAndroid)
    implementation(Deps.Kotlin.result)

    // dagger
    implementation(Deps.Dagger.dagger)
    kapt(Deps.Dagger.compiler)

    // retrofit
    implementation(Deps.Square.Retrofit.retrofit)
    implementation(Deps.Square.Retrofit.converterMoshi)
    implementation(Deps.Square.Moshi.adapters)
    kapt(Deps.Square.Moshi.codegen)

    // sqldelight
    api(Deps.Square.SqlDelight.pagingExtensions)
    implementation(Deps.Square.SqlDelight.androidDriver)
    implementation(Deps.Square.SqlDelight.coroutinesExtensions)

    // testing
    testImplementation(Deps.junit)
}
