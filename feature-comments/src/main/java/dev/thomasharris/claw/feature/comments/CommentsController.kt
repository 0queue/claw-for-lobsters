package dev.thomasharris.claw.feature.comments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsClient
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import dev.thomasharris.claw.core.HasBinding
import dev.thomasharris.claw.core.ext.getComponent
import dev.thomasharris.claw.core.ui.ViewLifecycleController
import dev.thomasharris.claw.core.withBinding
import dev.thomasharris.claw.feature.comments.databinding.ControllerCommentsBinding
import dev.thomasharris.claw.feature.comments.di.CommentsComponent
import dev.thomasharris.claw.feature.comments.di.DaggerCommentsComponent
import dev.thomasharris.claw.lib.lobsters.LoadingStatus
import dev.thomasharris.claw.lib.navigator.Destination
import dev.thomasharris.claw.lib.navigator.goto
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@Suppress("unused")
class CommentsController constructor(
    args: Bundle
) : ViewLifecycleController(args), HasBinding<ControllerCommentsBinding> {

    private val component by getComponent<CommentsComponent> {
        DaggerCommentsComponent.builder()
            .singletonComponent(it)
            .build()
    }

    override var binding: ControllerCommentsBinding? = null

    private val shortId: String = getArgs().getString("shortId")!!

    private val listAdapter =
        CommentsAdapter(this::launchUrl, this::launchUrl) { shortId, isCollapsePredecessors ->
            if (isCollapsePredecessors)
                component.commentRepository.collapsePredecessors(shortId)
            else
                component.commentRepository.toggleCollapseComment(shortId)
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedStateBundle: Bundle?
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
                addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                    override fun onViewDetachedFromWindow(v: View?) {
                        adapter = null
                    }

                    override fun onViewAttachedToWindow(v: View?) = Unit
                })
            }

            root.listener = CommentsTouchListener(root.context) {
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
                requireBinding().commentsSwipeRefresh.isRefreshing =
                    status.peek() == LoadingStatus.LOADING
                status.consume {
                    if (it == LoadingStatus.ERROR)
                        Snackbar.make(
                            requireBinding().root,
                            "Couldn't reach lobste.rs",
                            Snackbar.LENGTH_SHORT
                        ).show()
                }
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

    private fun launchUrl(@Suppress("UNUSED_PARAMETER") _x: Any, url: String) = launchUrl(url)
    private fun launchUrl(url: String) = goto(Destination.WebPage(url))
}