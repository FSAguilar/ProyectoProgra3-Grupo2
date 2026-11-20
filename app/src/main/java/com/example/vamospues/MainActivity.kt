package com.example.vamospues

import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.vamospues.ui.navigation.NavGraph
import com.example.vamospues.ui.theme.MyApplicationTheme

/** Tiempo mínimo que se muestra el logo al abrir la app (la demo carga al instante). */
private const val DuracionSplashMs = 700L
private const val DuracionSalidaSplashMs = 250L

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            val fin = SystemClock.uptimeMillis() + DuracionSplashMs
            splash.setKeepOnScreenCondition { SystemClock.uptimeMillis() < fin }
        }
        splash.setOnExitAnimationListener { vista ->
            vista.view.animate()
                .alpha(0f)
                .setDuration(DuracionSalidaSplashMs)
                .withEndAction { vista.remove() }
                .start()
        }
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                NavGraph()
            }
        }
    }
}

