package dev.thomasharris.claw.feature.comments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bluelinelabs.conductor.archlifecycle.LifecycleController
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.button.MaterialButton
import dev.thomasharris.claw.core.ext.fade
import dev.thomasharris.claw.core.ext.getComponent
import dev.thomasharris.claw.core.ext.setScrollEnabled
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