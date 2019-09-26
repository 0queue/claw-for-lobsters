package dev.thomasharris.feature.frontpage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.paging.PagedList
import androidx.paging.toLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bluelinelabs.conductor.archlifecycle.LifecycleController
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.button.MaterialButton
import dev.thomasharris.claw.core.ext.getComponent
import dev.thomasharris.claw.core.ext.observe
import dev.thomasharris.feature.frontpage.di.DaggerFrontPageComponent
import dev.thomasharris.feature.frontpage.di.FrontPageComponent
import dev.thomasharris.feature.frontpage.di.FrontPageModule

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
        component.storyDataSourceFactory().toLiveData(config)
    }

    private val listAdapter: StoryAdapter = StoryAdapter()

    private lateinit var toolbar: Toolbar
    private lateinit var toolbarScrollFlags: AppBarLayout.LayoutParams
    private lateinit var toolbarNoScrollFlags: AppBarLayout.LayoutParams

    private lateinit var recycler: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var errorView: LinearLayout
    private lateinit var errorReload: MaterialButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        val root = inflater.inflate(R.layout.front_page, container, false)
        // viewBinding please
        toolbar = root.findViewById(R.id.front_page_toolbar)
        toolbarScrollFlags = toolbar.layoutParams as AppBarLayout.LayoutParams
        toolbarNoScrollFlags = AppBarLayout.LayoutParams(toolbarScrollFlags).apply {
            scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL
        }
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
        }

        (activity as AppCompatActivity?)?.apply {
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayShowTitleEnabled(false)
        }

        component.storyDataSourceFactory().loadingStatus.observe(this) {
            swipeRefreshLayout.isRefreshing = it == LoadingStatus.LOADING
            if (it == LoadingStatus.ERROR)
                errorView.fadeIn()
            else
                errorView.fadeOut()

            toolbar.layoutParams = if (it == LoadingStatus.ERROR)
                toolbarNoScrollFlags
            else
                toolbarScrollFlags
        }

        liveStories.observe(this) {
            listAdapter.submitList(it)
        }

        swipeRefreshLayout.setOnRefreshListener {
            liveStories.value?.dataSource?.invalidate()
        }

        errorReload.setOnClickListener {
            liveStories.value?.dataSource?.invalidate()
        }

        return root
    }
}

fun View.fadeIn() {
    visibility = View.VISIBLE
    animate().apply {
        duration = context.resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
        alpha(1f)
        setListener(null)
    }
}

fun View.fadeOut() {
    if (visibility == View.GONE)
        return

    animate().apply {
        alpha(0f)
        duration = context.resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
        withEndAction {
            visibility = View.GONE
        }
    }
}