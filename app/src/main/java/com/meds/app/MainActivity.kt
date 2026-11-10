package com.meds.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.meds.app.ui.MedsRoot
import com.meds.app.ui.theme.MedsTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MedsTheme {
                MedsRoot(onExit = { finish() })
            }
        }
    }
}
