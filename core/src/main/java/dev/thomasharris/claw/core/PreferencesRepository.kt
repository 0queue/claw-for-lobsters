package dev.thomasharris.claw.core

import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.NightMode
import androidx.core.content.edit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepository @Inject constructor(
    private val prefs: SharedPreferences
) {

    fun setTheme(value: ThemeMode) = prefs.edit {
        putString("THEME_MODE", value.toString())
    }

    fun getTheme(): ThemeMode = ThemeMode.valueOf(prefs.getString("THEME_MODE", "SYSTEM")!!)

    enum class ThemeMode(@NightMode val modeNight: Int) {
        DAY(AppCompatDelegate.MODE_NIGHT_NO),
        NIGHT(AppCompatDelegate.MODE_NIGHT_YES),
        SYSTEM(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            else
                AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
        )
    }
}