package dev.thomasharris.claw.feature.comments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.annotation.Keep
import androidx.browser.customtabs.CustomTabsClient
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import dev.thomasharris.claw.core.HasBinding
import dev.thomasharris.claw.core.ext.dipToPx
import dev.thomasharris.claw.core.ext.getComponent
import dev.thomasharris.claw.core.ui.ViewLifecycleController
import dev.thomasharris.claw.core.withBinding
import dev.thomasharris.claw.feature.comments.databinding.ControllerCommentsBinding
import dev.thomasharris.claw.feature.comments.di.CommentsComponent
import dev.thomasharris.claw.feature.comments.di.DaggerCommentsComponent
import dev.thomasharris.claw.lib.lobsters.LoadingStatus
import dev.thomasharris.claw.lib.navigator.Destination
import dev.thomasharris.claw.lib.navigator.goto
import dev.thomasharris.claw.lib.swipeback.SwipeBackTouchListener
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@Suppress("unused")
class CommentsController constructor(
    args: Bundle,
) : ViewLifecycleController(args), HasBinding<ControllerCommentsBinding> {

    private val component by getComponent<CommentsComponent> {
        DaggerCommentsComponent.builder()
            .singletonComponent(it)
            .build()
    }

    override var binding: ControllerCommentsBinding? = null

    @Keep // proguard not liking this without Keeping
    private var preDrawListener: ViewTreeObserver.OnPreDrawListener? = null

    private val shortId: String = getArgs().getString("shortId")!!

    private val listAdapter =
        CommentsAdapter(
            onHeaderClick = this::launchUrl,
            onLinkClick = this::launchUrl,
            onCommentClick = { shortId, isCollapsePredecessors ->
                if (isCollapsePredecessors)
                    component.commentRepository.collapsePredecessors(shortId)
                else
                    component.commentRepository.toggleCollapseComment(shortId)
            },
            onLongClick = {
                // Reusing the StoryModal for now, until comments
                // and stories have different options
                goto(Destination.StoryModal(it))
            },
        )

    // cache the last status to filter out redelivered ERROR statuses on rotate
    private var lastStatus: LoadingStatus? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedStateBundle: Bundle?,
    ): View {
        binding = ControllerCommentsBinding.inflate(inflater, container, false)

        withBinding {
            with(commentsToolbar) {
                setNavigationOnClickListener { router.popCurrentController() }
                title = "Comments"
            }

            with(commentsRecycler) {
                adapter = listAdapter
                layoutManager = LinearLayoutManager(context)
                setOnScrollChangeListener { v, _, _, _, _ ->
                    commentsAppBarLayout.isSelected = v.canScrollVertically(-1)
                }
                addOnAttachStateChangeListener(
                    object : View.OnAttachStateChangeListener {
                        override fun onViewDetachedFromWindow(v: View?) {
                            adapter = null
                        }

                        override fun onViewAttachedToWindow(v: View?) = Unit
                    }
                )
            }

            root.listener = SwipeBackTouchListener(root.context) {
                router.popCurrentController()
            }

            commentsSwipeRefresh.setOnRefreshListener {
                lifecycleScope.launch {
                    component.commentRepository.refresh(shortId, true)
                }
            }

            root.setOnApplyWindowInsetsListener { v, insets ->
                v.onApplyWindowInsets(insets)
                insets
            }

            // only elevate if in movement, a static elevation in the layout
            // means layers of UserProfile don't have elevation over each other!
            preDrawListener = ViewTreeObserver.OnPreDrawListener {
                root.elevation = if (root.translationX != 0f) 4f.dipToPx(root.context) else 0f
                true
            }
            root.viewTreeObserver.addOnPreDrawListener(preDrawListener)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            component.commentRepository.visibleComments(shortId)
                .collect { (story, comments) ->
                    val head = story?.let(CommentsItem::Header)
                    val tail = comments.map { CommentsItem.Comment(it) }
                    listAdapter.submitList(listOfNotNull(head) + tail + CommentsItem.Spacer(tail.isEmpty()))
                }
        }

        // don't forget about the git stash
        viewLifecycleOwner.lifecycleScope.launch {
            component.commentRepository.status.collect { status ->
                requireBinding().commentsSwipeRefresh.isRefreshing = status == LoadingStatus.LOADING

                if (status != lastStatus && status == LoadingStatus.ERROR)
                    Snackbar.make(
                        requireBinding().root,
                        "Couldn't reach lobste.rs",
                        Snackbar.LENGTH_SHORT
                    ).show()

                lastStatus = status
            }
        }

        // hmm refreshing should maybe always be forced for a story?
        // or just add another condition for comment mismatches?
        lifecycleScope.launch {
            component.commentRepository.refresh(shortId)
        }

        // warm up custom tabs a little
        CustomTabsClient.connectAndInitialize(container.context, "com.android.chrome")
        return requireBinding().root
    }

    override fun onDestroyView(view: View) {
        super.onDestroyView(view)
        if (preDrawListener != null)
            binding?.root?.viewTreeObserver?.removeOnPreDrawListener(preDrawListener)
    }

    private fun launchUrl(@Suppress("UNUSED_PARAMETER") _x: Any, url: String) = launchUrl(url)
    private fun launchUrl(url: String) = goto(Destination.WebPage(url))
}
