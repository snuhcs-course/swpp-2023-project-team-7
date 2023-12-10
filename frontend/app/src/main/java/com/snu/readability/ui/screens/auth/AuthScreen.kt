package com.snu.readability.ui.screens.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.snu.readability.ui.animation.SharedAxis
import com.snu.readability.ui.animation.composableSharedAxis
import com.snu.readability.ui.viewmodels.UserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.lang.Thread.sleep

sealed class AuthScreens(val route: String) {
    object Intro : AuthScreens("intro")
    object SignIn : AuthScreens("sign_in")

    object SignUp : AuthScreens("sign_up")
    object VerifyEmail : AuthScreens("verify_email/{fromSignUp}") {
        fun createRoute(fromSignUp: Boolean) = "verify_email/$fromSignUp"
    }

    object ResetPassword : AuthScreens("reset_password")
    object ForgotPassword : AuthScreens("forgot_password")
    object Email : AuthScreens("email")
}

@Composable
fun AuthScreen(navController: NavHostController = rememberNavController(), onNavigateBookList: () -> Unit = {}) {
    var email by remember { mutableStateOf("") }
    NavHost(navController = navController, startDestination = AuthScreens.Intro.route) {
        composableSharedAxis(AuthScreens.Intro.route, axis = SharedAxis.X) {
            IntroView(
                onContinueWithEmailClicked = { navController.navigate(AuthScreens.Email.route) },
            )
        }
        composableSharedAxis(AuthScreens.Email.route, axis = SharedAxis.X) {
            EmailView(
                email = email,
                onEmailChanged = { email = it },
                onBack = { navController.popBackStack() },
                onNavigateSignIn = { navController.navigate(AuthScreens.SignIn.route) },
                onNavigateSignUp = { navController.navigate(AuthScreens.SignUp.route) },
            )
        }
        composableSharedAxis(AuthScreens.SignIn.route, axis = SharedAxis.X) {
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
                    onNavigateBookList()
                },
            )
        }
        composableSharedAxis(AuthScreens.ForgotPassword.route, axis = SharedAxis.X) {
            ForgotPasswordView(
                email = email,
                onEmailChanged = { email = it },
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
            ),
        ) {
            VerifyEmailView(
                email = email,
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
