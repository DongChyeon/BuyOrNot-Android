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
import com.sseotdabwa.buyornot.core.analytics.Analytics
import com.sseotdabwa.buyornot.core.analytics.AnalyticsEvent
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

    @Inject
    lateinit var analytics: Analytics

    private val pendingFeedDeepLink = MutableStateFlow<PendingFeedDeepLink?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 딥링크 처리가 feedId extra를 소비하므로 로깅을 먼저 수행한다.
        handlePushOpened(intent)
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
            val pendingDeepLink by pendingFeedDeepLink.collectAsStateWithLifecycle()
            BuyOrNotTheme {
                BuyOrNotApp(
                    authEventBus = authEventBus,
                    pendingFeedDeepLink = pendingDeepLink,
                    onPendingFeedDeepLinkConsumed = { pendingFeedDeepLink.value = null },
                    onBackPressed = { finish() },
                    onFinish = { finishAffinity() },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handlePushOpened(intent)
        handleFeedDeepLink(intent)
    }

    /**
     * 알림 탭 유입을 로깅한다. 딥링크(feedId 필요)와 분리해야 feedId가 없는 마케팅 알림 탭도 잡힌다.
     */
    private fun handlePushOpened(intent: Intent?) {
        if (intent == null || !intent.getBooleanExtra(FcmKeys.FROM_PUSH, false)) return
        val pushType = intent.getStringExtra(FcmKeys.TYPE) ?: FcmKeys.UNKNOWN_TYPE
        val feedId = intent.longExtraOrNull(FcmKeys.FEED_ID)
        val notificationId = intent.longExtraOrNull(FcmKeys.NOTIFICATION_ID)
        // 회전·프로세스 재생성으로 onCreate가 같은 Intent를 다시 받아도 중복 발행되지 않도록 마커를 소비한다.
        intent.removeExtra(FcmKeys.FROM_PUSH)
        intent.removeExtra(FcmKeys.TYPE)
        setIntent(intent)
        if (BuildConfig.DEBUG) {
            Log.d("FCM", "handlePushOpened - pushType=$pushType, feedId=$feedId, notificationId=$notificationId")
        }
        analytics.track(
            AnalyticsEvent.PushOpened(
                pushType = pushType,
                feedId = feedId,
                notificationId = notificationId,
            ),
        )
    }

    private fun handleFeedDeepLink(intent: Intent?) {
        if (intent == null || !intent.hasExtra(FcmKeys.FEED_ID)) return
        val feedId = intent.longExtraOrNull(FcmKeys.FEED_ID)
        val notificationId = intent.longExtraOrNull(FcmKeys.NOTIFICATION_ID)
        intent.removeExtra(FcmKeys.FEED_ID)
        intent.removeExtra(FcmKeys.NOTIFICATION_ID)
        setIntent(intent)
        if (BuildConfig.DEBUG) {
            Log.d("FCM", "handleFeedDeepLink - resolved feedId=$feedId, notificationId=$notificationId")
        }
        // 딥링크는 feedId가 있어야 성립한다. 마케팅(feedId 없음)은 여기 도달 시 pending 미설정.
        if (feedId != null) {
            pendingFeedDeepLink.value = PendingFeedDeepLink(feedId = feedId, notificationId = notificationId)
        }
    }
}

/** 서버가 Long/String 중 무엇으로 보내도 읽히도록 두 형태를 모두 시도한다. */
private fun Intent.longExtraOrNull(key: String): Long? =
    getStringExtra(key)?.toLongOrNull()
        ?: getLongExtra(key, -1L).takeIf { it != -1L }

data class PendingFeedDeepLink(
    val feedId: Long,
    val notificationId: Long?,
)
