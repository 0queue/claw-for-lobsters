package dev.thomasharris.claw.feature.comments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.bluelinelabs.conductor.archlifecycle.LifecycleController

@Suppress("unused")
class CommentsController constructor(args: Bundle) : LifecycleController(args) {

    private val shortId: String = getArgs().getString("shortId")!!

    private lateinit var recycler: RecyclerView
    private lateinit var toolbar: Toolbar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        val root = inflater.inflate(R.layout.comments, container, false)

        recycler = root.findViewById(R.id.comments_recycler)
        toolbar = root.findViewById<Toolbar>(R.id.comments_toolbar).apply {
            setNavigationOnClickListener {
                router.popCurrentController()
            }

            title = "Comments"
        }

        return root
    }
}