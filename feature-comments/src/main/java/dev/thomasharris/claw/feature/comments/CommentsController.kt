package dev.thomasharris.claw.feature.comments

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.AttrRes
import androidx.appcompat.widget.Toolbar
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bluelinelabs.conductor.archlifecycle.LifecycleController
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import dev.thomasharris.claw.core.ext.getComponent
import dev.thomasharris.claw.feature.comments.di.CommentsComponent
import dev.thomasharris.claw.feature.comments.di.DaggerCommentsComponent
import dev.thomasharris.claw.lib.lobsters.LoadingStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
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
    private val url: String = getArgs().getString("url")!!

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recycler: RecyclerView
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var toolbar: Toolbar

    private val jobs = mutableListOf<Job>()

    private val listAdapter = CommentsAdapter({ _, url ->
        // TODO eventually fallback to web view
        CustomTabsIntent.Builder().apply {
            activity?.bitmapFromVector(R.drawable.ic_arrow_back_black_24dp)?.let {
                setCloseButtonIcon(it)
            }

            setShowTitle(true)

            activity?.let {
                setStartAnimations(it, R.anim.slide_in_from_right, R.anim.nothing)
                setExitAnimations(it, R.anim.nothing, R.anim.slide_out_to_right)
                // closest thing to turning on dark mode as far as I can tell
                setToolbarColor(it.getColorAttr(R.attr.colorSurface))
            }
        }.build().launchUrl(activity, Uri.parse(url))
    }) {
        component.collapseUseCase().collapse(it)
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
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

        jobs += lifecycleScope.launch {
            component.commentRepository().liveVisibleComments(shortId)
                .collect { (story, tags, comments) ->
                    val head = CommentsItem.Header(story, tags)
                    val tail = comments.map { CommentsItem.Comment(it) }
                    listAdapter.submitList(listOf(head) + tail + CommentsItem.Spacer(tail.isEmpty()))
                }
        }

        jobs += lifecycleScope.launch {
            component.commentRepository().liveStatus().collect { status ->
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
            component.commentRepository().refresh(shortId, true)
        }

        // hmm refreshing should maybe always be forced for a story?
        // or just add another condition for comment mismatches?
        component.commentRepository().refresh(shortId)

        root.setOnApplyWindowInsetsListener { v, insets ->
            v.onApplyWindowInsets(insets)
            insets
        }

        CustomTabsClient.connectAndInitialize(activity, "com.android.chrome")
        return root
    }

    override fun onDestroyView(view: View) {
        super.onDestroyView(view)
        jobs.forEach { it.cancel() }
        jobs.clear()
    }
}

fun Context.bitmapFromVector(drawableId: Int): Bitmap? {
    val drawable = ContextCompat.getDrawable(this, drawableId) ?: return null
    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth,
        drawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )

    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

fun Context.getColorAttr(@AttrRes attr: Int): Int {
    TypedValue().let {
        theme.resolveAttribute(attr, it, true)
        return it.data
    }
}