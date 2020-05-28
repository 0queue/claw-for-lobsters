package dev.thomasharris.claw.feature.settings


import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.forEach
import com.bluelinelabs.conductor.archlifecycle.LifecycleController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import dev.thomasharris.claw.core.PreferencesRepository
import dev.thomasharris.claw.core.ext.getComponent
import dev.thomasharris.claw.feature.settings.di.DaggerSettingsComponent
import dev.thomasharris.claw.feature.settings.di.SettingsComponent
import dev.thomasharris.claw.lib.navigator.back

@Suppress("unused")
class SettingsController : LifecycleController() {

    private val component by getComponent<SettingsComponent> {
        DaggerSettingsComponent.builder()
            .singletonComponent(it)
            .build()
    }

    private lateinit var appInfo: TextView

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var themeToggleGroup: MaterialButtonToggleGroup
    private lateinit var themeDescription: TextView

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.settings, container, false) as CoordinatorLayout
        val bottomSheet = root.findViewById<ConstraintLayout>(R.id.settings_bottom_sheet)
        themeToggleGroup = root.findViewById(R.id.settings_theme_toggle_group)
        themeDescription = root.findViewById(R.id.settings_theme_description)

        appInfo = root.findViewById(R.id.settings_app_info)

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet).apply {
            addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit

                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_HIDDEN)
                        back()
                }
            })

            state = BottomSheetBehavior.STATE_HIDDEN
        }

        root.findViewById<View>(R.id.settings_scrim).setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            back()
        }

        themeToggleGroup.check(
            when (component.preferencesRepository().getTheme()) {
                PreferencesRepository.ThemeMode.DAY -> R.id.settings_theme_day_mode
                PreferencesRepository.ThemeMode.NIGHT -> R.id.settings_theme_night_mode
                PreferencesRepository.ThemeMode.SYSTEM -> R.id.settings_theme_follow_system_mode
            }
        )
        updateThemeDescription(component.preferencesRepository().getTheme())

        themeToggleGroup.addOnButtonCheckedListener { v, checkedId, isChecked ->
            val themeMode = when (checkedId) {
                R.id.settings_theme_day_mode -> PreferencesRepository.ThemeMode.DAY
                R.id.settings_theme_night_mode -> PreferencesRepository.ThemeMode.NIGHT
                R.id.settings_theme_follow_system_mode -> PreferencesRepository.ThemeMode.SYSTEM
                else -> throw IllegalStateException("Theme mode not recognized")
            }

            if (isChecked) {
                updateThemeDescription(themeMode)
                component.preferencesRepository().setTheme(themeMode)
                v.postDelayed({
                    AppCompatDelegate.setDefaultNightMode(themeMode.modeNight)
                }, 150) // little hack to stop button flickering
            }
        }

        themeToggleGroup.forEach {
            it.setOnClickListener { b ->
                (b as MaterialButton).isChecked = true
            }
        }

        @Suppress("ConstantConditionIf")
        val debug = if (BuildConfig.BUILD_TYPE == "debug") " (debug)" else ""
        appInfo.text =
            root.context.getString(R.string.settings_app_info_text, BuildConfig.VERSION_CODE, debug)

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

    private fun updateThemeDescription(themeMode: PreferencesRepository.ThemeMode) {
        themeDescription.text = when (themeMode) {
            PreferencesRepository.ThemeMode.DAY -> "Day mode"
            PreferencesRepository.ThemeMode.NIGHT -> "Night mode"
            PreferencesRepository.ThemeMode.SYSTEM -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                "Follow system"
            else
                "Follow battery saver"
        }
    }
}