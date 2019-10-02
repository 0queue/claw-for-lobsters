package dev.thomasharris.claw.feature.frontpage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource
import dev.thomasharris.claw.lib.lobsters.LoadingStatus
import dev.thomasharris.claw.lib.lobsters.StoryModel
import dev.thomasharris.claw.lib.lobsters.StoryRepository
import dev.thomasharris.claw.lib.lobsters.TagModel
import dev.thomasharris.claw.lib.lobsters.TagRepository

/**
 * All the load methods occur on an IO thread already,
 * so just fetch things synchronously
 */
class FrontPageDataSource(
    private val storyRepository: StoryRepository,
    private val tagRepository: TagRepository,
    private val loadingStatus: MutableLiveData<LoadingStatus>
) : PageKeyedDataSource<Int, FrontPageItem>() {

    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, FrontPageItem>
    ) {
        loadingStatus.postValue(LoadingStatus.LOADING)
        val page = storyRepository.getFrontPageSync(0)
        val tagMap = tagRepository.getFrontPageTagsSync()
        loadingStatus.postValue(if (page != null) LoadingStatus.DONE else LoadingStatus.ERROR)
        page?.let { p ->
            callback.onResult(p.map { it x tagMap } + FrontPageItem.Divider(2), null, 1)
        }
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, FrontPageItem>) {
        val tagMap = tagRepository.getFrontPageTagsSync()
        storyRepository.getFrontPageSync(params.key)?.let { page ->
            callback.onResult(
                page.map { it x tagMap } + FrontPageItem.Divider(params.key + 2),
                params.key + 1
            )
        }
    }


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

    override fun invalidate() {
        storyRepository.invalidate()
        tagRepository.invalidate()
        super.invalidate()
    }
}

infix fun StoryModel.x(tagMap: Map<String, TagModel>) =
    FrontPageItem.Story(this, tags.map {
        tagMap[it] ?: TagModel.Impl(it, false)
    })

class StoryDataSourceFactory(
    private val storyRepository: StoryRepository,
    private val tagRepository: TagRepository
) : DataSource.Factory<Int, FrontPageItem>() {
    private val _loadingStatus = MutableLiveData<LoadingStatus>()
    val loadingStatus: LiveData<LoadingStatus> = _loadingStatus

    override fun create(): DataSource<Int, FrontPageItem> =
        FrontPageDataSource(
            storyRepository,
            tagRepository,
            _loadingStatus
        )
}