package com.example.readability.ui.screens.auth

import android.util.Base64
import android.util.Base64.URL_SAFE
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.readability.ui.animation.SharedAxis
import com.example.readability.ui.animation.composableSharedAxis
import com.example.readability.ui.viewmodels.UserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.lang.Thread.sleep

sealed class AuthScreens(val route: String) {
    object Intro : AuthScreens("intro")
    object SignIn : AuthScreens("sign_in/{email}") {
        fun createRoute(email: String) = "sign_in/$email"
    }

    object SignUp : AuthScreens("sign_up")
    object VerifyEmail : AuthScreens("verify_email/{email}/{fromSignUp}") {
        fun createRoute(email: String, fromSignUp: Boolean) = "verify_email/${
            Base64.encodeToString(email.toByteArray(), URL_SAFE).trim()
        }/$fromSignUp"
    }

    object ResetPassword : AuthScreens("reset_password")
    object ForgotPassword : AuthScreens("forgot_password")
    object Email : AuthScreens("email")
}

@Composable
fun AuthScreen(navController: NavHostController = rememberNavController(), onNavigateBookList: () -> Unit = {}) {
    NavHost(navController = navController, startDestination = AuthScreens.Intro.route) {
        composableSharedAxis(AuthScreens.Intro.route, axis = SharedAxis.X) {
            IntroView(
                onContinueWithEmailClicked = { navController.navigate(AuthScreens.Email.route) },
            )
        }
        composableSharedAxis(AuthScreens.Email.route, axis = SharedAxis.X) {
            EmailView(
                onBack = { navController.popBackStack() },
                onNavigateSignIn = { navController.navigate(AuthScreens.SignIn.createRoute(it)) },
                onNavigateSignUp = { navController.navigate(AuthScreens.SignUp.route) },
                onNavigateForgotPassword = { navController.navigate(AuthScreens.ForgotPassword.route) },
            )
        }
        composableSharedAxis(AuthScreens.SignIn.route, axis = SharedAxis.X) {
            val email = it.arguments?.getString("email") ?: ""
            val userViewModel: UserViewModel = hiltViewModel()
            SignInView(
                email = email,
                onBack = { navController.popBackStack() },
                onPasswordSubmitted = { password ->
                    withContext(Dispatchers.IO) {
                        userViewModel.signIn(email, password)
                    }
                },
                onNavigateBookList = { onNavigateBookList() },
                onNavigateForgotPassword = { navController.navigate(AuthScreens.ForgotPassword.route) },
            )
        }
        composableSharedAxis(AuthScreens.SignUp.route, axis = SharedAxis.X) {
            val userViewModel: UserViewModel = hiltViewModel()
            SignUpView(
                onBack = { navController.popBackStack() },
                onSubmitted = { email, username, password ->
                    withContext(Dispatchers.IO) {
                        userViewModel.signUp(email, username, password)
                    }
                },
                onNavigateVerify = {
                    // TODO: implement verification on backend
//                    navController.navigate(
//                        AuthScreens.VerifyEmail.createRoute(
//                            it, true
//                        )
//                    )
                    onNavigateBookList()
                },
            )
        }
        composableSharedAxis(AuthScreens.ForgotPassword.route, axis = SharedAxis.X) {
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
                        AuthScreens.VerifyEmail.createRoute(
                            it,
                            false,
                        ),
                    )
                },
            )
        }
        composableSharedAxis(
            AuthScreens.VerifyEmail.route,
            axis = SharedAxis.X,
            arguments = listOf(
                navArgument("fromSignUp") { defaultValue = false },
                navArgument("email") { defaultValue = "" },
            ),
        ) {
            VerifyEmailView(
                email = String(
                    Base64.decode(
                        it.arguments?.getString("email") ?: "",
                        URL_SAFE,
                    ),
                ),
                fromSignUp = it.arguments?.getBoolean("fromSignUp") ?: false,
                onBack = { navController.popBackStack() },
                onNavigateBookList = { onNavigateBookList() },
                onNavigateResetPassword = { navController.navigate(AuthScreens.ResetPassword.route) },
                onVerificationCodeSubmitted = {
                    withContext(Dispatchers.IO) {
                        sleep(1000)
                    }
                    Result.success(Unit)
                },
            )
        }
        composableSharedAxis(AuthScreens.ResetPassword.route, axis = SharedAxis.X) {
            ResetPasswordView(
                onBack = { navController.popBackStack() },
                onNavigateEmail = { navController.navigate(AuthScreens.Email.route) },
                onPasswordSubmitted = {
                    withContext(Dispatchers.IO) {
                        sleep(1000)
                    }
                    Result.success(Unit)
                },
            )
        }
    }
}
