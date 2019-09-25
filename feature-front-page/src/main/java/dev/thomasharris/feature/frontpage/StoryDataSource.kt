package dev.thomasharris.feature.frontpage

import android.util.Log
import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource
import dev.thomasharris.lib.lobsters.Story
import dev.thomasharris.lib.lobsters.StoryRepository

/**
 * All the load methods occur on an IO thread already,
 * so just fetch things synchronously
 */
class StoryDataSource(
    private val storyRepository: StoryRepository
) : PageKeyedDataSource<Int, Story>() {

    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, Story>
    ) = storyRepository.getPageSync(0) {
        Log.i("TEH", "Fetched initial : (0)")
        callback.onResult(it, null, 1)
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, Story>) =
        storyRepository.getPageSync(params.key) {
            Log.i("TEH", "Fetched after : (${params.key})")
            callback.onResult(it, params.key + 1)
        }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, Story>) =
        storyRepository.getPageSync(params.key) {
            Log.i("TEH", "Fetched before : (${params.key})")
            val adjacentKey = if (params.key == 0) null else params.key + 1
            callback.onResult(it, adjacentKey)
        }

    override fun invalidate() {
        storyRepository.invalidate()
        super.invalidate()
    }
}

class StoryDataSourceFactory(
    private val storyRepository: StoryRepository
) : DataSource.Factory<Int, Story>() {
    override fun create(): DataSource<Int, Story> = StoryDataSource(storyRepository)
}