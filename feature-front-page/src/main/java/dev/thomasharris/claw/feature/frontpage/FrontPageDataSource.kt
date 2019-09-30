package dev.thomasharris.claw.feature.frontpage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource
import dev.thomasharris.claw.lib.lobsters.FrontPageTag
import dev.thomasharris.claw.lib.lobsters.LoadingStatus
import dev.thomasharris.claw.lib.lobsters.StoryRepository
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
            callback.onResult(p.map {
                it.toItem(tagMap * it.tags)
            } + FrontPageItem.Divider(2), null, 1)
        }
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, FrontPageItem>) {
        val tagMap = tagRepository.getFrontPageTagsSync()
        storyRepository.getFrontPageSync(params.key)?.let { page ->
            callback.onResult(
                page.map {
                    it.toItem(tagMap * it.tags)
                } + FrontPageItem.Divider(params.key + 2),
                params.key + 1
            )
        }
    }


    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, FrontPageItem>) {
        val tagMap = tagRepository.getFrontPageTagsSync()
        storyRepository.getFrontPageSync(params.key)?.let { page ->
            val adjacentKey = if (params.key == 0) null else params.key + 1
            callback.onResult(
                page.map {
                    it.toItem(tagMap * it.tags)
                } + FrontPageItem.Divider(params.key + 2),
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

// do our own combining of tables here because
// I don't know how to do that sort of thing in sql
operator fun Map<String, FrontPageTag>.times(l: List<String>) = l.mapNotNull {
    get(it)
}

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