package com.example.readability.ui.screens.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.readability.ui.animation.SharedAxis
import com.example.readability.ui.animation.composableSharedAxis
import com.example.readability.ui.models.BookModel
import com.example.readability.ui.models.SettingModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

sealed class SettingsScreens(val route: String) {
    object Settings : SettingsScreens("settings")
    object PasswordCheck : SettingsScreens("password_check")
    object Account : SettingsScreens("account")
    object ChangePassword : SettingsScreens("change_password")
    object Viewer : SettingsScreens("viewer")
    object About : SettingsScreens("about/{type}") {
        fun createRoute(type: String) = "about/$type"
    }
}

@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
    onNavigateAuth: () -> Unit = {},
    startDestination: String = SettingsScreens.Settings.route
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = startDestination) {
        composableSharedAxis(SettingsScreens.Settings.route, axis = SharedAxis.X) {
            SettingsView(
                onBack = { onBack() },
                onNavigatePasswordCheck = { navController.navigate(SettingsScreens.PasswordCheck.route) },
                onNavigateViewer = { navController.navigate(SettingsScreens.Viewer.route) },
                onNavigateAbout = { navController.navigate(SettingsScreens.About.createRoute(it)) },
            )
        }
        composableSharedAxis(SettingsScreens.PasswordCheck.route, axis = SharedAxis.X) {
            PasswordCheckView(onBack = { navController.popBackStack() }, onPasswordSubmitted = {
                withContext(Dispatchers.IO) {
                    delay(1000L)
                }
                Result.success(Unit)
            }, onNavigateAccount = {
                navController.navigate(SettingsScreens.Account.route)
            })
        }
        composableSharedAxis(SettingsScreens.Account.route, axis = SharedAxis.X) {
            AccountView(
                onBack = { navController.popBackStack() },
                onNavigateChangePassword = { navController.navigate(SettingsScreens.ChangePassword.route) },
                onUpdatePhoto = {
                    withContext(Dispatchers.IO) {
                        delay(1000L)
                    }
                    Result.success(Unit)
                },
                onUpdatePersonalInfo = {
                    withContext(Dispatchers.IO) {
                        delay(1000L)
                    }
                    Result.success(Unit)
                },
                onDeleteAccount = {
                    withContext(Dispatchers.IO) {
                        delay(1000L)
                    }
                    Result.success(Unit)
                },
                onNavigateIntro = {
                    onNavigateAuth()
                },
            )
        }
        composableSharedAxis(SettingsScreens.ChangePassword.route, axis = SharedAxis.X) {
            ChangePasswordView(
                onBack = { navController.popBackStack() },
                onPasswordSubmitted = {
                    withContext(Dispatchers.IO) {
                        delay(1000L)
                    }
                    Result.success(Unit)
                },
            )
        }
        composableSharedAxis(SettingsScreens.Viewer.route, axis = SharedAxis.X) {
            val sampleText by SettingModel.getInstance().sampleText.collectAsState()
            val viewerStyle by BookModel.getInstance().pageSplitter.viewerStyle.collectAsState()
            ViewerView(
                sampleText = sampleText,
                viewerStyle = viewerStyle,
                pageSplitter = BookModel.getInstance().pageSplitter,
                onViewerStyleChanged = { BookModel.getInstance().pageSplitter.setViewerStyle(it) },
                onBack = {
                    if (navController.previousBackStackEntry != null) {
                        navController.popBackStack()
                    } else {
                        onBack()
                    }
                },
            )
        }
        composableSharedAxis(SettingsScreens.About.route, axis = SharedAxis.X) {
            val type = it.arguments?.getString("type") ?: ""
            AboutView(
                type = type,
                onBack = { navController.popBackStack() },
            )
        }
    }
}