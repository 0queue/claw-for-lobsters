import dev.thomasharris.claw.build.testing

plugins {
    id("dev.thomasharris.claw.android")
    id("com.squareup.sqldelight")
}

android.buildTypes.getByName("release").consumerProguardFile("proguard-rules.pro")

dependencies {
    testing()

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.moshi:moshi-adapters:1.9.2")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.9.2")

    implementation("com.squareup.sqldelight:android-driver:1.3.0")
    api("com.squareup.sqldelight:android-paging-extensions:1.3.0")
    implementation("com.squareup.sqldelight:coroutines-extensions:1.3.0")
}