package dev.thomasharris.claw.core.ui

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import coil.transform.CircleCropTransformation
import dev.thomasharris.claw.core.R
import dev.thomasharris.claw.core.ext.dipToPx
import dev.thomasharris.claw.core.ext.postedAgo
import dev.thomasharris.claw.core.ext.toString
import dev.thomasharris.claw.core.ui.betterhtml.PressableLinkMovementMethod
import dev.thomasharris.claw.core.ui.betterhtml.fromHtml
import dev.thomasharris.claw.lib.lobsters.StoryModel
import org.threeten.bp.DateTimeUtils
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import java.net.URI
import java.util.Date

class StoryViewHolder private constructor(private val root: View) : RecyclerView.ViewHolder(root) {
    private val context: Context = root.context
    private val title: TextView = root.findViewById(R.id.story_view_title)
    private val author: TextView = root.findViewById(R.id.story_view_author)
    private val avatar: ImageView = root.findViewById(R.id.story_view_avatar)
    private val description: TextView = root.findViewById(R.id.story_view_description)

    fun bind(
        story: StoryModel,
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
            story.tags.forEach { tag ->
                append(" ")
                append(SpannableString(tag).apply {
                    val span = TagSpan(
                        backgroundColor = context.tagBackground(tag),
                        borderColor = context.tagBorder(tag)
                    )
                    setSpan(span, 0, tag.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                })
            }
            if (story.description.isNotBlank()) append(" ☶")
        }

        val ago = story.createdAt.postedAgo().toString(context)
        val numComments = context.resources.getQuantityString(
            R.plurals.numberOfComments,
            story.commentCount,
            story.commentCount
        )

        val numVotes = String.format("%+d", story.score)
        buildSpannedString {
            append(numVotes)
            append(" | ")
            append("by ") // TODO change after getting PR accepted
            if (story.userCreatedAt.isNewUser()) append(
                story.username,
                // probably doesn't have to be an attribute because it doesn't change with theme
                ForegroundColorSpan(ContextCompat.getColor(root.context, R.color.new_author)),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            else append(story.username)
            append(" $ago")
            append(" $numComments")
            story.shortUrl()?.let {
                append(" | ")
                append(it, StyleSpan(Typeface.ITALIC), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }.let {
            author.text = it
        }

//        author.text = SpannableStringBuilder().apply {
//            val ago = story.createdAt.postedAgo().toString(context)
//            val numComments = context.resources.getQuantityString(
//                R.plurals.numberOfComments,
//                story.commentCount,
//                story.commentCount
//            )
//
//            val numVotes = String.format("%+d", story.score)
//
//            append(
//                context.getString(
//                    R.string.story_view_author,
//                    numVotes,
//                    story.username,
//                    ago,
//                    numComments
//                )
//            )
//
//            story.shortUrl()?.let { url ->
//                append(" | ")
//                append(SpannableStringBuilder(url).apply {
//                    setSpan(
//                        StyleSpan(Typeface.ITALIC),
//                        0,
//                        url.length,
//                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE
//                    )
//                })
//            }
//        }
        author.ellipsize = if (isCompact) TextUtils.TruncateAt.END else null
        author.maxLines = if (isCompact) 1 else Int.MAX_VALUE

        if (onClickListener != null)
            root.setOnClickListener {
                onClickListener(story.shortId, story.url)
            }

        val shouldShowDescription = !isCompact && story.description.isNotBlank()
        description.visibility = if (shouldShowDescription) View.VISIBLE else View.GONE
        if (shouldShowDescription) {
            description.text = story.description
                .fromHtml(dipToPx = { it.dipToPx(root.context) })
                .trim()
            description.movementMethod =
                PressableLinkMovementMethod {
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

fun StoryModel.shortUrl() = URI(url.trim()).host?.removePrefix("www.")

fun Date.isNewUser(asOf: Instant = Instant.now()): Boolean {
    return Duration.between(DateTimeUtils.toInstant(this), asOf)
        .toDays() <= 70 // user.rb#NEW_USER_DAYS
}