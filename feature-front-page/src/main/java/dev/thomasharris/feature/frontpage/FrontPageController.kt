package dev.thomasharris.feature.frontpage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.paging.toLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bluelinelabs.conductor.archlifecycle.LifecycleController
import com.google.android.material.appbar.AppBarLayout
import dev.thomasharris.claw.core.getComponent
import dev.thomasharris.feature.frontpage.di.DaggerFrontPageComponent
import dev.thomasharris.feature.frontpage.di.FrontPageComponent
import dev.thomasharris.feature.frontpage.di.FrontPageModule
import dev.thomasharris.lib.lobsters.StoryDatabaseEntity

@Suppress("unused")
class FrontPageController : LifecycleController() {

    private val component by getComponent<FrontPageComponent> {
        DaggerFrontPageComponent.builder()
            .singletonComponent(it)
            .frontPageModule(FrontPageModule())
            .build()
    }

    private lateinit var recycler: RecyclerView
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var listAdapter: ItemAdapter2

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        val root = inflater.inflate(R.layout.front_page, container, false)
        recycler = root.findViewById(R.id.front_page_recycler)
        appBarLayout = root.findViewById(R.id.front_page_app_bar_layout)
        listAdapter = ItemAdapter2()

        recycler.apply {
            adapter = listAdapter
            layoutManager = LinearLayoutManager(this@FrontPageController.activity)
            setOnScrollChangeListener { v, _, _, _, _ ->
                appBarLayout.isSelected = v.canScrollVertically(-1)
            }
        }

//        listAdapter.submitList((1..30).map { Item("Title number $it") })

        (activity as AppCompatActivity?)?.setSupportActionBar(root.findViewById(R.id.front_page_toolbar))


        val config = PagedList.Config.Builder()
            .setPageSize(25)
            .build()

        // TODO this is all very annoying and probably bad.
        //  instead, create my own data source that fetches
        //  pages from a repository with getPage(index) or
        //  similar, and that's it.  the repository will
        //  worry about picking database or network, and
        //  keeping the db up to date
        val boundaryCallback = object : PagedList.BoundaryCallback<StoryDatabaseEntity>() {
            override fun onItemAtEndLoaded(itemAtEnd: StoryDatabaseEntity) {
                super.onItemAtEndLoaded(itemAtEnd)

                // TODO load more here
                // repository.load(itemAtEnd.pageIndex + 1)
            }
        }

        component.queryDataSourceFactory().toLiveData(config, initialLoadKey = 0, boundaryCallback = boundaryCallback).observe(this, Observer {
            listAdapter.submitList(it)
        })

        return root
    }

}

data class Item(val text: String) {
    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Item>() {
            override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
                return oldItem.text == newItem.text
            }

            override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
                return oldItem === newItem
            }

        }
    }
}

class ItemViewHolder(root: View) : RecyclerView.ViewHolder(root) {
    val textView: TextView = root.findViewById(R.id.item_front_page_text)
}

class ItemAdapter : ListAdapter<Item, ItemViewHolder>(Item.DIFF_CALLBACK) {
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.textView.text = getItem(position)?.text ?: "$position is null!"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ItemViewHolder(inflater.inflate(R.layout.item_front_page, parent, false))
    }

}

val DIFF = object : DiffUtil.ItemCallback<StoryDatabaseEntity>() {
    override fun areItemsTheSame(
        oldItem: StoryDatabaseEntity,
        newItem: StoryDatabaseEntity
    ) = oldItem === newItem

    override fun areContentsTheSame(
        oldItem: StoryDatabaseEntity,
        newItem: StoryDatabaseEntity
    ) = oldItem.shortId == newItem.shortId
}

class ItemAdapter2 : PagedListAdapter<StoryDatabaseEntity, ItemViewHolder>(DIFF) {
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.textView.text = getItem(position)?.title ?: "$position is null"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ItemViewHolder(inflater.inflate(R.layout.item_front_page, parent, false))
    }

}