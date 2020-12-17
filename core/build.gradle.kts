import dev.thomasharris.claw.build.androidTesting
import dev.thomasharris.claw.build.coil
import dev.thomasharris.claw.build.conductor
import dev.thomasharris.claw.build.material
import dev.thomasharris.claw.build.testing

plugins {
    id("dev.thomasharris.claw.android")
}

dependencies {
    testing()
    androidTesting()
    conductor()
    material()
    coil()

    implementation("org.jetbrains.kotlin:kotlin-reflect:1.4.21")

    api("com.michael-bull.kotlin-result:kotlin-result:1.1.9")

    api(project(":lib-lobsters"))
    api(project(":lib-navigator"))

    implementation("org.jsoup:jsoup:1.13.1")

    // not sure when I can remove this, the build still complains about java.time.*
    api("com.jakewharton.threetenabp:threetenabp:1.3.0")
}