package dev.thomasharris.claw.core

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.SimpleSwapChangeHandler
import dev.thomasharris.claw.core.databinding.ActivityMainBinding
import dev.thomasharris.claw.lib.navigator.Destination
import dev.thomasharris.claw.lib.navigator.Navigator

private const val REQUEST_CODE = 2020_12_27

class MainActivity : AppCompatActivity(), Navigator {
    private lateinit var router: Router

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        router =
            Conductor.attachRouter(this, binding.conductorContainer, savedInstanceState).apply {
                if (!hasRootController())
                    intent
                        .syntheticBackstack(true)
                        .map(Destination::routerTransaction)
                        .also { it.last().pushChangeHandler(SimpleSwapChangeHandler(false)) }
                        .let { setBackstack(it, null) }
            }
    }

    override fun goto(routerTransaction: RouterTransaction) {
        router.pushController(routerTransaction)
    }

    override fun onBackPressed() {
        if (!router.handleBack())
            super.onBackPressed()
    }

    /**
     * When a new intent is received, we are already on top,
     * so turn the intent into a Destination by "deeplinking" but
     * *not* replacing the whole conductor backstack, to reuse the url code.
     *
     * If the intent is for the FrontPage, start a new MainActivity,
     * instead of ignoring or, even worse, adding FrontPage as
     * anything but the root of the conductor backstack
     */
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if (intent != null && intent.isLinkHandlingIntent()) intent
            .syntheticBackstack(false)
            .lastOrNull()
            ?.let { dest ->
                if (dest !is Destination.FrontPage)
                    dest.routerTransaction().let(this::goto)
                else {
                    val explicit = Intent(this, MainActivity::class.java)
                    // startActivityForResult ignores singleTop :^) Can't find a better solution
                    startActivityForResult(explicit, REQUEST_CODE)
                }
            }
    }

    /**
     * Good test area: https://lobste.rs/s/z7floj/beautiful_silent_thunderbolt_3_pc
     *
     * Test command: adb shell am start -a android.intent.action.VIEW -d "..."
     *
     * @return non empty list of destinations
     */
    private fun Intent?.syntheticBackstack(isDeeplinked: Boolean): List<Destination> {
        if (this == null || data == null || data?.pathSegments.isNullOrEmpty())
            return listOf(Destination.FrontPage)

        return data?.pathSegments?.let { segments ->
            when (segments.getOrNull(0)) {
                "s" -> segments.getOrNull(1)?.let { Destination.Comments(it, isDeeplinked) }
                "u" -> segments.getOrNull(1)?.let { Destination.UserProfile(it, isDeeplinked) }
                else -> null
            }
        }.let { lastDestination ->
            if (lastDestination == null)
                Log.e(
                    this@MainActivity::class.java.simpleName,
                    "Failed to determine destination for $data (deeplink? $isDeeplinked)"
                )

            listOfNotNull(Destination.FrontPage, lastDestination)
        }
    }

    private fun Intent.isLinkHandlingIntent(): Boolean =
        action == Intent.ACTION_VIEW && data?.host == "lobste.rs"
}
