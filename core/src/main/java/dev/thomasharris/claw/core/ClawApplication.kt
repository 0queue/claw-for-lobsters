package dev.thomasharris.claw.core

import android.app.Application
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import dev.thomasharris.claw.core.di.ComponentStore
import dev.thomasharris.claw.core.di.DaggerSingletonComponent
import dev.thomasharris.claw.core.di.PrefsModule
import dev.thomasharris.claw.core.di.SingletonComponent
import dev.thomasharris.claw.lib.lobsters.di.LobstersModule
import kotlin.reflect.KClass

@Suppress("unused")
class ClawApplication : Application(), ComponentStore<SingletonComponent> {
    private val components: MutableSet<Any> = mutableSetOf()
    private lateinit var singletonComponent: SingletonComponent

    override fun onCreate() {
        super.onCreate()

        singletonComponent = DaggerSingletonComponent.builder()
            .lobstersModule(LobstersModule(this))
            .prefsModule(PrefsModule(this))
            .build()

        val default = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        else
            AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY

        val preference = singletonComponent.sharedPreferences()
            .getInt("NIGHT_MODE", default)
        AppCompatDelegate.setDefaultNightMode(preference)
    }

    override fun <T : Any> get(
        obj: KClass<T>,
        factory: (SingletonComponent) -> T
    ): T {
        // isInstance check makes this safe
        @Suppress("UNCHECKED_CAST") val first = components.firstOrNull(obj::isInstance) as T?
        if (first != null)
            return first


        val t = factory(singletonComponent)
        components.add(t)
        return t
    }
}