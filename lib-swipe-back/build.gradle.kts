import dev.thomasharris.claw.build.Deps

plugins {
    id("dev.thomasharris.claw.android")
}

dependencies {
    implementation(Deps.Kotlin.stdlib)

    implementation(Deps.Android.X.appCompat)
    implementation(Deps.material)
}
