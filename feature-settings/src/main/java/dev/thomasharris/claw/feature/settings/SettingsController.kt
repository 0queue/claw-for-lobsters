package dev.thomasharris.claw.feature.settings


import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.bluelinelabs.conductor.archlifecycle.LifecycleController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dev.thomasharris.claw.lib.navigator.back

@Suppress("unused")
class SettingsController : LifecycleController() {

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        val root = inflater.inflate(R.layout.settings, container, false) as CoordinatorLayout
        val bottomSheet = root.findViewById<ConstraintLayout>(R.id.settings_bottom_sheet)

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet).apply {
            bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit

                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_HIDDEN)
                        back()
                }
            }

            state = BottomSheetBehavior.STATE_HIDDEN
        }

        root.findViewById<View>(R.id.settings_scrim).setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            back()
        }

        return root
    }

    override fun onAttach(view: View) {
        super.onAttach(view)

        view.post {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun handleBack(): Boolean {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        back()

        return true
    }
}