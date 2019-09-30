package dev.thomasharris.claw.core.ui

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.StyleSpan
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dev.thomasharris.claw.core.R
import dev.thomasharris.claw.core.ext.postedAgo
import dev.thomasharris.claw.core.ext.shortUrl
import dev.thomasharris.claw.lib.lobsters.FrontPageStory
import dev.thomasharris.claw.lib.lobsters.FrontPageTag
import java.util.concurrent.TimeUnit

class StoryViewHolder private constructor(private val root: View) : RecyclerView.ViewHolder(root) {
    private val context: Context = root.context
    private val title: TextView = root.findViewById(R.id.story_view_title)
    private val author: TextView = root.findViewById(R.id.story_view_author)
    private val avatar: ImageView = root.findViewById(R.id.story_view_avatar)
    private val description: TextView = root.findViewById(R.id.story_view_description)

    fun bind(
        story: FrontPageStory,
        tags: List<FrontPageTag>,
        isCompact: Boolean = true,
        onClickListener: ((String) -> Unit)? = null
    ) {
        Glide.with(context)
            .load("https://lobste.rs/${story.avatarShortUrl}")
            .circleCrop()
            .into(avatar)

        title.text = SpannableStringBuilder().apply {
            append(story.title)
            tags.forEach {
                append(" ")
                append(SpannableString(it.tag).apply {
                    val span = TagSpan(
                        backgroundColor = context.tagBackgroundColor(it.tag, it.isMedia),
                        borderColor = context.tagBorderColor(it.tag, it.isMedia)
                    )
                    setSpan(span, 0, it.tag.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                })
            }
            if (story.description.isNotBlank()) append(" ☶")
        }

        author.text = SpannableStringBuilder().apply {
            val ago = story.postedAgo().toString(context)
            val numComments = context.resources.getQuantityString(
                R.plurals.numberOfComments,
                story.commentCount,
                story.commentCount
            )

            val numVotes = String.format("%+d", story.score)

            append(
                context.getString(
                    R.string.story_view_author,
                    numVotes,
                    story.submitterUsername,
                    ago,
                    numComments
                )
            )

            Log.i("TEH", "${story.shortUrl()} ${story.shortUrl()?.length}")
            story.shortUrl()?.let { url ->
                append(" | ")
                append(SpannableStringBuilder(url).apply {
                    setSpan(
                        StyleSpan(Typeface.ITALIC),
                        0,
                        url.length,
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                    )
                })
            }
        }
        author.ellipsize = if (isCompact) TextUtils.TruncateAt.END else null
        author.maxLines = if (isCompact) 1 else Int.MAX_VALUE

        if (onClickListener != null)
            root.setOnClickListener {
                onClickListener(story.shortId)
            }

        val shouldShowDescription = !isCompact && story.description.isNotBlank()
        description.visibility = if (shouldShowDescription) View.VISIBLE else View.GONE
        if (shouldShowDescription) description.text = HtmlCompat.fromHtml(
            story.description,
            HtmlCompat.FROM_HTML_MODE_LEGACY
        ).trimEnd()
    }

    companion object {
        fun inflate(context: Context, parent: ViewGroup) = StoryViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.story_view,
                parent,
                false
            )
        )
    }
}

private fun Context.resolveColor(attr: () -> Int) = TypedValue().run {
    theme.resolveAttribute(attr(), this, true)
    data
}

private fun Context.tagBorderColor(tag: String, isMedia: Boolean) = resolveColor {
    when {
        (tag == "show") || (tag == "ask") -> R.attr.colorTagBorderShowAsk
        isMedia -> R.attr.colorTagBorderMedia
        else -> R.attr.colorTagBorder
    }
}

private fun Context.tagBackgroundColor(tag: String, isMedia: Boolean) = resolveColor {
    when {
        (tag == "show") || (tag == "ask") -> R.attr.colorTagBackgroundShowAsk
        isMedia -> R.attr.colorTagBackgroundMedia
        else -> R.attr.colorTagBackground
    }
}

private fun Pair<Long, TimeUnit>.toString(context: Context): String {
    val t = first.toInt()
    return when (val unit = second) {
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