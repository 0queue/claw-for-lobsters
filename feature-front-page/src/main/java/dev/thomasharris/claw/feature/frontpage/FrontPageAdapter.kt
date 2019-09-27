package dev.thomasharris.claw.feature.frontpage

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
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dev.thomasharris.claw.frontpage.feature.frontpage.R
import java.util.concurrent.TimeUnit

const val VIEW_TYPE_STORY = 1
const val VIEW_TYPE_DIVIDER = 2

class FrontPageAdapter(private val onClick: (String) -> Unit) :
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
            VIEW_TYPE_STORY -> StoryViewHolder(
                inflater.inflate(
                    R.layout.item_front_page_story,
                    parent,
                    false
                ),
                onClick
            )
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
            VIEW_TYPE_STORY -> (holder as StoryViewHolder).bind(getItem(position) as FrontPageItem.Story)
            VIEW_TYPE_DIVIDER -> (holder as DividerViewHolder).bind(getItem(position) as FrontPageItem.Divider)
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
                    return old.shortId == new.shortId
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

class StoryViewHolder(
    private val root: View,
    private val onClick: (String) -> Unit
) : RecyclerView.ViewHolder(root) {
    private val title: TextView = root.findViewById(R.id.item_front_page_title)
    private val byline: TextView = root.findViewById(R.id.item_front_page_author)
    private val avatar: ImageView = root.findViewById(R.id.item_front_page_avatar)

    fun bind(story: FrontPageItem.Story) {
        val context = root.context

        Glide.with(root)
            .load("https://lobste.rs/${story.avatarShortUrl}")
            .circleCrop()
            .into(avatar)

        val titleText = SpannableStringBuilder().apply {
            append(story.title)
            story.tags.forEach { (tag, isMedia) ->
                append(" ")
                append(SpannableString(tag).apply {
                    val span = TagSpan(
                        backgroundColor = context.tagBackgroundColor(tag, isMedia),
                        borderColor = context.tagBorderColor(tag, isMedia)
                    )
                    setSpan(span, 0, tag.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                })
            }
        }

        title.text = titleText

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

        val comments = with(story.commentCount) {
            context.resources.getQuantityString(R.plurals.numberOfComments, this, this)
        }

        val voteCount = String.format("%+d", story.score)

        val bylineText = SpannableStringBuilder().apply {
            append(
                context.getString(
                    R.string.front_page_caption,
                    voteCount,
                    story.submitterUsername,
                    ago,
                    comments
                )
            )
            story.shortUrl()?.let { url ->
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

        byline.text = bylineText
        root.setOnClickListener {
            onClick(story.shortId)
        }
    }
}

fun Context.resolveColor(attr: () -> Int) = TypedValue().run {
    theme.resolveAttribute(attr(), this, true)
    data
}

fun Context.tagBorderColor(tag: String, isMedia: Boolean) = resolveColor {
    when {
        (tag == "show") || (tag == "ask") -> R.attr.colorTagBorderShowAsk
        isMedia -> R.attr.colorTagBorderMedia
        else -> R.attr.colorTagBorder
    }
}

fun Context.tagBackgroundColor(tag: String, isMedia: Boolean) = resolveColor {
    when {
        (tag == "show") || (tag == "ask") -> R.attr.colorTagBackgroundShowAsk
        isMedia -> R.attr.colorTagBackgroundMedia
        else -> R.attr.colorTagBackground
    }
}