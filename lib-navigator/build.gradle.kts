import dev.thomasharris.claw.build.conductor
import dev.thomasharris.claw.build.testing

plugins {
    id("dev.thomasharris.claw.android")
}

dependencies {
    testing()
    conductor()
}