package dev.thomasharris.claw.lib.navigator

import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.RouterTransaction

interface Navigator {
    fun goto(routerTransaction: RouterTransaction)
}


sealed class Destination {
    abstract fun routerTransaction(): RouterTransaction

    object Home : Destination() {
        override fun routerTransaction(): RouterTransaction {
            val controller =
                Class.forName("dev.thomasharris.claw.feature.frontpage.FrontPageController")
                    .newInstance()
            return RouterTransaction.with(controller as Controller)
        }
    }
}

fun Controller.goto(destination: Destination) {
    (activity as Navigator).goto(destination.routerTransaction())
}