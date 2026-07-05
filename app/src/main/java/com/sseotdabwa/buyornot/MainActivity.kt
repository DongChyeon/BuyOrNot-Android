package com.sseotdabwa.buyornot

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme
import com.sseotdabwa.buyornot.core.network.AuthEventBus
import com.sseotdabwa.buyornot.notification.FcmKeys
import com.sseotdabwa.buyornot.ui.BuyOrNotApp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var authEventBus: AuthEventBus

    private val pendingFeedId = MutableStateFlow<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleFeedDeepLink(intent)
        enableEdgeToEdge(
            statusBarStyle =
                SystemBarStyle.light(
                    scrim = Color.WHITE,
                    darkScrim = Color.WHITE,
                ),
            navigationBarStyle =
                SystemBarStyle.light(
                    scrim = Color.TRANSPARENT,
                    darkScrim = Color.TRANSPARENT,
                ),
        )
        setContent {
            val feedId by pendingFeedId.collectAsStateWithLifecycle()
            BuyOrNotTheme {
                BuyOrNotApp(
                    authEventBus = authEventBus,
                    pendingFeedId = feedId,
                    onPendingFeedIdConsumed = { pendingFeedId.value = null },
                    onBackPressed = { finish() },
                    onFinish = { finishAffinity() },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleFeedDeepLink(intent)
    }

    private fun handleFeedDeepLink(intent: Intent?) {
        if (intent == null || !intent.hasExtra(FcmKeys.FEED_ID)) return
        val feedId =
            intent.getStringExtra(FcmKeys.FEED_ID)?.toLongOrNull()
                ?: intent.getLongExtra(FcmKeys.FEED_ID, -1L).takeIf { it != -1L }
        intent.removeExtra(FcmKeys.FEED_ID)
        setIntent(intent)
        if (BuildConfig.DEBUG) {
            Log.d("FCM", "handleFeedDeepLink - resolved feedId=$feedId")
        }
        if (feedId != null) {
            pendingFeedId.value = feedId
        }
    }
}
