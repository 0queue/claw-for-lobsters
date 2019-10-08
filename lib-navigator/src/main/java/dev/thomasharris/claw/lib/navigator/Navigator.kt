package dev.thomasharris.claw.lib.navigator

import androidx.core.os.bundleOf
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.RouterTransaction

interface Navigator {
    fun goto(routerTransaction: RouterTransaction)
}


sealed class Destination {
    abstract fun routerTransaction(): RouterTransaction

    object FrontPage : Destination() {
        override fun routerTransaction(): RouterTransaction {
            val controller =
                Class.forName("dev.thomasharris.claw.feature.frontpage.FrontPageController")
                    .newInstance()
            return RouterTransaction.with(controller as Controller)
        }
    }

    class Comments(private val shortId: String, private val url: String) : Destination() {
        override fun routerTransaction(): RouterTransaction {
            val clazz =
                Class.forName("dev.thomasharris.claw.feature.comments.CommentsController")

            // the normal java reflection seems to work better
            val controller = clazz.constructors[0].newInstance(
                bundleOf(
                    "shortId" to shortId,
                    "url" to url
                )
            )

            return RouterTransaction.with(controller as Controller)
                .popChangeHandler(SlideChangeHandler(200)) // TODO magic number
                .pushChangeHandler(SlideChangeHandler(200))
        }
    }
}

fun Controller.goto(destination: Destination) {
    (activity as Navigator).goto(destination.routerTransaction())
}