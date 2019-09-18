package dev.thomasharris.claw.core

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import dagger.Component
import dev.thomasharris.lib.lobsters.di.LobstersModule

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        DaggerTempComponent.builder()
            .lobstersModule(LobstersModule())
            .build()
    }
}

@Component(modules = [LobstersModule::class])
interface TempComponent
