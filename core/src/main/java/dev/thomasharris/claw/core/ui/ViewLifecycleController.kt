package dev.thomasharris.claw.core.ui

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.archlifecycle.LifecycleController

abstract class ViewLifecycleController(bundle: Bundle?) : LifecycleController(bundle) {

    constructor() : this(null)

    private var _viewLifecycleOwner: ViewLifecycleOwner<ViewLifecycleController>? = null

    val viewLifecycleOwner: ViewLifecycleOwner<ViewLifecycleController>
        get() = _viewLifecycleOwner!!

    init {
        addLifecycleListener(object : LifecycleListener {
            override fun preCreateView(controller: Controller) {
                _viewLifecycleOwner = ViewLifecycleOwner(this@ViewLifecycleController)
            }

            override fun postDestroyView(controller: Controller) {
                _viewLifecycleOwner = null
            }
        })
    }
}

class ViewLifecycleOwner<T>(
    lifecycleController: T
) : LifecycleOwner where T : Controller, T : LifecycleOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)

    init {
        lifecycleController.addLifecycleListener(object : Controller.LifecycleListener {
            override fun postCreateView(controller: Controller, view: View) {
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
            }

            override fun postAttach(controller: Controller, view: View) {
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
            }

            override fun preDetach(controller: Controller, view: View) {
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            }

            override fun preDestroyView(controller: Controller, view: View) {
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                lifecycleController.removeLifecycleListener(this)
            }
        })
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }
}