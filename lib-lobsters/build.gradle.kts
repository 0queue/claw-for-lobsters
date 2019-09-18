import dev.thomasharris.build.testing

plugins {
    id("dev.thomasharris.claw")
}

dependencies {
    testing()

    implementation("com.squareup.retrofit2:retrofit:2.6.1")
    implementation("com.squareup.retrofit2:converter-moshi:2.6.1")
    implementation("com.squareup.moshi:moshi-adapters:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.2.1")
}