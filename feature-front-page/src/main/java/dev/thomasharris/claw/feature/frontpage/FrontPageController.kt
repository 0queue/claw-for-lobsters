package dev.thomasharris.claw.feature.frontpage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagedList
import androidx.paging.toLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import dev.thomasharris.claw.core.HasBinding
import dev.thomasharris.claw.core.ext.fade
import dev.thomasharris.claw.core.ext.getComponent
import dev.thomasharris.claw.core.ext.observe
import dev.thomasharris.claw.core.ui.ViewLifecycleController
import dev.thomasharris.claw.feature.frontpage.di.DaggerFrontPageComponent
import dev.thomasharris.claw.feature.frontpage.di.FrontPageComponent
import dev.thomasharris.claw.feature.frontpage.di.FrontPageModule
import dev.thomasharris.claw.frontpage.feature.frontpage.R
import dev.thomasharris.claw.frontpage.feature.frontpage.databinding.FrontPageBinding
import dev.thomasharris.claw.lib.lobsters.LoadingStatus
import dev.thomasharris.claw.lib.navigator.Destination
import dev.thomasharris.claw.lib.navigator.goto
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@Suppress("unused")
class FrontPageController : ViewLifecycleController(), HasBinding<FrontPageBinding> {

    private val component by getComponent<FrontPageComponent> {
        DaggerFrontPageComponent.builder()
            .singletonComponent(it)
            .frontPageModule(FrontPageModule())
            .build()
    }

    private val liveStories by lazy {
        val config = PagedList.Config.Builder()
            .setPageSize(25)
            // to mitigate stopping while flinging, although a larger story card will help too
            .setPrefetchDistance(50)
            .build()

        // TODO creates once, so the lifecycleowner never changes...
        component.storyDataSourceFactoryFactory().create(viewLifecycleOwner.lifecycleScope)
            .toLiveData(config)
    }

    private val listAdapter = FrontPageAdapter { shortId, _ ->
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
                adapter = listAdapter
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

            /*job = */
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

            liveStories.observe(viewLifecycleOwner) {
                frontPageErrorView.fade(it.isEmpty())
                frontPageRecycler.fade(it.isNotEmpty())
                listAdapter.submitList(it)
            }

            frontPageSwipeRefresh.setOnRefreshListener {
                liveStories.value?.dataSource?.invalidate()
            }

            frontPageErrorViewReload.setOnClickListener {
                liveStories.value?.dataSource?.invalidate()
            }

            root.setOnApplyWindowInsetsListener { v, insets ->
                v.onApplyWindowInsets(insets)
                insets
            }
        }

        return requireBinding().root
    }
}