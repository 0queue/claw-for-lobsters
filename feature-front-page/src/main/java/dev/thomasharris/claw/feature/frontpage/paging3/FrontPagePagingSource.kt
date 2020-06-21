package dev.thomasharris.claw.feature.frontpage.paging3

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.github.michaelbull.result.mapBoth
import dev.thomasharris.claw.feature.frontpage.FrontPageItem
import dev.thomasharris.claw.feature.frontpage.x
import dev.thomasharris.claw.lib.lobsters.AsyncStoryRepository
import dev.thomasharris.claw.lib.lobsters.AsyncTagRepository
import javax.inject.Inject

class FrontPagePagingSource @Inject constructor(
    private val storyRepository: AsyncStoryRepository,
    private val tagRepository: AsyncTagRepository
) : PagingSource<Int, FrontPageItem>() {

    init {
        registerInvalidatedCallback {
            Log.i("FrontPagePagingSource", "Invalidated!")
            storyRepository.refresh()
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, FrontPageItem> {

        Log.i("FrontPagePagingSource", "Load params: ${params.key} $params")

        val index = params.key ?: 0

        val tagMap = tagRepository.getFrontPageTags()

        return storyRepository.getFrontPage(index).mapBoth(
            success = { p ->
                val items = p.map { it x tagMap } + FrontPageItem.Divider(index + 2)
                Log.i("FrontPagePagingSource", "Loaded ${items.map { item -> (item as? FrontPageItem.Story)?.frontPageStory?.title }}")
                LoadResult.Page(items, params.key?.minus(1), index + 1)
            },
            failure = { ex ->
                Log.e("FrontPagePagingSource", "failed to fetch page with index $index", ex)
                LoadResult.Error(ex)
            }
        )
    }
}

@ExperimentalPagingApi
class Mediator : RemoteMediator<Int, FrontPageItem>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, FrontPageItem>
    ): MediatorResult {
        TODO("Not yet implemented")
    }

}