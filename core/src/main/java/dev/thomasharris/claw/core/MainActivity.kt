package dev.thomasharris.claw.core

import android.os.Bundle
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
        val container = findViewById<FrameLayout>(R.id.conductor_container)
        router = Conductor.attachRouter(this, container, savedInstanceState).apply {
            if (!hasRootController())
                setRoot(Destination.Home.routerTransaction())
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
