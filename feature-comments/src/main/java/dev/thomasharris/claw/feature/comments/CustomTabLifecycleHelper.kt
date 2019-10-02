package dev.thomasharris.claw.feature.comments

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsSession
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

class CustomTabLifecycleHelper(
    private val url: String,
    private val contextProvider: () -> Context
) : LifecycleObserver {

    var session: CustomTabsSession? = null

    private val connection = object : CustomTabsServiceConnection() {
        override fun onCustomTabsServiceConnected(name: ComponentName, client: CustomTabsClient) {
            client.warmup(0)
            session = client.newSession(null).apply {
                mayLaunchUrl(Uri.parse(url), bundleOf(), listOf())
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            // empty
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        if (url.isNotBlank())
            CustomTabsClient.bindCustomTabsService(
                contextProvider(),
                "com.android.chrome",
                connection
            )
    }
}