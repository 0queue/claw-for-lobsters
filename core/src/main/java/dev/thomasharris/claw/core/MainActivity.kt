package dev.thomasharris.claw.core

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import dev.thomasharris.claw.lib.navigator.Destination
import dev.thomasharris.claw.lib.navigator.Navigator

class MainActivity : AppCompatActivity(), Navigator {
    private lateinit var router: Router

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        val container = findViewById<FrameLayout>(R.id.conductor_container)
        router = Conductor.attachRouter(this, container, savedInstanceState).apply {
            if (!hasRootController())
                setRoot(Destination.FrontPage.routerTransaction())
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
