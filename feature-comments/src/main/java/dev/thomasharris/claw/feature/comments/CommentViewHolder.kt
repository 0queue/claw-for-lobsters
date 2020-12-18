package dev.thomasharris.claw.feature.comments

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import dev.thomasharris.betterhtml.fromHtml
import dev.thomasharris.claw.core.ext.dipToPx
import dev.thomasharris.claw.core.ext.postedAgo
import dev.thomasharris.claw.core.ext.toString
import dev.thomasharris.claw.core.ui.isNewUser
import dev.thomasharris.claw.feature.comments.databinding.ItemCommentBinding
import dev.thomasharris.claw.lib.lobsters.CommentModel
import dev.thomasharris.claw.lib.lobsters.CommentStatus
import java.util.Date
import kotlin.math.min

class CommentViewHolder private constructor(
    private val binding: ItemCommentBinding
) : RecyclerView.ViewHolder(binding.root) {

    private val colors =
        binding.root.context.resources.getIntArray(R.array.indentation_colors).toList()

    @SuppressLint("SetTextI18n")
    fun bind(
        comment: CommentModel,
        position: Int,
        onClick: (String, Boolean) -> Unit,
        onLinkClicked: (String) -> Unit
    ) = with(binding) {
        commentMarker.backgroundTintList =
            ColorStateList.valueOf(colors[(comment.indentLevel - 1) % colors.size])

        commentMarker.layoutParams =
            (commentMarker.layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
            leftMargin = (comment.indentLevel - 1) * 8f.dipToPx(root.context).toInt()
        } ?: commentMarker.layoutParams

        root.layoutParams = (root.layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
            topMargin = (if (position != 1 && comment.indentLevel == 1) 12f else 0f)
                .dipToPx(root.context).toInt()
        } ?: root.layoutParams

        commentAvatar.load("https://lobste.rs/${comment.avatarShortUrl}") {
            crossfade(true)
            placeholder(R.drawable.ic_person_black_24dp)
            transformations(CircleCropTransformation())
        }

        val t = Date(min(comment.createdAt.time, comment.updatedAt.time)).postedAgo()
        val action = if (comment.createdAt != comment.updatedAt) "edited " else ""
        val scoreText = comment.score.let { s ->
            when {
                s < -2 -> " | $s"
                s > 4 -> " | +$s"
                else -> ""
            }
        }

        commentAuthor.text =
            SpannableString("${comment.username} $action${t.toString(root.context)}$scoreText").apply {
                // CAREFUL slightly hardcoded here
                when {
                    comment.username == comment.storyAuthor -> R.color.comment_original_poster
                    comment.userCreatedAt?.isNewUser() == true -> R.color.new_author
                    else -> null
                }?.let { c ->
                    setSpan(
                        ForegroundColorSpan(ContextCompat.getColor(root.context, c)),
                        0,
                        comment.username.length,
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                    )
                }
            }

        commentBody.text = comment.comment
            .fromHtml(dipToPx = { it.dipToPx(root.context) })
            .trim()
        commentBody.movementMethod =
            dev.thomasharris.betterhtml.PressableLinkMovementMethod {
                if (it != null)
                    onLinkClicked(it)
            }

        val isCollapsed = comment.status == CommentStatus.COLLAPSED

        val indicator = if (isCollapsed)
            R.drawable.ic_arrow_drop_down_black_16dp
        else
            R.drawable.ic_arrow_drop_up_black_16dp

        commentCollapsedIndicator.setImageDrawable(
            ContextCompat.getDrawable(
                commentCollapsedIndicator.context,
                indicator
            )
        )
        commentCollapsedIndicator.setOnClickListener {
            onClick(comment.shortId, true)
        }

        commentBody.visibility = if (isCollapsed) View.GONE else View.VISIBLE

        commentChildCount.visibility =
            if (isCollapsed && comment.childCount > 0) View.VISIBLE else View.GONE
        commentChildCount.text = comment.childCount.toString(10)

        val commentAlpha = if (comment.score < -2) .7f else 1f
        commentContentContainer.alpha = commentAlpha
        commentMarker.alpha = commentAlpha

        root.setOnClickListener {
            onClick(comment.shortId, false)
        }
    }

    companion object {
        fun inflate(parent: ViewGroup) =
            CommentViewHolder(
                ItemCommentBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
    }
}
