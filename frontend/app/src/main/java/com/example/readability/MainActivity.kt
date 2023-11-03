package com.example.readability

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.readability.ui.animation.composableFadeThrough
import com.example.readability.ui.screens.Screen
import com.example.readability.ui.screens.auth.AuthView
import com.example.readability.ui.screens.book.BookView
import com.example.readability.ui.screens.settings.SettingsView
import com.example.readability.ui.screens.viewer.ViewerView
import com.example.readability.ui.theme.ReadabilityTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class SnackBarState (val state: SnackbarHostState? = null, val scope: CoroutineScope? = null) {
    fun showSnackbar(message: String) {
        scope?.launch {
            state?.showSnackbar(message)
        }
    }
}

val LocalSnackbarHost = staticCompositionLocalOf {
    SnackBarState()
}

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            val snackbarScope = rememberCoroutineScope()
            ReadabilityTheme {
                Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) {
                    val navController = rememberNavController()
                    CompositionLocalProvider(LocalSnackbarHost provides SnackBarState(
                        state = snackbarHostState,
                        scope = snackbarScope
                    )) {
                        NavHost(
//                            modifier = Modifier.padding(innerPadding),
                            navController = navController,
                            startDestination = Screen.Auth.route,
                        ) {
                            composableFadeThrough(Screen.Auth.route) {
                                AuthView(onNavigateBookList = {
                                    navController.navigate(Screen.Book.route) {
                                        popUpTo(Screen.Auth.route) {
                                            inclusive = true
                                        }
                                    }
                                })
                            }
                            composableFadeThrough(Screen.Book.route) {
                                BookView()
                            }
                            composableFadeThrough(Screen.Viewer.route) {
                                ViewerView()
                            }
                            composableFadeThrough(Screen.Settings.route) {
                                SettingsView()
                            }
                        }
                    }
                }
            }
        }
    }
}
