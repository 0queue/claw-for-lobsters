package dev.thomasharris.feature.frontpage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource
import dev.thomasharris.lib.lobsters.Story
import dev.thomasharris.lib.lobsters.StoryRepository

/**
 * All the load methods occur on an IO thread already,
 * so just fetch things synchronously
 */
class StoryDataSource(
    private val storyRepository: StoryRepository,
    private val loadingStatus: MutableLiveData<LoadingStatus>
) : PageKeyedDataSource<Int, FrontPageItem>() {

    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, FrontPageItem>
    ) {
        loadingStatus.postValue(LoadingStatus.LOADING)
        storyRepository.getPageSync(0) {
            loadingStatus.postValue(if (it != null) LoadingStatus.DONE else LoadingStatus.ERROR)
            it?.let { page ->
                callback.onResult(page.map(Story::frontPage) + FrontPageDivider(2), null, 1)
            }
        }
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, FrontPageItem>) =
        storyRepository.getPageSync(params.key) {
            it?.let { page ->
                callback.onResult(
                    page.map(Story::frontPage) + FrontPageDivider(params.key + 2),
                    params.key + 1
                )
            }
        }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, FrontPageItem>) =
        storyRepository.getPageSync(params.key) {
            val adjacentKey = if (params.key == 0) null else params.key + 1
            it?.let { page ->
                callback.onResult(
                    page.map(Story::frontPage) + FrontPageDivider(params.key + 2),
                    adjacentKey
                )
            }
        }

    override fun invalidate() {
        storyRepository.invalidate()
        super.invalidate()
    }
}

class StoryDataSourceFactory(
    private val storyRepository: StoryRepository
) : DataSource.Factory<Int, FrontPageItem>() {
    private val _loadingStatus = MutableLiveData<LoadingStatus>()
    val loadingStatus: LiveData<LoadingStatus> = _loadingStatus

    override fun create(): DataSource<Int, FrontPageItem> =
        StoryDataSource(storyRepository, _loadingStatus)
}

enum class LoadingStatus {
    LOADING,
    ERROR,
    DONE
}