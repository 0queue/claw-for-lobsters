package dev.thomasharris.claw.feature.userprofile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dev.thomasharris.claw.core.HasBinding
import dev.thomasharris.claw.core.ui.ViewLifecycleController
import dev.thomasharris.claw.feature.userprofile.databinding.ControllerUserProfileBinding
import dev.thomasharris.claw.lib.navigator.back
import dev.thomasharris.claw.lib.swipeback.SwipeBackTouchListener

class UserProfileController(
    args: Bundle,
) : ViewLifecycleController(args), HasBinding<ControllerUserProfileBinding> {
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

            usernameTextView.text = username
        }

        return requireBinding().root
    }
}
