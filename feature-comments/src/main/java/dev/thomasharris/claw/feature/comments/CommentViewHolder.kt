package dev.thomasharris.claw.feature.comments

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dev.thomasharris.claw.core.ext.dipToPx
import dev.thomasharris.claw.core.ext.postedAgo
import dev.thomasharris.claw.core.ext.toString
import dev.thomasharris.claw.lib.lobsters.CommentModel
import dev.thomasharris.claw.lib.lobsters.CommentStatus
import java.util.Date
import java.util.Locale
import kotlin.math.min

class CommentViewHolder private constructor(
    private val root: View
) : RecyclerView.ViewHolder(root) {
    private val marker: View = root.findViewById(R.id.comment_marker)
    private val avatar: ImageView = root.findViewById(R.id.comment_avatar)
    private val author: TextView = root.findViewById(R.id.comment_author)
    private val body: TextView = root.findViewById(R.id.comment_body)
    private val collapsedIndicator: ImageView = root.findViewById(R.id.comment_collapsed_indicator)
    private val childCount: TextView = root.findViewById(R.id.comment_child_count)

    private val colors = root.context.resources.getIntArray(R.array.indentation_colors).toList()

    @SuppressLint("SetTextI18n")
    fun bind(comment: CommentModel, position: Int, onClick: (String) -> Unit) {
        marker.backgroundTintList =
            ColorStateList.valueOf(colors[(comment.indentLevel - 1) % colors.size])

        marker.layoutParams = (marker.layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
            leftMargin = (comment.indentLevel - 1) * 8f.dipToPx(root.context).toInt()
        } ?: marker.layoutParams

        root.layoutParams = (root.layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
            topMargin = (if (position != 1 && comment.indentLevel == 1) 12f else 0f)
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

        val isCollapsed = comment.status == CommentStatus.COLLAPSED

        val indicator = if (isCollapsed)
            R.drawable.ic_arrow_drop_down_black_24dp
        else
            R.drawable.ic_arrow_drop_up_black_24dp

        collapsedIndicator.background =
            ContextCompat.getDrawable(collapsedIndicator.context, indicator)

        body.visibility = if (isCollapsed) View.GONE else View.VISIBLE

        childCount.visibility = if (isCollapsed && comment.childCount > 0) View.VISIBLE else View.GONE
        childCount.text = String.format(Locale.US, "%d", comment.childCount)

        root.setOnClickListener {
            onClick(comment.shortId)
        }
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