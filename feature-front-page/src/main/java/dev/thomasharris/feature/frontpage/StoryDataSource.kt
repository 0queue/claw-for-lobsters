package dev.thomasharris.feature.frontpage

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
) : PageKeyedDataSource<Int, FrontPageStory>() {

    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, FrontPageStory>
    ) = storyRepository.getPageSync(0) {
        callback.onResult(it.map(Story::frontPage), null, 1)
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, FrontPageStory>) =
        storyRepository.getPageSync(params.key) {
            callback.onResult(it.map(Story::frontPage), params.key + 1)
        }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, FrontPageStory>) =
        storyRepository.getPageSync(params.key) {
            val adjacentKey = if (params.key == 0) null else params.key + 1
            callback.onResult(it.map(Story::frontPage), adjacentKey)
        }

    override fun invalidate() {
        storyRepository.invalidate()
        super.invalidate()
    }
}

class StoryDataSourceFactory(
    private val storyRepository: StoryRepository
) : DataSource.Factory<Int, FrontPageStory>() {
    override fun create(): DataSource<Int, FrontPageStory> = StoryDataSource(storyRepository)
}