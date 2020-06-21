package dev.thomasharris.claw.feature.frontpage

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import dev.thomasharris.claw.core.HasBinding
import dev.thomasharris.claw.core.ext.getComponent
import dev.thomasharris.claw.core.ui.ViewLifecycleController
import dev.thomasharris.claw.feature.frontpage.di.DaggerFrontPageComponent
import dev.thomasharris.claw.feature.frontpage.di.FrontPageComponent
import dev.thomasharris.claw.feature.frontpage.di.FrontPageModule
import dev.thomasharris.claw.feature.frontpage.paging3.FrontPageAdapter2
import dev.thomasharris.claw.frontpage.feature.frontpage.R
import dev.thomasharris.claw.frontpage.feature.frontpage.databinding.FrontPageBinding
import dev.thomasharris.claw.lib.lobsters.LoadingStatus
import dev.thomasharris.claw.lib.lobsters.TagModel
import dev.thomasharris.claw.lib.navigator.Destination
import dev.thomasharris.claw.lib.navigator.goto
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Suppress("unused")
class FrontPageController : ViewLifecycleController(), HasBinding<FrontPageBinding> {

    private val component by getComponent<FrontPageComponent> {
        DaggerFrontPageComponent.builder()
            .singletonComponent(it)
            .frontPageModule(FrontPageModule())
            .build()
    }

//    private val liveStories by lazy {
//        val config = PagedList.Config.Builder()
//            .setPageSize(25)
//            // to mitigate stopping while flinging, although a larger story card will help too
//            .setPrefetchDistance(50)
//            .build()
//
//        // TODO creates once, so the lifecycleowner never changes...
//        component.storyDataSourceFactoryFactory().create(viewLifecycleOwner.lifecycleScope)
//            .toLiveData(config)
//    }

    private lateinit var tagMap: Map<String, TagModel>

    private val stories by lazy {
        Pager(
            PagingConfig(
                pageSize = 25,
                prefetchDistance = 50,
                enablePlaceholders = false
            ),
            pagingSourceFactory = component::frontPagePagingSource
        ).flow
            .map { pagingData ->
                if (!this::tagMap.isInitialized)
                    tagMap = component.tagRepository.getFrontPageTags()

                @Suppress("RemoveExplicitTypeArguments")
                pagingData.map { story ->
                    story x tagMap
                }.insertSeparators<FrontPageItem.Story, FrontPageItem> { before, after ->
                    before?.let { b ->
                        after?.let { a ->
                            if (b.frontPageStory.pageIndex == after.frontPageStory.pageIndex - 1 &&
                                b.frontPageStory.pageSubIndex == 24 &&
                                a.frontPageStory.pageSubIndex == 0
                            ) {
                                FrontPageItem.Divider(after.frontPageStory.pageIndex + 1)
//                                null
                            } else {
                                null
                            }
                        }
                    }
                }

            }
            .cachedIn(lifecycleScope) // fine to cache in controller lifecycle
    }

//    private val listAdapter = FrontPageAdapter { shortId, _ ->
//        goto(Destination.Comments(shortId))
//    }

    private val listAdapter2 = FrontPageAdapter2 { shortId, _ ->
        goto(Destination.Comments(shortId))
    }

    override var binding: FrontPageBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedStateBundle: Bundle?
    ): View {
        binding = FrontPageBinding.inflate(inflater, container, false).apply {
            frontPageRecycler.apply {
                adapter = listAdapter2
                layoutManager = LinearLayoutManager(root.context)
                setOnScrollChangeListener { v, _, _, _, _ ->
                    frontPageAppBarLayout.isSelected = v.canScrollVertically(-1)
                }
                addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                    override fun onViewDetachedFromWindow(v: View?) {
                        // listAdapter outlives recycler, so make sure to detach it
                        frontPageRecycler.adapter = null
                    }

                    override fun onViewAttachedToWindow(v: View?) = Unit
                })
            }

            frontPageToolbar.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_item_front_page_settings -> {
                        goto(Destination.Settings)
                        true
                    }
                    else -> super.onOptionsItemSelected(item)
                }
            }

            viewLifecycleOwner.lifecycleScope.launch {

                component.storyRepositoryStatus().collect { status ->
                    frontPageSwipeRefresh.isRefreshing = status.peek() == LoadingStatus.LOADING
                    status.consume {
                        if (it == LoadingStatus.ERROR)
                            Snackbar.make(
                                frontPageCoordinator,
                                "Couldn't reach lobste.rs",
                                Snackbar.LENGTH_SHORT
                            ).show()
                    }
                }
            }

            viewLifecycleOwner.lifecycleScope.launch {
                Log.i("FrontPageController", "Starting collection")

                stories.collectLatest { pagingData ->
                    pagingData.map {
                        Log.i("FrontPageController", "--> Item $it")
                    }
                    Log.i("FrontPageController", "Collected $pagingData")
                    listAdapter2.submitData(pagingData)
                }

                Log.i("FrontPageController", "Done collecting")
            }

            viewLifecycleOwner.lifecycleScope.launch {
                listAdapter2.loadStateFlow.collect {
                    frontPageSwipeRefresh.isRefreshing = it.refresh is LoadState.Loading
                }
            }

//            liveStories.observe(viewLifecycleOwner) {
//                frontPageErrorView.fade(it.isEmpty())
//                frontPageRecycler.fade(it.isNotEmpty())
//                listAdapter.submitList(it)
//            }

            frontPageSwipeRefresh.setOnRefreshListener {
//                liveStories.value?.dataSource?.invalidate()
//                component.frontPagePagingSource.invalidate()
                listAdapter2.refresh()
            }

//            frontPageErrorViewReload.setOnClickListener {
//                liveStories.value?.dataSource?.invalidate()
//            }

            root.setOnApplyWindowInsetsListener { v, insets ->
                v.onApplyWindowInsets(insets)
                insets
            }
        }

        return requireBinding().root
    }
}