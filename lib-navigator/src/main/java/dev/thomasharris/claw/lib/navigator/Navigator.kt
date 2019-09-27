package dev.thomasharris.claw.lib.navigator

import androidx.core.os.bundleOf
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.RouterTransaction
import kotlin.reflect.full.primaryConstructor

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

    class Comments(val shortId: String) : Destination() {
        override fun routerTransaction(): RouterTransaction {
            val clazz =
                Class.forName("dev.thomasharris.claw.feature.comments.CommentsController")

            val controller = clazz.kotlin.primaryConstructor!!.call(
                bundleOf(
                    "shortId" to shortId
                )
            )

            return RouterTransaction.with(controller as Controller)
        }
    }
}

fun Controller.goto(destination: Destination) {
    (activity as Navigator).goto(destination.routerTransaction())
}