package dev.thomasharris.claw.feature.frontpage.paging3

import android.util.Log
import androidx.paging.PagingSource
import com.github.michaelbull.result.mapBoth
import dev.thomasharris.claw.lib.lobsters.AsyncStoryRepository
import dev.thomasharris.claw.lib.lobsters.StoryModel
import javax.inject.Inject

class FrontPagePagingSource @Inject constructor(
    private val storyRepository: AsyncStoryRepository
) : PagingSource<Int, StoryModel>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, StoryModel> {

        val index = params.key ?: 0

        return storyRepository.getFrontPage(index, params is LoadParams.Refresh).mapBoth(
            success = { p ->
                LoadResult.Page(p, params.key?.minus(1), index + 1)
            },
            failure = { ex ->
                Log.e("FrontPagePagingSource", "failed to fetch page with index $index", ex)
                LoadResult.Error(ex)
            }
        )
    }
}