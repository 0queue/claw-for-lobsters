package dev.thomasharris.claw.feature.frontpage.di

import dagger.Component
import dev.thomasharris.claw.core.di.FeatureScope
import dev.thomasharris.claw.core.di.SingletonComponent
import dev.thomasharris.claw.feature.frontpage.paging3.FrontPagePagingSource

@Component(dependencies = [SingletonComponent::class])
@FeatureScope
interface FrontPageComponent {
    val frontPagePagingSource: FrontPagePagingSource
}