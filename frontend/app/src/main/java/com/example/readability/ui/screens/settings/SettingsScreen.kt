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
import com.example.readability.ui.viewmodels.BookListViewModel
import com.example.readability.ui.viewmodels.SettingViewModel
import com.example.readability.ui.viewmodels.UserViewModel
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
    startDestination: String = SettingsScreens.Settings.route,
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = startDestination) {
        composableSharedAxis(SettingsScreens.Settings.route, axis = SharedAxis.X) {
            val userViewModel: UserViewModel = hiltViewModel()
            val bookListViewModel: BookListViewModel = hiltViewModel()
            // TODO: put correct user info to SettingsView
            SettingsView(
                onSignOut = {
                    withContext(Dispatchers.IO) {
                        userViewModel.signOut()
                        bookListViewModel.clearBookList()
                    }
                    Result.success(Unit)
                },
                onBack = { onBack() },
                onNavigatePasswordCheck = { navController.navigate(SettingsScreens.Account.route) },
                onNavigateViewer = { navController.navigate(SettingsScreens.Viewer.route) },
                onNavigateIntro = { onNavigateAuth() },
            )
        }
        composableSharedAxis(SettingsScreens.PasswordCheck.route, axis = SharedAxis.X) {
            val userViewModel: UserViewModel = hiltViewModel()
            PasswordCheckView(onBack = { navController.popBackStack() }, onPasswordSubmitted = {
                withContext(Dispatchers.IO) {
                    // TODO: check password again using userViewModel
                    delay(1000L)
                    Result.success(Unit)
                }
            }, onNavigateAccount = {
                navController.navigate(SettingsScreens.Account.route)
            })
        }
        composableSharedAxis(SettingsScreens.Account.route, axis = SharedAxis.X) {
            val userViewModel: UserViewModel = hiltViewModel()
            // TODO: put correct user info to AccountView
            AccountView(
                onBack = { navController.popBackStack() },
                onNavigateChangePassword = { navController.navigate(SettingsScreens.ChangePassword.route) },
                onUpdatePhoto = {
                    withContext(Dispatchers.IO) {
                        // TODO: update photo using userViewModel
                        delay(1000L)
                        Result.success(Unit)
                    }
                },
                onUpdatePersonalInfo = {
                    withContext(Dispatchers.IO) {
                        // TODO: update personal info using userViewModel
                        delay(1000L)
                        Result.success(Unit)
                    }
                },
                onDeleteAccount = {
                    withContext(Dispatchers.IO) {
                        // TODO: delete account using userViewModel
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
            ChangePasswordView(
                onBack = { navController.popBackStack() },
                onPasswordSubmitted = { newPassword ->
                    withContext(Dispatchers.IO) {
                        // TODO: change password using userViewModel
                        delay(1000L)
                        Result.success(Unit)
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
