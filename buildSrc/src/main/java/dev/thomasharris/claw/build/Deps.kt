package dev.thomasharris.claw.build

object Deps {

    const val material = "com.google.android.material:material:1.3.0-beta01"
    const val junit = "junit:junit:4.13"
    const val coil = "io.coil-kt:coil:1.1.0"
    const val jsoup = "org.jsoup:jsoup:1.13.1"
    const val threetenAbp = "com.jakewharton.threetenabp:threetenabp:1.3.0"
    const val commonmark = "com.atlassian.commonmark:commonmark:0.16.1"

    object Kotlin {
        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.4.21"
        const val reflect = "org.jetbrains.kotlin:kotlin-reflect:1.4.21"
        const val result = "com.michael-bull.kotlin-result:kotlin-result:1.1.9"

        object X {
            const val coroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.4.2"
        }
    }

    object Dagger {
        const val dagger = "com.google.dagger:dagger:2.30.1"
        const val compiler = "com.google.dagger:dagger-compiler:2.30.1"
    }

    object Android {
        object X {
            const val coreKtx = "androidx.core:core-ktx:1.3.2"
            const val appCompat = "androidx.appcompat:appcompat:1.2.0"
            const val swipeRefreshLayout = "androidx.swiperefreshlayout:swiperefreshlayout:1.1.0"
            const val constraintLayout = "androidx.constraintlayout:constraintlayout:2.0.4"
            const val browser = "androidx.browser:browser:1.3.0"
            const val lifecycleRuntime = "androidx.lifecycle:lifecycle-runtime-ktx:2.2.0"
            const val pagingRuntime = "androidx.paging:paging-runtime:3.0.0-alpha11"
        }

        object Testing {
            const val runner = "androidx.test:runner:1.2.0"
            const val espresso = "androidx.test.espresso:espresso-core:3.2.0"
        }
    }

    object Conductor {
        const val conductor = "com.bluelinelabs:conductor:3.0.0-rc6"
        const val lifecycle = "com.bluelinelabs:conductor-archlifecycle:3.0.0-rc6"
    }

    object Square {
        object Retrofit {
            const val retrofit = "com.squareup.retrofit2:retrofit:2.9.0"
            const val converterMoshi = "com.squareup.retrofit2:converter-moshi:2.9.0"
        }

        object Moshi {
            const val adapters = "com.squareup.moshi:moshi-adapters:1.11.0"
            const val codegen = "com.squareup.moshi:moshi-kotlin-codegen:1.11.0"
        }

        object SqlDelight {
            const val androidDriver = "com.squareup.sqldelight:android-driver:1.4.4"
            const val pagingExtensions = "com.squareup.sqldelight:android-paging-extensions:1.4.4"
            const val coroutinesExtensions = "com.squareup.sqldelight:coroutines-extensions:1.4.4"
        }
    }

}