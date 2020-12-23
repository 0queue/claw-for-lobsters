package dev.thomasharris.claw.lib.navigator

import androidx.core.os.bundleOf
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import com.bluelinelabs.conductor.changehandler.SimpleSwapChangeHandler

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

    class Comments(private val shortId: String) : Destination() {
        override fun routerTransaction(): RouterTransaction {
            val clazz =
                Class.forName("dev.thomasharris.claw.feature.comments.CommentsController")

            // the normal java reflection seems to work better
            val controller = clazz.constructors[0].newInstance(bundleOf("shortId" to shortId))

            return RouterTransaction.with(controller as Controller)
                .popChangeHandler(SlideChangeHandler(200)) // TODO magic number
                .pushChangeHandler(SlideChangeHandler(200))
        }
    }

    object Settings : Destination() {
        override fun routerTransaction(): RouterTransaction {
            val controller =
                Class.forName("dev.thomasharris.claw.feature.settings.SettingsController")
                    .newInstance()
            return RouterTransaction.with(controller as Controller)
                .pushChangeHandler(FadeChangeHandler(100, false))
                .popChangeHandler(FadeChangeHandler(100))
        }
    }

    class WebPage(private val webPageUrl: String) : Destination() {
        override fun routerTransaction(): RouterTransaction {
            val clazz = Class.forName("dev.thomasharris.claw.feature.webpage.WebPageController")
            val controller = clazz.constructors[0].newInstance(bundleOf("webPageUrl" to webPageUrl))

            return RouterTransaction.with(controller as Controller)
                .pushChangeHandler(SimpleSwapChangeHandler(false))
                .popChangeHandler(SimpleSwapChangeHandler(false))
        }
    }

    class StoryModal(private val author: String) : Destination() {
        override fun routerTransaction(): RouterTransaction {
            val clazz =
                Class.forName("dev.thomasharris.claw.core.ui.StoryAdditionalActionsController")
            val controller = clazz.constructors[0].newInstance(bundleOf("author" to author))

            return RouterTransaction.with(controller as Controller)
                .pushChangeHandler(FadeChangeHandler(100, false))
                .popChangeHandler(FadeChangeHandler(100))
        }
    }

    class UserProfile(private val username: String) : Destination() {
        override fun routerTransaction(): RouterTransaction {
            val clazz =
                Class.forName("dev.thomasharris.claw.feature.userprofile.UserProfileController")
            val controller = clazz.constructors[0].newInstance(bundleOf("username" to username))

            return RouterTransaction.with(controller as Controller)
                .popChangeHandler(SlideChangeHandler(200))
                .pushChangeHandler(SlideChangeHandler(200))
        }
    }
}

fun Controller.goto(destination: Destination) {
    (activity as Navigator).goto(destination.routerTransaction())
}

fun Controller.back() {
    router.popController(this)
}
