package com.example.readability.ui.screens.settings

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.readability.ui.animation.SharedAxis
import com.example.readability.ui.animation.composableSharedAxis
import com.example.readability.ui.viewmodels.SettingViewModel
import com.example.readability.ui.viewmodels.UserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

sealed class SettingsScreens(val route: String) {
    object Settings : SettingsScreens("settings")
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
    startDestination: String = SettingsScreens.Settings.route,
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = startDestination) {
        composableSharedAxis(SettingsScreens.Settings.route, axis = SharedAxis.X) {
            val userViewModel: UserViewModel = hiltViewModel()
            val user by userViewModel.user.collectAsState()

            SettingsView(
                username = user?.userName ?: "",
                onBack = { onBack() },
                onNavigateAccountSetting = { navController.navigate(SettingsScreens.Account.route) },
                onNavigateViewer = { navController.navigate(SettingsScreens.Viewer.route) },
            )
        }
        composableSharedAxis(SettingsScreens.Account.route, axis = SharedAxis.X) {
            val userViewModel: UserViewModel = hiltViewModel()
            val user by userViewModel.user.collectAsState()
            AccountView(
                email = user?.userEmail ?: "",
                username = user?.userName ?: "",
                onBack = { navController.popBackStack() },
                onNavigateChangePassword = { navController.navigate(SettingsScreens.ChangePassword.route) },
                onSignOut = {
                    withContext(Dispatchers.IO) {
                        userViewModel.signOut()
                    }
                    Result.success(Unit)
                },
                onDeleteAccount = {
                    withContext(Dispatchers.IO) {
                        // TODO: delete account using userViewModel
                        userViewModel.signOut()
                        delay(1000L)
                        Result.success(Unit)
                    }
                },
                onNavigateIntro = {
                    onNavigateAuth()
                },
            )
        }
        composableSharedAxis(SettingsScreens.ChangePassword.route, axis = SharedAxis.X) {
            val userViewModel: UserViewModel = hiltViewModel()
            ChangePasswordView(
                onBack = { navController.popBackStack() },
                onPasswordSubmitted = { newPassword ->
                    withContext(Dispatchers.IO) {
                        userViewModel.changePassword(newPassword)
                    }
                },
            )
        }
        composableSharedAxis(SettingsScreens.Viewer.route, axis = SharedAxis.X) {
            val settingViewModel: SettingViewModel = hiltViewModel()
            val viewerStyle by settingViewModel.viewerStyle.collectAsState()
            val isDarkMode = isSystemInDarkTheme()
            ViewerView(
                viewerStyle = viewerStyle,
                onViewerStyleChanged = { settingViewModel.updateViewerStyle(it) },
                onDrawPage = { canvas, width ->
                    settingViewModel.drawPage(canvas, width, isDarkMode)
                },
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
