package dev.thomasharris.claw.feature.frontpage.paging3

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dev.thomasharris.claw.core.ui.StoryViewHolder
import dev.thomasharris.claw.feature.frontpage.DividerViewHolder
import dev.thomasharris.claw.feature.frontpage.FrontPageItem
import dev.thomasharris.claw.feature.frontpage.VIEW_TYPE_DIVIDER
import dev.thomasharris.claw.feature.frontpage.VIEW_TYPE_STORY
import dev.thomasharris.claw.frontpage.feature.frontpage.R

class FrontPageAdapter2(
    private val onClick: (String, String) -> Unit
) : PagingDataAdapter<FrontPageItem, RecyclerView.ViewHolder>(this) {

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
        when (val item = getItem(position)) {
            is FrontPageItem.Story -> {
                (holder as StoryViewHolder).bind(item.frontPageStory, item.tags, onClickListener = onClick)
            }
            is FrontPageItem.Divider -> (holder as DividerViewHolder).bind(getItem(position) as FrontPageItem.Divider)
            null -> {
                // TODO
                Log.i("FrontPageAdapter2", "Does not support null yet...")
            }
        }
    }

    companion object : DiffUtil.ItemCallback<FrontPageItem>() {
        override fun areContentsTheSame(oldItem: FrontPageItem, newItem: FrontPageItem): Boolean {
            (oldItem as? FrontPageItem.Story)?.let { old ->
                (newItem as? FrontPageItem.Story)?.let { new ->
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