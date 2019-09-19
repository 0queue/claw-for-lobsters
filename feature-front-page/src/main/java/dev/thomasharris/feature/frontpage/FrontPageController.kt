package dev.thomasharris.feature.frontpage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bluelinelabs.conductor.Controller
import com.google.android.material.appbar.AppBarLayout

@Suppress("unused")
class FrontPageController : Controller() {

    private lateinit var recycler: RecyclerView
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var listAdapter: ItemAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        val root = inflater.inflate(R.layout.front_page, container, false)
        recycler = root.findViewById(R.id.front_page_recycler)
        appBarLayout = root.findViewById(R.id.front_page_app_bar_layout)
        listAdapter = ItemAdapter()

        recycler.apply {
            adapter = listAdapter
            layoutManager = LinearLayoutManager(this@FrontPageController.activity)
            setOnScrollChangeListener { v, _, _, _, _ ->
                appBarLayout.isSelected = v.canScrollVertically(-1)
            }
        }

        listAdapter.submitList((1..30).map { Item("Title number $it") })

        (activity as AppCompatActivity?)?.setSupportActionBar(root.findViewById(R.id.front_page_toolbar))

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