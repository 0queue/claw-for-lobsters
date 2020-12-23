package dev.thomasharris.claw.lib.lobsters

import com.github.michaelbull.result.map
import com.github.michaelbull.result.onSuccess
import com.squareup.sqldelight.runtime.coroutines.asFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import com.github.michaelbull.result.runCatching as catch

@Singleton
class AsyncUserRepository @Inject constructor(
    private val lobstersService: LobstersService,
    private val lobstersQueries: LobstersQueries,
) {

    // TODO loading status

    suspend fun latestUser(username: String) = withContext(Dispatchers.IO) {
        lobstersQueries.getUser(username).asFlow().map { it.oneOrNull() }
    }

    suspend fun refresh(username: String) {
        withContext(Dispatchers.IO) {
            lobstersService.catch { getUser(username) }
                .map { it.toDB() }
                .onSuccess(lobstersQueries::insertUser)
        }
    }
}
