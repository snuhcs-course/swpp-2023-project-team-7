package com.example.readability.ui.screens

import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.readability.ui.animation.composableFadeThrough
import com.example.readability.ui.screens.auth.AuthScreen
import com.example.readability.ui.screens.book.BookScreen
import com.example.readability.ui.screens.settings.SettingsScreen
import com.example.readability.ui.screens.settings.SettingsScreens
import com.example.readability.ui.screens.viewer.ViewerScreen
import com.example.readability.ui.screens.viewer.findActivity

sealed class Screens(val route: String) {
    object Auth : Screens("auth")
    object Book : Screens("book")
    object Settings : Screens("settings/{route}") {
        fun createRoute(route: String) = "settings/$route"
    }

    object Viewer : Screens("viewer/{book_id}") {
        fun createRoute(bookId: Int) = "viewer/$bookId"
    }
}

@Composable
fun Screen(navController: NavHostController = rememberNavController(), isSignedIn: Boolean) {
    val context = LocalContext.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val immersiveModeEnabled = navBackStackEntry?.destination?.route?.startsWith("viewer") == true

    LaunchedEffect(immersiveModeEnabled) {
        val activity = context.findActivity() ?: return@LaunchedEffect
        if (immersiveModeEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                activity.window.insetsController?.let {
                    it.hide(WindowInsets.Type.systemBars())
                    it.systemBarsBehavior =
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                @Suppress("DEPRECATION")
                activity.window.decorView.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                activity.window.insetsController?.show(WindowInsets.Type.systemBars())
            } else {
                @Suppress("DEPRECATION")
                activity.window.decorView.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_VISIBLE
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (isSignedIn) Screens.Book.route else Screens.Auth.route,
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
                        SettingsScreens.Settings.route,
                    ),
                )
            }, onNavigateViewer = {
                navController.navigate(Screens.Viewer.createRoute(it))
            })
        }
        composableFadeThrough(
            Screens.Viewer.route,
            listOf(
                navArgument("book_id") {
                    type = NavType.IntType
                },
            ),
        ) {
            ViewerScreen(
                id = it.arguments?.getInt("book_id") ?: -1,
                onNavigateSettings = {
                    navController.navigate(
                        Screens.Settings.createRoute(
                            SettingsScreens.Viewer.route,
                        ),
                    )
                },
                onBack = {
                    navController.popBackStack()
                },
            )
        }
        composableFadeThrough(Screens.Settings.route) {
            val route = it.arguments?.getString("route") ?: ""
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
                },
                startDestination = route,
            )
        }
    }
}
