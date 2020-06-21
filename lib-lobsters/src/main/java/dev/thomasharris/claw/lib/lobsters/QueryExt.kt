package dev.thomasharris.claw.lib.lobsters

import com.squareup.sqldelight.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun <T:Any> Query<T>.list() : List<T> = withContext(Dispatchers.IO) {
    @Suppress("RemoveExplicitTypeArguments")
    (suspendCoroutine<List<T>> { cont ->
        cont.resume(executeAsList())
    })
}

suspend fun <T:Any> Query<T>.one() : T = withContext(Dispatchers.IO) {
    @Suppress("RemoveExplicitTypeArguments")
    suspendCoroutine<T> { cont ->
        cont.resume(executeAsOne())
    }
}

suspend fun <T:Any> Query<T>.oneOrNull() : T? = withContext(Dispatchers.IO) {
    @Suppress("RemoveExplicitTypeArguments")
    suspendCoroutine<T?> { cont ->
        cont.resume(executeAsOneOrNull())
    }
}