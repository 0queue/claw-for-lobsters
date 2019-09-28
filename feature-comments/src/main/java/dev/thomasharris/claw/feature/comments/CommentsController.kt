package dev.thomasharris.claw.feature.comments

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bluelinelabs.conductor.archlifecycle.LifecycleController
import com.google.android.material.appbar.AppBarLayout
import dev.thomasharris.claw.core.ext.getComponent
import dev.thomasharris.claw.feature.comments.di.CommentsComponent
import dev.thomasharris.claw.feature.comments.di.DaggerCommentsComponent
import dev.thomasharris.claw.lib.lobsters.CommentDatabaseEntity
import dev.thomasharris.claw.lib.lobsters.StoryDatabaseEntity
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@Suppress("unused")
class CommentsController constructor(args: Bundle) : LifecycleController(args) {

    private val component by getComponent<CommentsComponent> {
        DaggerCommentsComponent.builder()
            .singletonComponent(it)
            .build()
    }

    private val shortId: String = getArgs().getString("shortId")!!

    private lateinit var recycler: RecyclerView
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var toolbar: Toolbar

    private val listAdapter = CommentsAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        val root = inflater.inflate(R.layout.comments, container, false)

        recycler = root.findViewById(R.id.comments_recycler)
        appBarLayout = root.findViewById(R.id.comments_app_bar_layout)
        toolbar = root.findViewById<Toolbar>(R.id.comments_toolbar).apply {
            setNavigationOnClickListener {
                router.popCurrentController()
            }

            title = "Comments"
        }

        recycler.apply {
            adapter = listAdapter
            layoutManager = LinearLayoutManager(root.context)
            setOnScrollChangeListener { v, _, _, _, _ ->
                appBarLayout.isSelected = v.canScrollVertically(-1)
            }
        }

        // Okay what am I doing
        // if I want to observe, then:
        //  I can just flow.collect here?
        //   if I get a new Story, then
        //   get the current list adapter,
        //   and replace the head
        //   if I get a new List<Comment>
        //   get the head, and replace the
        //   tail
        //  when to refresh?
        //  when requested by swiping
        //  or onCreateView(force = false)
        lifecycleScope.launch {
            Log.i("TEH", "executing story collect")
            component.commentRepository().liveStory(shortId).collect { story ->
                Log.i("TEH", "Collecting a story!")
                val head = CommentsItem.Header(story)
                val tail = listAdapter.currentList.filterIsInstance<CommentsItem.Comment>()
                listAdapter.submitList(listOf(head) + tail)
            }
        }

        lifecycleScope.launch {
            Log.i("TEH", "executing comment collect")
            component.commentRepository().liveComments(shortId).collect { comments ->
                Log.i("TEH", "Collecting a comments!")
                val head = listAdapter.currentList.filterIsInstance<CommentsItem.Header>()
                val tail = comments.map { CommentsItem.Comment(it) }
                Log.i("TEH", "n comments: ${tail.size}")
                listAdapter.submitList(head + tail)
            }


        }

        // TODO observe repository status here


        component.commentRepository().refresh(shortId)

        return root
    }
}

sealed class CommentsItem {
    data class Header(val storyDatabaseEntity: StoryDatabaseEntity) : CommentsItem()
    data class Comment(val commentDatabaseEntity: CommentDatabaseEntity) : CommentsItem()
}

class HeaderViewHolder(private val root: View) : RecyclerView.ViewHolder(root) {
    private val title: TextView = root.findViewById(R.id.comments_header_title)
    private val avatar: ImageView = root.findViewById(R.id.comments_header_avatar)
    private val author: TextView = root.findViewById(R.id.comments_header_author)

    private val description: TextView = root.findViewById(R.id.comments_description)

    fun bind(header: CommentsItem.Header) {
        title.text = header.storyDatabaseEntity.title

        // TODO whoops need a view
        avatar.background =
            ColorDrawable(ContextCompat.getColor(root.context, R.color.colorPrimary))
//        Glide.with(root)
//            .load("https://lobste.rs/${header.storyNetworkEntity.submitter.avatarUrl}")
//            .circleCrop()
//            .into(avatar)

        author.text = header.storyDatabaseEntity.submitterUsername
        description.text = HtmlCompat.fromHtml(
            header.storyDatabaseEntity.description,
            HtmlCompat.FROM_HTML_MODE_LEGACY
        ).trimEnd()
    }
}

class CommentViewHolder(private val root: View) : RecyclerView.ViewHolder(root) {
    private val marker: View = root.findViewById(R.id.comment_marker)
    private val avatar: ImageView = root.findViewById(R.id.comment_author_avatar)
    private val author: TextView = root.findViewById(R.id.comment_author)
    private val body: TextView = root.findViewById(R.id.comment_body)

    fun bind(comment: CommentsItem.Comment) {
        val colors = listOf(Color.RED, Color.BLUE, Color.GREEN)
        marker.backgroundTintList =
            ColorStateList.valueOf(colors[comment.commentDatabaseEntity.indentLevel % colors.size])

        marker.layoutParams = (marker.layoutParams as? ViewGroup.MarginLayoutParams)?.let {
            val displayMetrics = root.context.resources.displayMetrics
            val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, displayMetrics)
            it.leftMargin = (comment.commentDatabaseEntity.indentLevel - 1) * px.toInt()
            it
        } ?: marker.layoutParams

        // TODO oops don't have full user now, will need a view
        avatar.background =
            ColorDrawable(ContextCompat.getColor(root.context, R.color.colorPrimary))
//        Glide.with(root)
//            .load("https://lobste.rs/${comment.commentDatabaseEntity.commentingUser.avatarUrl}")
//            .circleCrop()
//            .into(avatar)

        author.text = comment.commentDatabaseEntity.commentUsername
        body.text = HtmlCompat.fromHtml(
            comment.commentDatabaseEntity.comment,
            HtmlCompat.FROM_HTML_MODE_LEGACY
        ).trimEnd()
    }
}

val DIFF = object : DiffUtil.ItemCallback<CommentsItem>() {
    override fun areContentsTheSame(oldItem: CommentsItem, newItem: CommentsItem): Boolean {
        (oldItem as? CommentsItem.Header)?.let { old ->
            (newItem as? CommentsItem.Header)?.let { new ->
                Log.i("TEH", "${old == new}")
                return old == new // TODO this doesn't work remember, causes flickering
            }
        }

        (oldItem as? CommentsItem.Comment)?.let { old ->
            (newItem as? CommentsItem.Comment)?.let { new ->
                return old == new
            }
        }

        return false
    }

    override fun areItemsTheSame(oldItem: CommentsItem, newItem: CommentsItem): Boolean {
        (oldItem as? CommentsItem.Header)?.let { old ->
            (newItem as? CommentsItem.Header)?.let { new ->
                return old.storyDatabaseEntity.shortId == new.storyDatabaseEntity.shortId
            }
        }

        (oldItem as? CommentsItem.Comment)?.let { old ->
            (newItem as? CommentsItem.Comment)?.let { new ->
                return old.commentDatabaseEntity.shortId == new.commentDatabaseEntity.shortId
            }
        }

        return false
    }

}

const val VIEW_TYPE_HEADER = 1
const val VIEW_TYPE_COMMENT = 2

class CommentsAdapter : ListAdapter<CommentsItem, RecyclerView.ViewHolder>(DIFF) {

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is CommentsItem.Header -> VIEW_TYPE_HEADER
        is CommentsItem.Comment -> VIEW_TYPE_COMMENT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> HeaderViewHolder(
                inflater.inflate(R.layout.item_comments_header, parent, false)
            )
            else -> CommentViewHolder(
                inflater.inflate(
                    R.layout.item_comments_comment,
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            VIEW_TYPE_HEADER -> (holder as HeaderViewHolder).bind(getItem(position) as CommentsItem.Header)
            VIEW_TYPE_COMMENT -> (holder as CommentViewHolder).bind(getItem(position) as CommentsItem.Comment)
        }
    }

}