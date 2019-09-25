package dev.thomasharris.feature.frontpage

import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.paging.toLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bluelinelabs.conductor.archlifecycle.LifecycleController
import com.google.android.material.appbar.AppBarLayout
import dev.thomasharris.claw.core.getComponent
import dev.thomasharris.feature.frontpage.di.DaggerFrontPageComponent
import dev.thomasharris.feature.frontpage.di.FrontPageComponent
import dev.thomasharris.feature.frontpage.di.FrontPageModule
import dev.thomasharris.lib.lobsters.Story

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
    private lateinit var appBarLayout: AppBarLayout

    init {
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_front_page, menu)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        val root = inflater.inflate(R.layout.front_page, container, false)
        recycler = root.findViewById(R.id.front_page_recycler)
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
        })

        return root
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.menu_item_front_page_refresh -> {
            liveStories.value?.dataSource?.invalidate()
            true
        }
        else -> false
    }
}

class StoryViewHolder(root: View) : RecyclerView.ViewHolder(root) {
    val textView: TextView = root.findViewById(R.id.item_front_page_text)
}

val STORY_DIFF = object : DiffUtil.ItemCallback<Story>() {
    override fun areItemsTheSame(
        oldItem: Story,
        newItem: Story
    ) = oldItem.shortId == newItem.shortId

    override fun areContentsTheSame(
        oldItem: Story,
        newItem: Story
    ) = oldItem == newItem
}

class StoryAdapter : PagedListAdapter<Story, StoryViewHolder>(STORY_DIFF) {
    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        holder.textView.text = getItem(position)?.title ?: "$position is null"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return StoryViewHolder(inflater.inflate(R.layout.item_front_page, parent, false))
    }

}