package dev.thomasharris.claw.core

interface HasBinding<T> {
    var binding: T?

    fun requireBinding(): T = binding!!
}

fun <T> HasBinding<T>.withBinding(block: T.() -> Unit) {
    binding?.let { it.block() }
}
