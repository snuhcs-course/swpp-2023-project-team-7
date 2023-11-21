package com.example.readability.ui.screens.viewer

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.readability.ui.animation.SharedAxis
import com.example.readability.ui.animation.composableSharedAxis
import com.example.readability.ui.viewmodels.QuizViewModel
import com.example.readability.ui.viewmodels.SummaryViewModel
import com.example.readability.ui.viewmodels.ViewerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

sealed class ViewerScreens(val route: String) {
    object Viewer : ViewerScreens("viewer")
    object Quiz : ViewerScreens("quiz")
    object QuizReport : ViewerScreens("quiz/report/{question}/{answer}") {
        fun createRoute(question: String, answer: String) = "quiz/report/$question/$answer"
    }

    object Summary : ViewerScreens("summary")
}

@Composable
fun ViewerScreen(id: Int, onNavigateSettings: () -> Unit, onBack: () -> Unit) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = ViewerScreens.Viewer.route) {
        composableSharedAxis(ViewerScreens.Viewer.route, axis = SharedAxis.X) {
            val viewerViewModel: ViewerViewModel = hiltViewModel()
            val quizViewModel: QuizViewModel = hiltViewModel()
            val summaryViewModel: SummaryViewModel = hiltViewModel()
            val bookData by viewerViewModel.getBookData(id).collectAsState(initial = null)
            val pageSplitData by viewerViewModel.pageSplitData.collectAsState(initial = null)
            val isDarkTheme = isSystemInDarkTheme()
            ViewerView(bookData = bookData,
                pageSplitData = pageSplitData,
                onBack = onBack,
                onNavigateQuiz = {
                    if (bookData != null) {
                        quizViewModel.loadQuiz(id, bookData!!.progress)
                        navController.navigate(ViewerScreens.Quiz.route)
                    }
                },
                onNavigateSettings = { onNavigateSettings() },
                onProgressChange = {
                    viewerViewModel.setProgress(id, it)
                },
                onNavigateSummary = {
                    if (bookData != null) {
                        summaryViewModel.loadSummary(id, bookData!!.progress)
                        navController.navigate(ViewerScreens.Summary.route)
                    }
                },
                onPageSizeChanged = { width, height ->
                    viewerViewModel.setPageSize(id, width, height)
                },
                onPageDraw = { canvas, pageIndex ->
                    viewerViewModel.drawPage(id, canvas, pageIndex, isDarkTheme)
                })
        }
        composableSharedAxis(ViewerScreens.Quiz.route, axis = SharedAxis.X) {
            val quizViewModel: QuizViewModel = hiltViewModel()
            val quizList by quizViewModel.quizList.collectAsState()
            val quizSize by quizViewModel.quizSize.collectAsState()
            val quizLoadState by quizViewModel.quizLoadState.collectAsState()
            QuizView(
                quizList = quizList,
                quizSize = quizSize,
                quizLoadState = quizLoadState,
                onBack = { navController.popBackStack() },
                onNavigateReport = {
                    navController.navigate(
                        ViewerScreens.QuizReport.createRoute(
                            question = quizList[it].question,
                            answer = quizList[it].answer
                        )
                    )
                },
            )
        }
        composableSharedAxis(ViewerScreens.QuizReport.route, axis = SharedAxis.X) {
            QuizReportView(question = it.arguments?.getString("question") ?: "",
                answer = it.arguments?.getString("answer") ?: "",
                onBack = { navController.popBackStack() },
                onReport = {
                    // TODO: send report
                    withContext(Dispatchers.IO) {
                        delay(1500L)
                    }
                    Result.success(Unit)
                })
        }
        composableSharedAxis(ViewerScreens.Summary.route, axis = SharedAxis.X) {
            val summaryViewModel: SummaryViewModel = hiltViewModel()
            val summary by summaryViewModel.summary.collectAsState()
            SummaryView(summary = summary, onBack = {
                navController.popBackStack()
            })
        }
    }
}