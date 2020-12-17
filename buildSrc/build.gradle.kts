plugins {
    `kotlin-dsl`
}

repositories {
    google()
    jcenter()
    mavenCentral()
}

dependencies {
    implementation("com.android.tools.build:gradle:4.1.1")
    // CANNNOT UPDATE UNTIL kotlin-dsl PLUGIN UPDATES
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.72")

    implementation(gradleApi())
    implementation(localGroovy())
}