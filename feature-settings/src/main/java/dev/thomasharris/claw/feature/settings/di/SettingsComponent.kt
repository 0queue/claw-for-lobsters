package dev.thomasharris.claw.feature.settings.di

import dagger.Component
import dev.thomasharris.claw.core.PreferencesRepository
import dev.thomasharris.claw.core.di.FeatureScope
import dev.thomasharris.claw.core.di.SingletonComponent

@FeatureScope
@Component(dependencies = [SingletonComponent::class])
interface SettingsComponent {
    fun preferencesRepository(): PreferencesRepository
}