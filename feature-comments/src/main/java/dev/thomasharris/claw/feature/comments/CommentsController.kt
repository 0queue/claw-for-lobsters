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
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bluelinelabs.conductor.archlifecycle.LifecycleController
import com.bumptech.glide.Glide
import com.google.android.material.appbar.AppBarLayout
import dev.thomasharris.claw.core.ext.getComponent
import dev.thomasharris.claw.feature.comments.di.CommentsComponent
import dev.thomasharris.claw.feature.comments.di.CommentsModule
import dev.thomasharris.claw.feature.comments.di.DaggerCommentsComponent
import dev.thomasharris.claw.lib.lobsters.CommentNetworkEntity
import dev.thomasharris.claw.lib.lobsters.StoryNetworkEntity
import retrofit2.Call
import retrofit2.Response

@Suppress("unused")
class CommentsController constructor(args: Bundle) : LifecycleController(args) {

    private val component by getComponent<CommentsComponent> {
        DaggerCommentsComponent.builder()
            .singletonComponent(it)
            .commentsModule(CommentsModule())
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

        return root
    }

    override fun onAttach(view: View) {
        super.onAttach(view)

        component.lobstersService().getStorySync(shortId).enqueue(object :
            retrofit2.Callback<StoryNetworkEntity> {
            override fun onFailure(call: Call<StoryNetworkEntity>, t: Throwable) {
                Toast.makeText(activity, "Failed to get story", Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(
                call: Call<StoryNetworkEntity>,
                response: Response<StoryNetworkEntity>
            ) {
                response.body()?.let { story ->
                    listAdapter.submitList(listOf(CommentsItem.Header(story)) + (story.comments?.map {
                        CommentsItem.Comment(it)
                    } ?: listOf()))
                }
            }
        })
    }
}

sealed class CommentsItem {
    data class Header(val storyNetworkEntity: StoryNetworkEntity) : CommentsItem()
    data class Comment(val commentNetworkEntity: CommentNetworkEntity) : CommentsItem()
}

class HeaderViewHolder(private val root: View) : RecyclerView.ViewHolder(root) {
    private val title: TextView = root.findViewById(R.id.comments_header_title)
    private val avatar: ImageView = root.findViewById(R.id.comments_header_avatar)
    private val author: TextView = root.findViewById(R.id.comments_header_author)

    private val description: TextView = root.findViewById(R.id.comments_description)

    fun bind(header: CommentsItem.Header) {
        title.text = header.storyNetworkEntity.title
        Glide.with(root)
            .load("https://lobste.rs/${header.storyNetworkEntity.submitter.avatarUrl}")
            .circleCrop()
            .into(avatar)

        author.text = header.storyNetworkEntity.submitter.username
        description.text = HtmlCompat.fromHtml(
            header.storyNetworkEntity.description,
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
            ColorStateList.valueOf(colors[comment.commentNetworkEntity.indentLevel % colors.size])

        marker.layoutParams = (marker.layoutParams as? ViewGroup.MarginLayoutParams)?.let {
            val displayMetrics = root.context.resources.displayMetrics
            val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, displayMetrics)
            it.leftMargin = (comment.commentNetworkEntity.indentLevel - 1) * px.toInt()
            it
        } ?: marker.layoutParams

        Glide.with(root)
            .load("https://lobste.rs/${comment.commentNetworkEntity.commentingUser.avatarUrl}")
            .circleCrop()
            .into(avatar)

        author.text = comment.commentNetworkEntity.commentingUser.username
        body.text = HtmlCompat.fromHtml(
            comment.commentNetworkEntity.comment,
            HtmlCompat.FROM_HTML_MODE_LEGACY
        ).trimEnd()
    }
}

val DIFF = object : DiffUtil.ItemCallback<CommentsItem>() {
    override fun areContentsTheSame(oldItem: CommentsItem, newItem: CommentsItem): Boolean {
        (oldItem as? CommentsItem.Header)?.let { old ->
            (newItem as? CommentsItem.Header)?.let { new ->
                return old == new
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
                return old.storyNetworkEntity.shortId == new.storyNetworkEntity.shortId
            }
        }

        (oldItem as? CommentsItem.Comment)?.let { old ->
            (newItem as? CommentsItem.Comment)?.let { new ->
                return old.commentNetworkEntity.shortId == new.commentNetworkEntity.shortId
            }
        }

        return false
    }

}

const val VIEW_TYPE_HEADER = 1
const val VIEW_TYPE_COMMENT = 2

// TODO list adapter with diff callback
class CommentsAdapter : ListAdapter<CommentsItem, RecyclerView.ViewHolder>(DIFF) {

    override fun getItemViewType(position: Int) = when (position) {
        0 -> VIEW_TYPE_HEADER
        else -> VIEW_TYPE_COMMENT
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