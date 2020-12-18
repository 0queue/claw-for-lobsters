package dev.thomasharris.claw.core

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.jakewharton.threetenabp.AndroidThreeTen
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

        val preference = singletonComponent.preferencesRepository().getTheme()
        AppCompatDelegate.setDefaultNightMode(preference.modeNight)

        AndroidThreeTen.init(this)
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
