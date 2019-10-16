package dev.thomasharris.claw.feature.frontpage

import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource
import dev.thomasharris.claw.lib.lobsters.StoryModel
import dev.thomasharris.claw.lib.lobsters.StoryRepository
import dev.thomasharris.claw.lib.lobsters.TagModel
import dev.thomasharris.claw.lib.lobsters.TagRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * All the load methods occur on an IO thread already,
 * so just fetch things synchronously
 */
class FrontPageDataSource(
    private val storyRepository: StoryRepository,
    private val tagRepository: TagRepository,
    private val scope: CoroutineScope
) : PageKeyedDataSource<Int, FrontPageItem>() {

    @ExperimentalCoroutinesApi
    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, FrontPageItem>
    ) {
        val page = storyRepository.getFrontPageSync(0)
        val tagMap = tagRepository.getFrontPageTagsSync()
        page?.let { p ->
            callback.onResult(p.map { it x tagMap } + FrontPageItem.Divider(2), null, 1)
        }
    }

    @ExperimentalCoroutinesApi
    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, FrontPageItem>) {
        val tagMap = tagRepository.getFrontPageTagsSync()
        storyRepository.getFrontPageSync(params.key)?.let { page ->
            callback.onResult(
                page.map { it x tagMap } + FrontPageItem.Divider(params.key + 2),
                params.key + 1
            )
        }
    }


    @ExperimentalCoroutinesApi
    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, FrontPageItem>) {
        val tagMap = tagRepository.getFrontPageTagsSync()
        storyRepository.getFrontPageSync(params.key)?.let { page ->
            val adjacentKey = if (params.key == 0) null else params.key - 1
            callback.onResult(
                page.map { it x tagMap } + FrontPageItem.Divider(params.key + 2),
                adjacentKey
            )
        }
    }

    @ExperimentalCoroutinesApi
    override fun invalidate() {
        scope.launch {
            if (storyRepository.refresh()) {
                tagRepository.invalidate()
                super.invalidate()
            }
        }
    }
}

infix fun StoryModel.x(tagMap: Map<String, TagModel>) =
    FrontPageItem.Story(this, tags.map {
        tagMap[it] ?: TagModel.Impl(it, false)
    })

class StoryDataSourceFactory(
    private val storyRepository: StoryRepository,
    private val tagRepository: TagRepository,
    private val scope: CoroutineScope
) : DataSource.Factory<Int, FrontPageItem>() {

    override fun create(): DataSource<Int, FrontPageItem> =
        FrontPageDataSource(
            storyRepository,
            tagRepository,
            scope
        )
}

// uh oh
// doing this to not give the factory a lifecycleScope,
// which can cancel and then we never get it back :(
class StoryDataSourceFactoryFactory @Inject constructor(
    private val storyRepository: StoryRepository,
    private val tagRepository: TagRepository
) {
    fun create(scope: CoroutineScope): StoryDataSourceFactory {
        return StoryDataSourceFactory(storyRepository, tagRepository, scope)
    }
}