package dev.thomasharris.claw.feature.userprofile.di

import dagger.Component
import dagger.Module
import dagger.Provides
import dev.thomasharris.claw.core.di.FeatureScope
import dev.thomasharris.claw.core.di.SingletonComponent
import dev.thomasharris.claw.feature.userprofile.RenderMarkdownUseCase
import dev.thomasharris.claw.lib.lobsters.AsyncUserRepository
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

@Component(
    dependencies = [SingletonComponent::class],
    modules = [UserProfileModule::class]
)
@FeatureScope
interface UserProfileComponent {
    val userRepository: AsyncUserRepository

    val renderMarkdownUseCase: RenderMarkdownUseCase
}

@Module
class UserProfileModule {

    @Provides
    @FeatureScope
    fun provideParser(): Parser = Parser.builder().build()

    @Provides
    @FeatureScope
    fun provideHtmlRenderer(): HtmlRenderer = HtmlRenderer.builder().build()
}
