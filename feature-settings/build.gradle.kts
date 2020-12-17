import dev.thomasharris.claw.build.conductor
import dev.thomasharris.claw.build.material
import dev.thomasharris.claw.build.testing

plugins {
    id("dev.thomasharris.claw.android")
}

android.buildTypes.all {
    buildConfigField("int", "VERSION_CODE", android.defaultConfig.versionCode.toString())
}

dependencies {
    testing()
    material()
    conductor()

    implementation(project(":core"))
}