package com.example.readability.ui.screens.auth

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.readability.ui.animation.SharedAxis
import com.example.readability.ui.animation.composableSharedAxis

sealed class AuthScreen(val route: String) {
    object Intro : AuthScreen("intro")
    object SignIn : AuthScreen("sign_in/{email}") {
        fun createRoute(email: String) = "sign_in/$email"
    }
    object SignUp : AuthScreen("sign_up")
    object VerifyEmail : AuthScreen("verify_email/{email}") {
        fun createRoute(email: String) = "verify_email/$email"
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
                onPasswordSubmitted = { navController.navigate(AuthScreen.VerifyEmail.route) },
                onNavigateForgotPassword = { navController.navigate(AuthScreen.ForgotPassword.route) },
            )
        }
        composableSharedAxis(AuthScreen.SignUp.route, axis = SharedAxis.X) {
            SignUpView(
                onBack = { navController.popBackStack() },
                onNavigateVerify = { navController.navigate(AuthScreen.VerifyEmail.createRoute(it)) },
            )
        }
        composableSharedAxis(AuthScreen.ForgotPassword.route, axis = SharedAxis.X) {
            ForgotPasswordView(
                onBack = { navController.popBackStack() },
                onNavigateVerify = { navController.navigate(AuthScreen.VerifyEmail.createRoute(it)) },
            )
        }
        composableSharedAxis(AuthScreen.VerifyEmail.route, axis = SharedAxis.X) {
            VerifyEmailView(
                email = it.arguments?.getString("email") ?: "",
                fromSignUp = false,
                onBack = { navController.popBackStack() },
                onNavigateBookList = { },
                onNavigateResetPassword = { }
            )
        }
        composableSharedAxis(AuthScreen.ResetPassword.route, axis = SharedAxis.X) {
            ResetPasswordView(
                onBack = { navController.popBackStack() },
                onNavigateEmail = { navController.navigate(AuthScreen.Email.route) },
            )
        }

    }
}