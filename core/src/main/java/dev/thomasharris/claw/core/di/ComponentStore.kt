package dev.thomasharris.claw.core.di

import kotlin.reflect.KClass

interface ComponentStore<U> {
    fun <T : Any> get(obj: KClass<T>, factory: (U) -> T): T
}