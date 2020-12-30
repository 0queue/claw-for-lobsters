plugins {
    `kotlin-dsl`
}

repositories {
    google()
    jcenter()
    mavenCentral()
}

gradlePlugin {
    plugins {
        create("clawPlugin") {
            id = "dev.thomasharris.claw"
            implementationClass = "dev.thomasharris.claw.build.ClawPlugin"
        }

        create("clawAndroidPlugin") {
            id = "dev.thomasharris.claw.android"
            implementationClass = "dev.thomasharris.claw.build.ClawAndroidPlugin"
        }
    }
}

dependencies {
    implementation("com.android.tools.build:gradle:4.1.1")
    // CANNNOT UPDATE UNTIL kotlin-dsl PLUGIN UPDATES
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.72")

    implementation(gradleApi())
    implementation(localGroovy())
}