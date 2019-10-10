package dev.thomasharris.claw.lib.lobsters

import java.util.concurrent.atomic.AtomicBoolean

enum class LoadingStatus {
    LOADING,
    ERROR,
    DONE
}

class Event<T>(private val value: T) {

    private val hasBeenHandled = AtomicBoolean(false)

    fun consume(listener: (T) -> Unit) {
        if (hasBeenHandled.compareAndSet(false, true))
            listener(value)
    }

    fun peek(): T {
        return value
    }

    override fun toString(): String {
        return "Event(value = $value, hasBeenHandled = ${hasBeenHandled.get()})"
    }
}
