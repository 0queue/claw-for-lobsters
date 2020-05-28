package dev.thomasharris.claw.feature.frontpage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagedList
import androidx.paging.toLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bluelinelabs.conductor.archlifecycle.LifecycleController
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import dev.thomasharris.claw.core.ext.fade
import dev.thomasharris.claw.core.ext.getComponent
import dev.thomasharris.claw.core.ext.observe
import dev.thomasharris.claw.feature.frontpage.di.DaggerFrontPageComponent
import dev.thomasharris.claw.feature.frontpage.di.FrontPageComponent
import dev.thomasharris.claw.feature.frontpage.di.FrontPageModule
import dev.thomasharris.claw.frontpage.feature.frontpage.R
import dev.thomasharris.claw.lib.lobsters.LoadingStatus
import dev.thomasharris.claw.lib.navigator.Destination
import dev.thomasharris.claw.lib.navigator.goto
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@Suppress("unused")
class FrontPageController : LifecycleController() {

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
        component.storyDataSourceFactoryFactory().create(lifecycleScope).toLiveData(config)
    }

    private val listAdapter = FrontPageAdapter { shortId, _ ->
        goto(Destination.Comments(shortId))
    }

    private lateinit var job: Job

    private lateinit var toolbar: Toolbar

    private lateinit var recycler: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var errorView: LinearLayout
    private lateinit var errorReload: MaterialButton

    init {
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedStateBundle: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.front_page, container, false)
        // viewBinding please
        toolbar = root.findViewById(R.id.front_page_toolbar)
        recycler = root.findViewById(R.id.front_page_recycler)
        swipeRefreshLayout = root.findViewById(R.id.front_page_swipe_refresh)
        appBarLayout = root.findViewById(R.id.front_page_app_bar_layout)
        errorView = root.findViewById(R.id.front_page_error_view)
        errorReload = root.findViewById(R.id.front_page_error_view_reload)

        recycler.apply {
            adapter = listAdapter
            layoutManager = LinearLayoutManager(root.context)
            setOnScrollChangeListener { v, _, _, _, _ ->
                appBarLayout.isSelected = v.canScrollVertically(-1)
            }
            addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewDetachedFromWindow(v: View?) {
                    // listAdapter outlives recycler, so make sure to detach it
                    recycler.adapter = null
                }

                override fun onViewAttachedToWindow(v: View?) = Unit
            })
        }

        (activity as AppCompatActivity?)?.apply {
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayShowTitleEnabled(false)
        }

        job = lifecycleScope.launch {
            component.storyRepositoryStatus().collect { status ->
                swipeRefreshLayout.isRefreshing = status.peek() == LoadingStatus.LOADING
                status.consume {
                    if (it == LoadingStatus.ERROR)
                        Snackbar.make(
                            root,
                            "Couldn't reach lobste.rs",
                            Snackbar.LENGTH_SHORT
                        ).show()
                }
            }
        }

        liveStories.observe(this) {
            errorView.fade(it.isEmpty())
            recycler.fade(it.isNotEmpty())
            listAdapter.submitList(it)
        }

        swipeRefreshLayout.setOnRefreshListener {
            liveStories.value?.dataSource?.invalidate()
        }

        errorReload.setOnClickListener {
            liveStories.value?.dataSource?.invalidate()
        }

        root.setOnApplyWindowInsetsListener { v, insets ->
            v.onApplyWindowInsets(insets)
            insets
        }

        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_front_page, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.menu_item_front_page_settings -> {
            goto(Destination.Settings)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onDestroyView(view: View) {
        super.onDestroyView(view)
        job.cancel()
    }
}