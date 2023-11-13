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
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.readability.ui.animation.composableFadeThrough
import com.example.readability.ui.screens.Screens
import com.example.readability.ui.screens.auth.AuthScreen
import com.example.readability.ui.screens.book.BookScreen
import com.example.readability.ui.screens.settings.SettingsScreen
import com.example.readability.ui.screens.viewer.ViewerScreen
import com.example.readability.ui.theme.ReadabilityTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class SnackBarState(val state: SnackbarHostState? = null, val scope: CoroutineScope? = null) {
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
    companion object {
        init {
            System.loadLibrary("readability")
        }
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        WindowCompat.setDecorFitsSystemWindows(window, false)

        installSplashScreen()

        setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            val snackbarScope = rememberCoroutineScope()
            ReadabilityTheme {
                Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) {
                    val navController = rememberNavController()
                    CompositionLocalProvider(
                        LocalSnackbarHost provides SnackBarState(
                            state = snackbarHostState,
                            scope = snackbarScope
                        )
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = Screens.Book.route,
                        ) {
                            composableFadeThrough(Screens.Auth.route) {
                                AuthScreen(onNavigateBookList = {
                                    navController.navigate(Screens.Book.route) {
                                        popUpTo(Screens.Auth.route) {
                                            inclusive = true
                                        }
                                    }
                                })
                            }
                            composableFadeThrough(Screens.Book.route) {
                                BookScreen(
                                    onNavigateSettings = {
                                        navController.navigate(Screens.Settings.route)
                                    },
                                    onNavigateViewer = {
                                        navController.navigate(Screens.Viewer.createRoute(it))
                                    }
                                )
                            }
                            composableFadeThrough(Screens.Viewer.route) {
                                ViewerScreen(
                                    id = it.arguments?.getString("book_id") ?: "",
                                    onNavigateSettings = {
                                        navController.navigate(Screens.Settings.route)
                                    },
                                    onBack = {
                                        navController.popBackStack()
                                    }
                                )
                            }
                            composableFadeThrough(Screens.Settings.route) {
                                SettingsScreen(
                                    onBack = {
                                        navController.popBackStack()
                                    },
                                    onNavigateAuth = {
                                        navController.navigate(Screens.Auth.route) {
                                            popUpTo(Screens.Auth.route) {
                                                inclusive = true
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
