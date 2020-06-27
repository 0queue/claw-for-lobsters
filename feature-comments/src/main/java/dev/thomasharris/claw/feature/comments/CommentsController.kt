package dev.thomasharris.claw.feature.comments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.browser.customtabs.CustomTabsClient
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import dev.thomasharris.claw.core.ext.getComponent
import dev.thomasharris.claw.core.ui.ViewLifecycleController
import dev.thomasharris.claw.feature.comments.di.CommentsComponent
import dev.thomasharris.claw.feature.comments.di.DaggerCommentsComponent
import dev.thomasharris.claw.lib.lobsters.LoadingStatus
import dev.thomasharris.claw.lib.navigator.Destination
import dev.thomasharris.claw.lib.navigator.goto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@Suppress("unused")
class CommentsController constructor(args: Bundle) : ViewLifecycleController(args) {

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

    private val listAdapter =
        CommentsAdapter(this::launchUrl, this::launchUrl) { shortId, isCollapsePredecessors ->
            if (isCollapsePredecessors)
                component.commentRepository.collapsePredecessors(shortId)
            else
                component.commentRepository.toggleCollapseComment(shortId)
        }

    @FlowPreview
    @ExperimentalCoroutinesApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedStateBundle: Bundle?
    ): View {
        val root = inflater.inflate(
            R.layout.comments,
            container,
            false
        ) as TouchInterceptingCoordinatorLayout

        swipeRefreshLayout = root.findViewById(R.id.comments_swipe_refresh)
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
            addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewDetachedFromWindow(v: View?) {
                    recycler.adapter = null
                }

                override fun onViewAttachedToWindow(v: View?) = Unit
            })
        }

        root.listener = CommentsTouchListener(root.context) {
            router.popCurrentController()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            component.commentRepository.visibleComments(shortId)
                .collect { (story, comments) ->
                    val head = story?.let(CommentsItem::Header)
                    val tail = comments.map { CommentsItem.Comment(it) }
                    listAdapter.submitList(listOfNotNull(head) + tail + CommentsItem.Spacer(tail.isEmpty()))
                }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            component.commentRepository.status.collect { status ->
                swipeRefreshLayout.isRefreshing = status.peek() == LoadingStatus.LOADING
                status.consume {
                    if (it == LoadingStatus.ERROR)
                        Snackbar.make(
                            root,
                            "Couldn't reach lobste.rs",
                            Snackbar.LENGTH_SHORT
                        ).show()
                }
            }
        }

        swipeRefreshLayout.setOnRefreshListener {
            lifecycleScope.launch {
                component.commentRepository.refresh(shortId, true)
            }
        }

        // hmm refreshing should maybe always be forced for a story?
        // or just add another condition for comment mismatches?
        lifecycleScope.launch {
            component.commentRepository.refresh(shortId)
        }

        root.setOnApplyWindowInsetsListener { v, insets ->
            v.onApplyWindowInsets(insets)
            insets
        }

        // warm up custom tabs a little
        CustomTabsClient.connectAndInitialize(root.context, "com.android.chrome")
        return root
    }

    private fun launchUrl(@Suppress("UNUSED_PARAMETER") _x: Any, url: String) = launchUrl(url)
    private fun launchUrl(url: String) = goto(Destination.WebPage(url))
}