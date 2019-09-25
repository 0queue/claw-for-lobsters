package dev.thomasharris.feature.frontpage

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import java.util.concurrent.TimeUnit

class StoryAdapter : PagedListAdapter<FrontPageStory, StoryAdapter.StoryViewHolder>(Companion) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return StoryViewHolder(inflater.inflate(R.layout.item_front_page, parent, false))
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = getItem(position) ?: return
        val context = holder.root.context

        val title = SpannableStringBuilder().apply {
            append(story.title)
            story.tags.forEach { tag ->
                append(" ")
                append(SpannableString(tag.tag).apply {
                    val span = TagSpan(
                        backgroundColor = context.tagBackgroundColor(tag),
                        borderColor = context.tagBorderColor(tag)
                    )
                    setSpan(span, 0, tag.tag.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                })
            }
        }

        holder.title.text = title

        val ago = with(story.postedAgo) {
            val t = first.toInt()
            when (val unit = second) {
                TimeUnit.DAYS -> context.resources.getQuantityString(R.plurals.numberOfDays, t, t)
                TimeUnit.HOURS -> context.resources.getQuantityString(R.plurals.numberOfHours, t, t)
                TimeUnit.MINUTES -> context.resources.getQuantityString(
                    R.plurals.numberOfMinutes,
                    t,
                    t
                )
                else -> throw IllegalStateException("Invalid TimeUnit: $unit")
            }
        }

        val comments = with(story.numComments) {
            context.resources.getQuantityString(R.plurals.numberOfComments, this, this)
        }

        val voteCount = String.format("%+d", story.voteTotal)

        val byline = SpannableStringBuilder().apply {
            append(
                context.getString(
                    R.string.front_page_caption,
                    voteCount,
                    story.username,
                    ago,
                    comments
                )
            )
            story.shortURL?.let { url ->
                append(" | ")
                append(SpannableString(url).apply {
                    setSpan(
                        StyleSpan(Typeface.ITALIC),
                        0,
                        url.length,
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                    )
                })
            }
        }

        holder.byline.text = byline
        holder.root.setOnClickListener {
            Toast.makeText(context, "TODO", Toast.LENGTH_SHORT).show()
        }
    }

    class StoryViewHolder(val root: View) : RecyclerView.ViewHolder(root) {
        val title: TextView = root.findViewById(R.id.item_front_page_title)
        val byline: TextView = root.findViewById(R.id.item_front_page_author)
    }

    companion object : DiffUtil.ItemCallback<FrontPageStory>() {
        override fun areContentsTheSame(oldItem: FrontPageStory, newItem: FrontPageStory) =
            oldItem == newItem

        override fun areItemsTheSame(oldItem: FrontPageStory, newItem: FrontPageStory) =
            oldItem.shortId == newItem.shortId
    }
}

fun Context.resolveColor(attr: () -> Int) = TypedValue().run {
    theme.resolveAttribute(attr(), this, true)
    data
}

fun Context.tagBorderColor(tag: FrontPageTag) = resolveColor {
    when {
        (tag.tag == "show") || (tag.tag == "ask") -> R.attr.colorTagBorderShowAsk
        tag.isMedia -> R.attr.colorTagBorderMedia
        else -> R.attr.colorTagBorder
    }
}

fun Context.tagBackgroundColor(tag: FrontPageTag) = resolveColor {
    when {
        (tag.tag == "show") || (tag.tag == "ask") -> R.attr.colorTagBackgroundShowAsk
        tag.isMedia -> R.attr.colorTagBackgroundMedia
        else -> R.attr.colorTagBackground
    }
}