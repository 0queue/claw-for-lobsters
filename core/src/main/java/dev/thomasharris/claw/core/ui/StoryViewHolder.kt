package dev.thomasharris.claw.core.ui

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.StyleSpan
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import coil.transform.CircleCropTransformation
import dev.thomasharris.claw.core.R
import dev.thomasharris.claw.core.ext.postedAgo
import dev.thomasharris.claw.core.ext.toString
import dev.thomasharris.claw.core.ui.betterlinks.PressableLinkMovementMethod
import dev.thomasharris.claw.core.ui.betterlinks.replaceUrlSpans
import dev.thomasharris.claw.lib.lobsters.StoryWithTagsModel
import dev.thomasharris.claw.lib.lobsters.shortUrl

class StoryViewHolder private constructor(private val root: View) : RecyclerView.ViewHolder(root) {
    private val context: Context = root.context
    private val title: TextView = root.findViewById(R.id.story_view_title)
    private val author: TextView = root.findViewById(R.id.story_view_author)
    private val avatar: ImageView = root.findViewById(R.id.story_view_avatar)
    private val description: TextView = root.findViewById(R.id.story_view_description)

    fun bind(
        story: StoryWithTagsModel,
        isCompact: Boolean = true,
        onClickListener: ((String, String) -> Unit)? = null,
        onLinkClicked: ((String) -> Unit)? = null
    ) {
        avatar.load("https://lobste.rs/${story.avatarShortUrl}") {
            crossfade(true)
            placeholder(R.drawable.ic_person_black_24dp)
            transformations(CircleCropTransformation())
        }

        title.text = SpannableStringBuilder().apply {
            append(story.title)
            story.tags.forEach {
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
            val ago = story.createdAt.postedAgo().toString(context)
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
                    story.username,
                    ago,
                    numComments
                )
            )

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
                onClickListener(story.shortId, story.url)
            }

        val shouldShowDescription = !isCompact && story.description.isNotBlank()
        description.visibility = if (shouldShowDescription) View.VISIBLE else View.GONE
        if (shouldShowDescription) {
            description.text = HtmlCompat.fromHtml(
                story.description,
                HtmlCompat.FROM_HTML_MODE_LEGACY
            ).replaceUrlSpans().trimEnd()
            description.movementMethod = PressableLinkMovementMethod {
                if (it != null)
                    onLinkClicked?.invoke(it)
            }
        }
    }

    companion object {
        fun inflate(parent: ViewGroup) = StoryViewHolder(
            LayoutInflater.from(parent.context).inflate(
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