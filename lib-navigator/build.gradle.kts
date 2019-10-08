import dev.thomasharris.build.conductor
import dev.thomasharris.build.testing

plugins {
    id("dev.thomasharris.claw.android")
}

dependencies {
    testing()
    conductor()
}