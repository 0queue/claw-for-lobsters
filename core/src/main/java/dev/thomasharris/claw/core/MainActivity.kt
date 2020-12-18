package dev.thomasharris.claw.core

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.SimpleSwapChangeHandler
import dev.thomasharris.claw.core.databinding.ActivityMainBinding
import dev.thomasharris.claw.lib.navigator.Destination
import dev.thomasharris.claw.lib.navigator.Navigator

class MainActivity : AppCompatActivity(), Navigator {
    private lateinit var router: Router

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        router = Conductor.attachRouter(this, binding.conductorContainer, savedInstanceState).apply {
            if (!hasRootController())
                setRoot(Destination.FrontPage.routerTransaction())
        }

        intent?.data?.pathSegments?.getOrNull(1)?.let {
            goto(
                Destination.Comments(it).routerTransaction()
                    .pushChangeHandler(SimpleSwapChangeHandler(false))
            )
        }
    }

    override fun goto(routerTransaction: RouterTransaction) {
        router.pushController(routerTransaction)
    }

    override fun onBackPressed() {
        if (!router.handleBack())
            super.onBackPressed()
    }
}
