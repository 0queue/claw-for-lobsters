package dev.thomasharris.claw.feature.frontpage

import android.os.Bundle
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
import dev.thomasharris.claw.core.HasBinding
import dev.thomasharris.claw.core.ext.fade
import dev.thomasharris.claw.core.ext.getComponent
import dev.thomasharris.claw.core.ui.ViewLifecycleController
import dev.thomasharris.claw.feature.frontpage.di.DaggerFrontPageComponent
import dev.thomasharris.claw.feature.frontpage.di.FrontPageComponent
import dev.thomasharris.claw.feature.frontpage.paging3.FrontPageAdapter2
import dev.thomasharris.claw.frontpage.feature.frontpage.R
import dev.thomasharris.claw.frontpage.feature.frontpage.databinding.FrontPageBinding
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
            .build()
    }

    private val stories by lazy {
        Pager(
            PagingConfig(
                pageSize = 25,
                prefetchDistance = 50,
                enablePlaceholders = false
            ),
            pagingSourceFactory = component::frontPagePagingSource
        ).flow.map { pagingData ->
            @Suppress("RemoveExplicitTypeArguments")
            pagingData
                .map(FrontPageItem::Story)
                .insertSeparators<FrontPageItem.Story, FrontPageItem> { before, after ->
                    before?.let { b ->
                        after?.let { a ->
                            if (b.story.pageIndex == a.story.pageIndex - 1)
                                FrontPageItem.Divider(a.story.pageIndex + 1)
                            else
                                null
                        }
                    }
                }

        }.cachedIn(lifecycleScope) // fine to cache in controller lifecycle
    }

    private val adapter = FrontPageAdapter2 { shortId, _ ->
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
                adapter = this@FrontPageController.adapter
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
                stories.collectLatest { pagingData ->
                    adapter.submitData(pagingData)
                }
            }

            viewLifecycleOwner.lifecycleScope.launch {
                adapter.loadStateFlow.collect { loadStates ->
                    frontPageSwipeRefresh.isRefreshing = loadStates.refresh is LoadState.Loading

                    // little ugly but I want to catch all errors
                    var isError = false
                    loadStates.forEach { _, _, loadState ->
                        isError = isError || loadState is LoadState.Error
                    }

                    frontPageErrorView.fade(isError)
                    frontPageRecycler.fade(!isError)
                }
            }

            frontPageErrorViewReload.setOnClickListener {
                adapter.refresh()
            }

            frontPageSwipeRefresh.setOnRefreshListener {
                adapter.refresh()
            }

            root.setOnApplyWindowInsetsListener { v, insets ->
                v.onApplyWindowInsets(insets)
                insets
            }
        }

        return requireBinding().root
    }
}