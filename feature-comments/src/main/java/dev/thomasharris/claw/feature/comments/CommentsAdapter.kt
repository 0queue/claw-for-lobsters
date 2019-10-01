package dev.thomasharris.claw.feature.comments

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.thomasharris.claw.core.ui.StoryViewHolder
import dev.thomasharris.claw.lib.lobsters.CommentModel
import dev.thomasharris.claw.lib.lobsters.StoryModel
import dev.thomasharris.claw.lib.lobsters.TagModel

const val VIEW_TYPE_HEADER = 1
const val VIEW_TYPE_COMMENT = 2

sealed class CommentsItem {
    data class Header(val story: StoryModel, val tags: List<TagModel>) : CommentsItem()
    data class Comment(val commentView: CommentModel) : CommentsItem()
}


class CommentsAdapter : ListAdapter<CommentsItem, RecyclerView.ViewHolder>(DIFF) {
    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is CommentsItem.Header -> VIEW_TYPE_HEADER
        is CommentsItem.Comment -> VIEW_TYPE_COMMENT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        VIEW_TYPE_HEADER -> StoryViewHolder.inflate(
            parent
        )
        else -> CommentViewHolder.inflate(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            VIEW_TYPE_HEADER -> {
                val (story, tags) = getItem(position) as CommentsItem.Header
                (holder as StoryViewHolder).bind(story, tags, isCompact = false)
            }
            VIEW_TYPE_COMMENT -> {
                val comment = getItem(position) as CommentsItem.Comment
                (holder as CommentViewHolder).bind(comment.commentView, position)
            }
        }
    }

}

val DIFF = object : DiffUtil.ItemCallback<CommentsItem>() {
    override fun areContentsTheSame(oldItem: CommentsItem, newItem: CommentsItem): Boolean {
        (oldItem as? CommentsItem.Header)?.let { old ->
            (newItem as? CommentsItem.Header)?.let { new ->
                // TODO is probably not working correctly
                //  probably should cast to the Impl
                return old == new
            }
        }

        (oldItem as? CommentsItem.Comment)?.let { old ->
            (newItem as? CommentsItem.Comment)?.let { new ->
                // TODO is probably not working correctly
                return old == new
            }
        }

        return false
    }

    override fun areItemsTheSame(oldItem: CommentsItem, newItem: CommentsItem): Boolean {
        (oldItem as? CommentsItem.Header)?.let { old ->
            (newItem as? CommentsItem.Header)?.let { new ->
                return old.story.shortId == new.story.shortId
            }
        }

        (oldItem as? CommentsItem.Comment)?.let { old ->
            (newItem as? CommentsItem.Comment)?.let { new ->
                return old.commentView.shortId == new.commentView.shortId
            }
        }

        return false
    }

}
