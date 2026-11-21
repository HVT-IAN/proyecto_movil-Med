package com.meds.app

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import com.meds.app.core.ui.theme.MedsTheme
import com.meds.app.navigation.AppNavGraph

class MainActivity : FragmentActivity() {
    private var contentReady = false
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

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MedsTheme {
                AppNavGraph(onExit = { finish() })
            }
            LaunchedEffect(Unit) {
                withFrameNanos { }
                contentReady = true
                mainHandler.removeCallbacks(releaseSplashFallback)
            }
        }
    }

    override fun onDestroy() {
        mainHandler.removeCallbacks(releaseSplashFallback)
        super.onDestroy()
    }
}
