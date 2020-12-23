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
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import dev.thomasharris.betterhtml.PressableLinkMovementMethod
import dev.thomasharris.betterhtml.fromHtml
import dev.thomasharris.claw.core.R
import dev.thomasharris.claw.core.databinding.StoryViewBinding
import dev.thomasharris.claw.core.ext.dipToPx
import dev.thomasharris.claw.core.ext.postedAgo
import dev.thomasharris.claw.core.ext.toString
import dev.thomasharris.claw.lib.lobsters.StoryModel
import org.threeten.bp.DateTimeUtils
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import java.net.URI
import java.util.Date

class StoryViewHolder private constructor(
    private val binding: StoryViewBinding,
) : RecyclerView.ViewHolder(binding.root) {

    private val context: Context = binding.root.context

    fun bind(
        story: StoryModel,
        isCompact: Boolean = true,
        onClickListener: ((String, String) -> Unit)? = null,
        onLongClickListener: ((String) -> Unit)? = null,
        onLinkClicked: ((String) -> Unit)? = null,
    ) = with(binding) {
        storyViewAvatar.load("https://lobste.rs/${story.avatarShortUrl}") {
            crossfade(true)
            placeholder(R.drawable.ic_person_black_24dp)
            transformations(CircleCropTransformation())
        }

        storyViewTitle.text = SpannableStringBuilder().apply {
            append(story.title)
            story.tags.forEach { tag ->
                append(" ")
                append(
                    SpannableString(tag).apply {
                        val span = TagSpan(
                            backgroundColor = context.tagBackground(tag),
                            borderColor = context.tagBorder(tag)
                        )
                        setSpan(span, 0, tag.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                    }
                )
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
            storyViewAuthor.text = it
        }

        storyViewAuthor.ellipsize = if (isCompact) TextUtils.TruncateAt.END else null
        storyViewAuthor.maxLines = if (isCompact) 1 else Int.MAX_VALUE

        if (onClickListener != null)
            root.setOnClickListener {
                onClickListener(story.shortId, story.url)
            }

        if (onLongClickListener != null)
            root.setOnLongClickListener {
                // TODO haptic/audio feedback?
                onLongClickListener(story.username)
                true
            }

        val shouldShowDescription = !isCompact && story.description.isNotBlank()
        storyViewDescription.visibility = if (shouldShowDescription) View.VISIBLE else View.GONE
        if (shouldShowDescription) {
            storyViewDescription.text = story.description
                .fromHtml(dipToPx = { it.dipToPx(root.context) })
                .trim()
            storyViewDescription.movementMethod =
                PressableLinkMovementMethod {
                    if (it != null)
                        onLinkClicked?.invoke(it)
                }
        }
    }

    companion object {
        fun inflate(parent: ViewGroup) = StoryViewHolder(
            StoryViewBinding.inflate(
                LayoutInflater.from(parent.context),
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
