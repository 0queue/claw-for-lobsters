package dev.thomasharris.feature.frontpage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.paging.toLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bluelinelabs.conductor.archlifecycle.LifecycleController
import com.google.android.material.appbar.AppBarLayout
import dev.thomasharris.claw.core.getComponent
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

    private lateinit var recycler: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var appBarLayout: AppBarLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        val root = inflater.inflate(R.layout.front_page, container, false)
        recycler = root.findViewById(R.id.front_page_recycler)
        swipeRefreshLayout = root.findViewById(R.id.front_page_swipe_refresh)
        appBarLayout = root.findViewById(R.id.front_page_app_bar_layout)

        recycler.apply {
            adapter = listAdapter
            layoutManager = LinearLayoutManager(root.context)
            setOnScrollChangeListener { v, _, _, _, _ ->
                appBarLayout.isSelected = v.canScrollVertically(-1)
            }
        }

        (activity as AppCompatActivity?)?.setSupportActionBar(root.findViewById(R.id.front_page_toolbar))

        liveStories.observe(this, Observer {
            listAdapter.submitList(it)
            swipeRefreshLayout.isRefreshing = false
        })

        swipeRefreshLayout.setOnRefreshListener {
            liveStories.value?.dataSource?.invalidate()
        }

        return root
    }
}