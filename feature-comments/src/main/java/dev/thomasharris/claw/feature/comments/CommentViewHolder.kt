package dev.thomasharris.claw.feature.comments

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dev.thomasharris.claw.core.ext.dipToPx
import dev.thomasharris.claw.core.ext.postedAgo
import dev.thomasharris.claw.core.ext.toString
import dev.thomasharris.claw.lib.lobsters.CommentModel
import java.util.Date
import kotlin.math.min

class CommentViewHolder private constructor(
    private val root: View
) : RecyclerView.ViewHolder(root) {
    private val marker: View = root.findViewById(R.id.comment_marker)
    private val avatar: ImageView = root.findViewById(R.id.comment_avatar)
    private val author: TextView = root.findViewById(R.id.comment_author)
    private val body: TextView = root.findViewById(R.id.comment_body)

    private val colors = root.context.resources.getIntArray(R.array.indentation_colors).toList()

    @SuppressLint("SetTextI18n")
    fun bind(comment: CommentModel, position: Int) {
        marker.backgroundTintList =
            ColorStateList.valueOf(colors[(comment.indentLevel - 1) % colors.size])

        marker.layoutParams = (marker.layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
            leftMargin = (comment.indentLevel - 1) * 8f.dipToPx(root.context).toInt()
        } ?: marker.layoutParams

        root.layoutParams = (root.layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
            topMargin = (if (position != 1 && comment.indentLevel == 1) 16f else 0f)
                .dipToPx(root.context).toInt()
        } ?: root.layoutParams

        Glide.with(root)
            .load("https://lobste.rs/${comment.avatarShortUrl}")
            .circleCrop()
            .into(avatar)

        val t = Date(min(comment.createdAt.time, comment.updatedAt.time)).postedAgo()
        val action = if (comment.createdAt != comment.updatedAt) " edited" else ""
        author.text = "${comment.username} $action ${t.toString(root.context)}"

        body.text = HtmlCompat.fromHtml(comment.comment, HtmlCompat.FROM_HTML_MODE_LEGACY).trimEnd()
        body.movementMethod = LinkMovementMethod.getInstance()
    }

    companion object {
        fun inflate(parent: ViewGroup) =
            CommentViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_comment,
                    parent,
                    false
                )
            )
    }
}