package com.meds.app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.withFrameNanos
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import com.meds.app.core.ui.theme.MedsTheme
import com.meds.app.integration.notifications.EXTRA_LOG_ID
import com.meds.app.navigation.AppNavGraph

class MainActivity : FragmentActivity() {
    private var contentReady = false
    private val pendingFocusLogId = mutableStateOf<String?>(null)
    private val mainHandler = Handler(Looper.getMainLooper())
    private val releaseSplashFallback = Runnable { contentReady = true }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        splash.setKeepOnScreenCondition { !contentReady }
        splash.setOnExitAnimationListener { provider ->
            provider.view.animate()
                .alpha(0f)
                .scaleX(0.92f)
                .scaleY(0.92f)
                .setDuration(220L)
                .withEndAction { provider.remove() }
                .start()
        }
        mainHandler.postDelayed(releaseSplashFallback, 2500L)

        if (savedInstanceState == null) {
            pendingFocusLogId.value = intent?.getStringExtra(EXTRA_LOG_ID)
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MedsTheme {
                AppNavGraph(
                    onExit = { finish() },
                    initialFocusLogId = pendingFocusLogId.value,
                    onFocusConsumed = { pendingFocusLogId.value = null }
                )
            }
            LaunchedEffect(Unit) {
                withFrameNanos { }
                contentReady = true
                mainHandler.removeCallbacks(releaseSplashFallback)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingFocusLogId.value = intent.getStringExtra(EXTRA_LOG_ID)
    }

    override fun onDestroy() {
        mainHandler.removeCallbacks(releaseSplashFallback)
        super.onDestroy()
    }
}
