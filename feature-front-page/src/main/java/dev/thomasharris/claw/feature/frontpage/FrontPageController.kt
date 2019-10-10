package dev.thomasharris.claw.feature.frontpage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
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
import dev.thomasharris.claw.core.ext.getComponent
import dev.thomasharris.claw.core.ext.observe
import dev.thomasharris.claw.feature.frontpage.di.DaggerFrontPageComponent
import dev.thomasharris.claw.feature.frontpage.di.FrontPageComponent
import dev.thomasharris.claw.feature.frontpage.di.FrontPageModule
import dev.thomasharris.claw.frontpage.feature.frontpage.R
import dev.thomasharris.claw.lib.lobsters.LoadingStatus
import dev.thomasharris.claw.lib.navigator.Destination
import dev.thomasharris.claw.lib.navigator.goto
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@Suppress("unused")
class FrontPageController : LifecycleController() {

    private val component by getComponent<FrontPageComponent> {
        DaggerFrontPageComponent.builder()
            .singletonComponent(it)
            .frontPageModule(FrontPageModule(lifecycleScope))
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

    private val listAdapter = FrontPageAdapter { shortId, url ->
        goto(Destination.Comments(shortId, url))
    }

    private lateinit var toolbar: Toolbar

    private lateinit var recycler: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var errorView: LinearLayout
    private lateinit var errorReload: MaterialButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
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

        lifecycleScope.launch {
            component.storyRepositoryStatus().collect { status ->
                swipeRefreshLayout.isRefreshing = status.peek() == LoadingStatus.LOADING
                status.consume {
                    // TODO snack time, general UI improvements
                    if (it == LoadingStatus.ERROR)
                        Toast.makeText(
                            activity,
                            "Could not reach lobste.rs",
                            Toast.LENGTH_SHORT
                        ).show()
                }
            }
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