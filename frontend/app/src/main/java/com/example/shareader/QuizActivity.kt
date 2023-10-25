package com.example.shareader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

class QuizActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()

            NavHost(navController = navController,
                startDestination = "quiz"
            ) {
                composable("quiz") {
                    QuizView(navController)
                }
                composable(
                    route = "report/{question}/{answer}",
                    arguments = listOf(
                        navArgument("question"){type = NavType.StringType},
                        navArgument("answer"){type = NavType.StringType}
                    )
                ) {
//                    ReportView(navController)
                    backStackEntry ->
                    val question = backStackEntry.arguments?.getString("question")
                    val answer = backStackEntry.arguments?.getString("answer")
                    if(question!= null && answer!= null){
                        ReportView(navController, question, answer)
                    }
                }
            }
        }
    }
}
