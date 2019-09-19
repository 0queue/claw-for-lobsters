package dev.thomasharris.claw.core

import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction

class MainActivity : AppCompatActivity() {

    private lateinit var router: Router

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val container = findViewById<FrameLayout>(R.id.conductor_container)
        router = Conductor.attachRouter(this, container, savedInstanceState).apply {
            if (!hasRootController()) {
                val controller =
                    Class.forName("dev.thomasharris.feature.frontpage.FrontPageController").newInstance()
                (controller as? Controller)?.let {
                    setRoot(RouterTransaction.with(it))
                }
            }
        }
    }

    override fun onBackPressed() {
        if (!router.handleBack())
            super.onBackPressed()
    }
}
