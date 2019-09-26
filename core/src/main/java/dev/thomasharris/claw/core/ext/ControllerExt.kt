package dev.thomasharris.claw.core.ext

import com.bluelinelabs.conductor.Controller
import dev.thomasharris.claw.core.di.ComponentStore
import dev.thomasharris.claw.core.di.SingletonComponent

inline fun <reified T : Any> Controller.getComponent(noinline factory: (SingletonComponent) -> T) =
    lazy {
        @Suppress("UNCHECKED_CAST") // is checked by the as?
        // force unwrap the null because controllers better have an application
        (activity?.application as? ComponentStore<SingletonComponent>)!!.get(T::class, factory)
    }