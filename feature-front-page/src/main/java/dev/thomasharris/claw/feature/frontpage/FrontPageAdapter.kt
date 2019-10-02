package dev.thomasharris.claw.feature.frontpage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dev.thomasharris.claw.core.ui.StoryViewHolder
import dev.thomasharris.claw.frontpage.feature.frontpage.R

const val VIEW_TYPE_STORY = 1
const val VIEW_TYPE_DIVIDER = 2

class FrontPageAdapter(private val onClick: (String, String) -> Unit) :
    PagedListAdapter<FrontPageItem, RecyclerView.ViewHolder>(
        Companion
    ) {

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is FrontPageItem.Story -> VIEW_TYPE_STORY
        else -> VIEW_TYPE_DIVIDER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_STORY -> StoryViewHolder.inflate(parent)
            else -> DividerViewHolder(
                inflater.inflate(
                    R.layout.item_front_page_divider,
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            VIEW_TYPE_STORY -> {
                val (story, tags) = (getItem(position) as FrontPageItem.Story)
                (holder as StoryViewHolder).bind(story, tags, onClickListener = onClick)
            }
            VIEW_TYPE_DIVIDER -> (holder as DividerViewHolder).bind(getItem(position) as FrontPageItem.Divider)
        }
    }

    companion object : DiffUtil.ItemCallback<FrontPageItem>() {
        override fun areContentsTheSame(oldItem: FrontPageItem, newItem: FrontPageItem): Boolean {
            (oldItem as? FrontPageItem.Story)?.let { old ->
                (newItem as? FrontPageItem.Story)?.let { new ->
                    // TODO does this equality check work?
                    //  or after refactoring to use observation
                    //  will it not matter?
                    return old == new
                }
            }

            (oldItem as? FrontPageItem.Divider)?.let { old ->
                (newItem as? FrontPageItem.Divider)?.let { new ->
                    return old == new
                }
            }

            return false
        }

        override fun areItemsTheSame(oldItem: FrontPageItem, newItem: FrontPageItem): Boolean {
            (oldItem as? FrontPageItem.Story)?.let { old ->
                (newItem as? FrontPageItem.Story)?.let { new ->
                    return old.frontPageStory.shortId == new.frontPageStory.shortId
                }
            }

            (oldItem as? FrontPageItem.Divider)?.let { old ->
                (newItem as? FrontPageItem.Divider)?.let { new ->
                    return old.n == new.n
                }
            }

            return false
        }
    }
}

class DividerViewHolder(private val root: View) : RecyclerView.ViewHolder(root) {

    private val label: TextView = root.findViewById(R.id.item_front_page_divider_text)

    fun bind(item: FrontPageItem.Divider) {
        label.text = root.context.getString(R.string.page_number, item.n)
    }
}