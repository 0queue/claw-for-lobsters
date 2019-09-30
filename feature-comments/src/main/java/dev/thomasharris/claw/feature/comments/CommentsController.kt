package dev.thomasharris.claw.feature.comments

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.text.HtmlCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bluelinelabs.conductor.archlifecycle.LifecycleController
import com.bumptech.glide.Glide
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.button.MaterialButton
import dev.thomasharris.claw.core.ext.fade
import dev.thomasharris.claw.core.ext.getComponent
import dev.thomasharris.claw.core.ext.setScrollEnabled
import dev.thomasharris.claw.core.ui.StoryViewHolder
import dev.thomasharris.claw.feature.comments.di.CommentsComponent
import dev.thomasharris.claw.feature.comments.di.DaggerCommentsComponent
import dev.thomasharris.claw.lib.lobsters.CommentView
import dev.thomasharris.claw.lib.lobsters.FrontPageStory
import dev.thomasharris.claw.lib.lobsters.FrontPageTag
import dev.thomasharris.claw.lib.lobsters.LoadingStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
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

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recycler: RecyclerView
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var toolbar: Toolbar

    private lateinit var errorView: View
    private lateinit var errorReload: MaterialButton

    private val listAdapter = CommentsAdapter()

    @FlowPreview
    @ExperimentalCoroutinesApi
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        val root = inflater.inflate(R.layout.comments, container, false)

        swipeRefreshLayout = root.findViewById(R.id.comments_swipe_refresh)
        recycler = root.findViewById(R.id.comments_recycler)
        appBarLayout = root.findViewById(R.id.comments_app_bar_layout)
        toolbar = root.findViewById<Toolbar>(R.id.comments_toolbar).apply {
            setNavigationOnClickListener {
                router.popCurrentController()
            }

            title = "Comments"
        }
        errorView = root.findViewById(R.id.comments_error_view)
        errorReload = root.findViewById(R.id.comments_error_view_reload)

        recycler.apply {
            adapter = listAdapter
            layoutManager = LinearLayoutManager(root.context)
            setOnScrollChangeListener { v, _, _, _, _ ->
                appBarLayout.isSelected = v.canScrollVertically(-1)
            }
        }

        lifecycleScope.launch {
            component.commentRepository().liveComments(shortId).collect { (story, tags, comments) ->
                val head = CommentsItem.Header(story, tags)
                val tail = comments.map { CommentsItem.Comment(it) }
                listAdapter.submitList(listOf(head) + tail)
            }
        }

        lifecycleScope.launch {
            component.commentRepository().liveStatus().collect { status ->
                swipeRefreshLayout.isRefreshing = status == LoadingStatus.LOADING
                errorView.fade(status == LoadingStatus.ERROR)
                recycler.fade(status != LoadingStatus.ERROR)
                toolbar.setScrollEnabled(status != LoadingStatus.ERROR)
            }
        }

        swipeRefreshLayout.setOnRefreshListener {
            component.commentRepository().refresh(shortId, true)
        }

        // hmm refreshing should maybe always be forced for a story?
        // or just add another condition for comment mismatches?
        component.commentRepository().refresh(shortId)

        errorReload.setOnClickListener {
            component.commentRepository().refresh(shortId, true)
        }

        return root
    }
}

sealed class CommentsItem {
    data class Header(val story: FrontPageStory, val tags: List<FrontPageTag>) : CommentsItem()
    data class Comment(val commentView: CommentView) : CommentsItem()
}

// TODO make this nice
class CommentViewHolder(private val root: View) : RecyclerView.ViewHolder(root) {
    private val marker: View = root.findViewById(R.id.comment_marker)
    private val avatar: ImageView = root.findViewById(R.id.comment_author_avatar)
    private val author: TextView = root.findViewById(R.id.comment_author)
    private val body: TextView = root.findViewById(R.id.comment_body)

    fun bind(comment: CommentsItem.Comment) {
        val colors = listOf(Color.RED, Color.BLUE, Color.GREEN)
        marker.backgroundTintList =
            ColorStateList.valueOf(colors[comment.commentView.indentLevel % colors.size])

        marker.layoutParams = (marker.layoutParams as? ViewGroup.MarginLayoutParams)?.let {
            val displayMetrics = root.context.resources.displayMetrics
            val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, displayMetrics)
            it.leftMargin = (comment.commentView.indentLevel - 1) * px.toInt()
            it
        } ?: marker.layoutParams

        Glide.with(root)
            .load("https://lobste.rs/${comment.commentView.avatarShortUrl}")
            .circleCrop()
            .into(avatar)

        author.text = comment.commentView.commentUsername
        body.text = HtmlCompat.fromHtml(
            comment.commentView.comment,
            HtmlCompat.FROM_HTML_MODE_LEGACY
        ).trimEnd()
    }
}

val DIFF = object : DiffUtil.ItemCallback<CommentsItem>() {
    override fun areContentsTheSame(oldItem: CommentsItem, newItem: CommentsItem): Boolean {
        (oldItem as? CommentsItem.Header)?.let { old ->
            (newItem as? CommentsItem.Header)?.let { new ->
                // TODO is probably not working correctly
                //  probably should cast to the Impl
                return old == new
            }
        }

        (oldItem as? CommentsItem.Comment)?.let { old ->
            (newItem as? CommentsItem.Comment)?.let { new ->
                // TODO is probably not working correctly
                return old == new
            }
        }

        return false
    }

    override fun areItemsTheSame(oldItem: CommentsItem, newItem: CommentsItem): Boolean {
        (oldItem as? CommentsItem.Header)?.let { old ->
            (newItem as? CommentsItem.Header)?.let { new ->
                return old.story.shortId == new.story.shortId
            }
        }

        (oldItem as? CommentsItem.Comment)?.let { old ->
            (newItem as? CommentsItem.Comment)?.let { new ->
                return old.commentView.shortId == new.commentView.shortId
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
            VIEW_TYPE_HEADER -> StoryViewHolder.inflate(parent.context, parent)
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
            VIEW_TYPE_HEADER -> {
                val (story, tags) = getItem(position) as CommentsItem.Header
                (holder as StoryViewHolder).bind(story, tags, isCompact = false)
            }
            VIEW_TYPE_COMMENT -> (holder as CommentViewHolder).bind(getItem(position) as CommentsItem.Comment)
        }
    }

}