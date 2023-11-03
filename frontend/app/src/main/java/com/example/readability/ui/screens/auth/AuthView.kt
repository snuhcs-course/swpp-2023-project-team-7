package com.example.readability.ui.screens.auth

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.readability.ui.animation.SharedAxis
import com.example.readability.ui.animation.composableSharedAxis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

sealed class AuthScreen(val route: String) {
    object Intro : AuthScreen("intro")
    object SignIn : AuthScreen("sign_in/{email}") {
        fun createRoute(email: String) = "sign_in/$email"
    }

    object SignUp : AuthScreen("sign_up")
    object VerifyEmail : AuthScreen("verify_email/{email}/{fromSignUp}") {
        fun createRoute(email: String, fromSignUp: Boolean) = "verify_email/$email/$fromSignUp"
    }

    object ResetPassword : AuthScreen("reset_password")
    object ForgotPassword : AuthScreen("forgot_password")
    object Email : AuthScreen("email")
}


@Composable
fun AuthView(
    onNavigateBookList: () -> Unit = {},
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = AuthScreen.Intro.route) {
        composableSharedAxis(AuthScreen.Intro.route, axis = SharedAxis.X) {
            IntroView(
                onContinueWithEmailClicked = { navController.navigate(AuthScreen.Email.route) },
            )
        }
        composableSharedAxis(AuthScreen.Email.route, axis = SharedAxis.X) {
            EmailView(
                onBack = { navController.popBackStack() },
                onNavigateSignIn = { navController.navigate(AuthScreen.SignIn.createRoute(it)) },
                onNavigateSignUp = { navController.navigate(AuthScreen.SignUp.route) },
                onNavigateForgotPassword = { navController.navigate(AuthScreen.ForgotPassword.route) },
            )
        }
        composableSharedAxis(AuthScreen.SignIn.route, axis = SharedAxis.X) {
            SignInView(
                email = it.arguments?.getString("email") ?: "",
                onBack = { navController.popBackStack() },
                onPasswordSubmitted = {
                    // TODO: post login request and return result
                    withContext(Dispatchers.IO) {
                        delay(2000L)
                    }
                    if (it == "testtest") {
                        onNavigateBookList()
                        Result.success(Unit)
                    } else {
                        Result.failure(Exception("Password is incorrect"))
                    }
                },
                onNavigateForgotPassword = { navController.navigate(AuthScreen.ForgotPassword.route) },
            )
        }
        composableSharedAxis(AuthScreen.SignUp.route, axis = SharedAxis.X) {
            SignUpView(
                onBack = { navController.popBackStack() },
                onSubmitted = {
                    // TODO: send fields and return
                    withContext(Dispatchers.IO) {
                        delay(1500L)
                    }
                    Result.success(Unit)
                },
                onNavigateVerify = {
                    navController.navigate(
                        AuthScreen.VerifyEmail.createRoute(
                            it, true
                        )
                    )
                },
            )
        }
        composableSharedAxis(AuthScreen.ForgotPassword.route, axis = SharedAxis.X) {
            ForgotPasswordView(
                onBack = { navController.popBackStack() },
                onEmailSubmitted = {
                    withContext(Dispatchers.IO) {
                        delay(1000L)
                    }
                    Result.success(Unit)
                },
                onNavigateVerify = {
                    navController.navigate(
                        AuthScreen.VerifyEmail.createRoute(
                            it, false
                        )
                    )
                },
            )
        }
        composableSharedAxis(AuthScreen.VerifyEmail.route,
            axis = SharedAxis.X,
            arguments = listOf(navArgument("fromSignUp") { defaultValue = false })) {
            VerifyEmailView(email = it.arguments?.getString("email") ?: "",
                fromSignUp = it.arguments?.getBoolean("fromSignUp") ?: false,
                onBack = { navController.popBackStack() },
                onNavigateBookList = { onNavigateBookList() },
                onNavigateResetPassword = { navController.navigate(AuthScreen.ResetPassword.route) })
        }
        composableSharedAxis(AuthScreen.ResetPassword.route, axis = SharedAxis.X) {
            ResetPasswordView(
                onBack = { navController.popBackStack() },
                onNavigateEmail = { navController.navigate(AuthScreen.Email.route) },
            )
        }

    }
}