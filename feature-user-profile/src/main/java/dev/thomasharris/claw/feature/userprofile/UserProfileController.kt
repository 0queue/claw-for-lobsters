package dev.thomasharris.claw.feature.userprofile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import dev.thomasharris.betterhtml.fromHtml
import dev.thomasharris.claw.core.HasBinding
import dev.thomasharris.claw.core.ext.dipToPx
import dev.thomasharris.claw.core.ext.fade
import dev.thomasharris.claw.core.ext.getComponent
import dev.thomasharris.claw.core.ui.ViewLifecycleController
import dev.thomasharris.claw.feature.userprofile.databinding.ControllerUserProfileBinding
import dev.thomasharris.claw.feature.userprofile.di.DaggerUserProfileComponent
import dev.thomasharris.claw.feature.userprofile.di.UserProfileComponent
import dev.thomasharris.claw.lib.navigator.back
import dev.thomasharris.claw.lib.swipeback.SwipeBackTouchListener
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class UserProfileController(
    args: Bundle,
) : ViewLifecycleController(args), HasBinding<ControllerUserProfileBinding> {

    private val component by getComponent<UserProfileComponent> {
        DaggerUserProfileComponent.builder()
            .singletonComponent(it)
            .build()
    }

    override var binding: ControllerUserProfileBinding? = null

    private val username = args.getString("username")!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?,
    ): View {
        binding = ControllerUserProfileBinding.inflate(inflater, container, false).apply {
            root.listener = SwipeBackTouchListener(root.context) {
                back()
            }

            root.setOnApplyWindowInsetsListener { v, insets ->
                v.onApplyWindowInsets(insets)
                insets
            }

            lifecycleScope.launch {
                component.userRepository.refresh(username)
                component.userRepository.latestUser(username).collect { user ->
                    if (user != null) {
                        usernameTextView.text = user.about.ifEmpty {
                            "<em>A mystery...</em>".fromHtml { it.dipToPx(root.context) }
                        }
                    }

                    Log.i("TEH", "user == null ? $user")
                    constraintLayout.fade(user != null)
                }
            }
        }

        return requireBinding().root
    }
}
