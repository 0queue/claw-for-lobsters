package dev.thomasharris.claw.feature.comments

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.thomasharris.claw.core.ui.StoryViewHolder
import dev.thomasharris.claw.lib.lobsters.CommentModel
import dev.thomasharris.claw.lib.lobsters.StoryModel

const val VIEW_TYPE_HEADER = 1
const val VIEW_TYPE_COMMENT = 2
const val VIEW_TYPE_SPACER = 3

sealed class CommentsItem {
    data class Header(val story: StoryModel) : CommentsItem()
    data class Comment(val commentView: CommentModel) : CommentsItem()
    data class Spacer(val isEmpty: Boolean) : CommentsItem()
}

class CommentsAdapter(
    private val onHeaderClick: (String, String) -> Unit,
    private val onLinkClick: (String) -> Unit,
    private val onCommentClick: (String, Boolean) -> Unit,
    private val onLongClick: (String) -> Unit,
) : ListAdapter<CommentsItem, RecyclerView.ViewHolder>(DIFF) {
    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is CommentsItem.Header -> VIEW_TYPE_HEADER
        is CommentsItem.Comment -> VIEW_TYPE_COMMENT
        is CommentsItem.Spacer -> VIEW_TYPE_SPACER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        VIEW_TYPE_HEADER -> StoryViewHolder.inflate(parent)
        VIEW_TYPE_COMMENT -> CommentViewHolder.inflate(parent)
        else -> SpacerViewHolder.inflate(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            VIEW_TYPE_HEADER -> {
                val header = getItem(position) as CommentsItem.Header
                val listener = if (header.story.url.isNotBlank()) onHeaderClick else null
                (holder as StoryViewHolder).bind(
                    header.story,
                    isCompact = false,
                    onClickListener = listener,
                    onLongClickListener = onLongClick,
                    onLinkClicked = onLinkClick,
                )
            }
            VIEW_TYPE_COMMENT -> {
                val comment = getItem(position) as CommentsItem.Comment
                (holder as CommentViewHolder).bind(
                    comment.commentView,
                    position = position,
                    onClick = onCommentClick,
                    onLinkClicked = onLinkClick,
                    onLongClick = onLongClick,
                )
            }
            VIEW_TYPE_SPACER -> {
                val spacer = getItem(position) as CommentsItem.Spacer
                (holder as SpacerViewHolder).bind(spacer)
            }
        }
    }
}

object DIFF : DiffUtil.ItemCallback<CommentsItem>() {
    override fun areContentsTheSame(oldItem: CommentsItem, newItem: CommentsItem): Boolean {
        (oldItem as? CommentsItem.Header)?.let { old ->
            (newItem as? CommentsItem.Header)?.let { new ->
                return old == new
            }
        }

        (oldItem as? CommentsItem.Comment)?.let { old ->
            (newItem as? CommentsItem.Comment)?.let { new ->
                return old == new
            }
        }

        (oldItem as? CommentsItem.Spacer)?.let { old ->
            (newItem as? CommentsItem.Spacer)?.let { new ->
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

        (oldItem as? CommentsItem.Spacer)?.let { old ->
            (newItem as? CommentsItem.Spacer)?.let { new ->
                return old.isEmpty == new.isEmpty
            }
        }

        return false
    }
}

class SpacerViewHolder(private val root: TextView) : RecyclerView.ViewHolder(root) {
    fun bind(spacer: CommentsItem.Spacer) {
        root.text = if (spacer.isEmpty) "No comments" else ""
    }

    companion object {
        fun inflate(parent: ViewGroup) =
            SpacerViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_spacer,
                    parent,
                    false
                ) as TextView
            )
    }
}
