package com.sseotdabwa.buyornot.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotSnackBarHost
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme
import com.sseotdabwa.buyornot.core.network.AuthEventBus
import com.sseotdabwa.buyornot.core.ui.permission.rememberNotificationPermission
import com.sseotdabwa.buyornot.core.ui.snackbar.LocalSnackbarState
import com.sseotdabwa.buyornot.core.ui.snackbar.rememberBuyOrNotSnackbarState
import com.sseotdabwa.buyornot.feature.auth.navigation.AuthRoute
import com.sseotdabwa.buyornot.feature.auth.navigation.SplashRoute
import com.sseotdabwa.buyornot.feature.home.navigation.HomeRoute
import com.sseotdabwa.buyornot.feature.notification.navigation.navigateToFeedDetail
import com.sseotdabwa.buyornot.navigation.BuyOrNotNavHost

@Composable
fun BuyOrNotApp(
    authEventBus: AuthEventBus,
    pendingFeedId: Long? = null,
    onPendingFeedIdConsumed: () -> Unit = {},
    onBackPressed: () -> Unit = {},
    onFinish: () -> Unit = {},
    viewModel: BuyOrNotViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val snackbarState = rememberBuyOrNotSnackbarState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val isFirstRun by viewModel.isFirstRun.collectAsStateWithLifecycle()

    BackHandler(enabled = currentDestination?.route?.startsWith(HomeRoute::class.qualifiedName ?: "") == true) {
        onBackPressed()
    }

    val (hasNotificationPermission, requestNotificationPermission) = rememberNotificationPermission()

    LaunchedEffect(isFirstRun) {
        if (isFirstRun) {
            if (!hasNotificationPermission) {
                requestNotificationPermission()
            }
            viewModel.updateIsFirstRun(false)
        }
    }

    // FCM 알림 탭으로 전달된 pending feedId를 인증 완료(Splash/Auth 통과) 후 한 번만 소비한다.
    // Splash/로그인 화면에서는 보류하고, 인증된 어떤 화면(Home·MyPage·Upload 등)에서든 즉시 이동한다.
    val currentRoute = currentDestination?.route
    val isPastAuthGate =
        currentRoute != null &&
            currentRoute != SplashRoute::class.qualifiedName &&
            currentRoute != AuthRoute::class.qualifiedName
    LaunchedEffect(pendingFeedId, isPastAuthGate) {
        val feedId = pendingFeedId ?: return@LaunchedEffect
        if (!isPastAuthGate) return@LaunchedEffect
        navController.navigateToFeedDetail(feedId)
        onPendingFeedIdConsumed()
    }

    val isFullscreen =
        currentDestination?.route.let { route ->
            route == SplashRoute::class.qualifiedName || route == AuthRoute::class.qualifiedName
        }

    CompositionLocalProvider(LocalSnackbarState provides snackbarState) {
        Scaffold(
            containerColor = BuyOrNotTheme.colors.gray0,
            snackbarHost = { BuyOrNotSnackBarHost(snackbarState.snackbarHostState) },
        ) { innerPadding ->
            BuyOrNotNavHost(
                navController = navController,
                authEventBus = authEventBus,
                onFinish = onFinish,
                modifier =
                    Modifier
                        .consumeWindowInsets(innerPadding)
                        .bottomBarPadding(isFullscreen, innerPadding),
            )
        }
    }
}

private fun Modifier.bottomBarPadding(
    isFullscreen: Boolean,
    padding: PaddingValues,
): Modifier =
    if (isFullscreen) {
        this
    } else {
        this.padding(padding)
    }
