import dev.thomasharris.claw.build.testing

plugins {
    id("com.squareup.sqldelight")
    id("dev.thomasharris.claw.android")
}

android.buildTypes.getByName("release").consumerProguardFile("proguard-rules.pro")

kapt {
    correctErrorTypes = true
}

dependencies {
    testing()

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.moshi:moshi-adapters:1.11.0")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.11.0")

    implementation("com.squareup.sqldelight:android-driver:1.4.4")
    api("com.squareup.sqldelight:android-paging-extensions:1.4.4")
    implementation("com.squareup.sqldelight:coroutines-extensions:1.4.4")
    implementation("com.michael-bull.kotlin-result:kotlin-result:1.1.9")
}