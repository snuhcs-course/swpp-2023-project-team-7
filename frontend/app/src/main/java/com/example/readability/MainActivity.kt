package com.example.readability

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.readability.ui.animation.composableFadeThrough
import com.example.readability.ui.screens.Screens
import com.example.readability.ui.screens.auth.AuthScreen
import com.example.readability.ui.screens.book.BookScreen
import com.example.readability.ui.screens.settings.SettingsScreen
import com.example.readability.ui.screens.settings.SettingsScreens
import com.example.readability.ui.screens.viewer.ViewerScreen
import com.example.readability.ui.theme.ReadabilityTheme
import com.example.readability.ui.viewmodels.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

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

@AndroidEntryPoint
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

        val userViewModel: UserViewModel by viewModels()
        val isSignedIn = runBlocking {
            withContext(Dispatchers.IO) {
                userViewModel.isSignedIn()
            }
        }

        setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            val snackbarScope = rememberCoroutineScope()
            ReadabilityTheme {
                Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) {
                    val navController = rememberNavController()
                    CompositionLocalProvider(
                        LocalSnackbarHost provides SnackBarState(
                            state = snackbarHostState, scope = snackbarScope
                        )
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = if (isSignedIn) Screens.Book.route else Screens.Auth.route
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
                                BookScreen(onNavigateSettings = {
                                    navController.navigate(
                                        Screens.Settings.createRoute(
                                            SettingsScreens.Settings.route
                                        )
                                    )
                                }, onNavigateViewer = {
                                    navController.navigate(Screens.Viewer.createRoute(it))
                                })
                            }
                            composableFadeThrough(Screens.Viewer.route,
                                listOf(navArgument("book_id") {
                                    type = NavType.IntType
                                })) {
                                ViewerScreen(id = it.arguments?.getInt("book_id") ?: -1,
                                    onNavigateSettings = {
                                        navController.navigate(
                                            Screens.Settings.createRoute(
                                                SettingsScreens.Viewer.route
                                            )
                                        )
                                    },
                                    onBack = {
                                        navController.popBackStack()
                                    })
                            }
                            composableFadeThrough(Screens.Settings.route) {
                                val route = it.arguments?.getString("route") ?: ""
                                SettingsScreen(onBack = {
                                    navController.popBackStack()
                                }, onNavigateAuth = {
                                    navController.navigate(Screens.Auth.route) {
                                        popUpTo(Screens.Auth.route) {
                                            inclusive = true
                                        }
                                    }
                                }, startDestination = route
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
