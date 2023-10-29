package com.example.shareader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.shareader.ui.theme.SHAReaderTheme

class LoginActivity : ComponentActivity() {

//    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {

    super.onCreate(savedInstanceState)
    setContent {
            SHAReaderTheme {
                val loginNavController = rememberNavController()

                NavHost(navController = loginNavController,
                    startDestination = "email"
                ){
                    composable("email") {
                        EmailView(loginNavController)
                    }
//                    composable("sign_in") {
//                        SignInView(loginNavController)
//                    }
                    composable(
                        route = "sign_in/{email}",
                        arguments = listOf(
                            navArgument("email"){type = NavType.StringType},
                        )
                    ) {
                            backStackEntry ->
                        val email = backStackEntry.arguments?.getString("email")
                        if(email!= null){
                            SignInView(loginNavController, email)
                        }
                    }
                    composable("sign_up"){
                        SignUpView(loginNavController)
                    }

                    composable(
                        route = "verify/{email}/{fromSignUp}",
                        arguments = listOf(
                            navArgument("email"){type = NavType.StringType},
                            navArgument("fromSignUp"){type = NavType.BoolType},
                        )
                    ) {
                            backStackEntry ->
                        val email = backStackEntry.arguments?.getString("email")
                        val fromSignUp = backStackEntry.arguments?.getBoolean("fromSignUp")
                        if(email!= null && fromSignUp != null){
                            VerifyEmailView(loginNavController, email, fromSignUp)
                        }
                    }
                    composable("pwd"){
                        ForgotPasswordView(loginNavController)
                    }

                    composable("reset"){
                        ResetPasswordView(loginNavController)
                    }




            }


        }

    }
}}