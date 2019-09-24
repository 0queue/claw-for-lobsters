package dev.thomasharris.claw.core

import android.app.Application
import dev.thomasharris.claw.core.di.ComponentStore
import dev.thomasharris.claw.core.di.SingletonComponent
import kotlin.reflect.KClass

class ClawApplication : Application(), ComponentStore<SingletonComponent> {
    private val components: MutableSet<Any> = mutableSetOf()
    private lateinit var singletonComponent: SingletonComponent

    override fun onCreate() {
        super.onCreate()
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