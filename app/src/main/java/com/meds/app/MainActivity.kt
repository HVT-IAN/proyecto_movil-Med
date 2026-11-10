package com.meds.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.meds.app.navigation.AppNavGraph
import com.meds.app.core.ui.theme.MedsTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MedsTheme {
                AppNavGraph(onExit = { finish() })
            }
        }
    }
}
