package com.example.readability.ui.screens.viewer

import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.readability.ui.animation.SharedAxis
import com.example.readability.ui.animation.composableSharedAxis
import com.example.readability.ui.viewmodels.NetworkStatusViewModel
import com.example.readability.ui.viewmodels.QuizViewModel
import com.example.readability.ui.viewmodels.SummaryViewModel
import com.example.readability.ui.viewmodels.ViewerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

sealed class ViewerScreens(val route: String) {
    object Viewer : ViewerScreens("viewer")
    object Quiz : ViewerScreens("quiz")
    object QuizReport : ViewerScreens("quiz/report?question={question}&answer={answer}") {
        fun createRoute(question: String, answer: String) = "quiz/report?question=$question&answer=$answer"
    }

    object Summary : ViewerScreens("summary")
}

@Composable
fun ViewerScreen(
    id: Int,
    navController: NavHostController = rememberNavController(),
    onNavigateSettings: () -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val immersiveModeEnabled = navBackStackEntry?.destination?.route == ViewerScreens.Viewer.route
    var firstImmersiveMode by remember { mutableStateOf(true) }

    LaunchedEffect(immersiveModeEnabled) {
        if (firstImmersiveMode) {
            firstImmersiveMode = false
            return@LaunchedEffect
        }
        val activity = context.findActivity() ?: return@LaunchedEffect
        if (immersiveModeEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                activity.window.insetsController?.let {
                    it.hide(WindowInsets.Type.systemBars())
                    it.systemBarsBehavior =
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                @Suppress("DEPRECATION")
                activity.window.decorView.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                activity.window.insetsController?.show(WindowInsets.Type.systemBars())
            } else {
                @Suppress("DEPRECATION")
                activity.window.decorView.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_VISIBLE
            }
        }
    }

    NavHost(navController = navController, startDestination = ViewerScreens.Viewer.route) {
        composableSharedAxis(ViewerScreens.Viewer.route, axis = SharedAxis.X) {
            val viewerViewModel: ViewerViewModel = hiltViewModel()
            val quizViewModel: QuizViewModel = hiltViewModel()
            val summaryViewModel: SummaryViewModel = hiltViewModel()
            val networkStatusViewModel: NetworkStatusViewModel = hiltViewModel()
            val bookData by viewerViewModel.getBookData(id).collectAsState(initial = null)
            val pageSplitData by viewerViewModel.pageSplitData.collectAsState(initial = null)
            val isDarkTheme = isSystemInDarkTheme()
            val isNetworkConnected by networkStatusViewModel.connectedState.collectAsState()
            ViewerView(
                bookData = bookData,
                pageSplitData = pageSplitData,
                onBack = onBack,
                isNetworkConnected = isNetworkConnected,
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
                },
            )
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
                            answer = quizList[it].answer,
                        ),
                    )
                },
            )
        }
        composableSharedAxis(ViewerScreens.QuizReport.route, axis = SharedAxis.X) {
            QuizReportView(
                question = it.arguments?.getString("question") ?: "",
                answer = it.arguments?.getString("answer") ?: "",
                onBack = { navController.popBackStack() },
                onReport = {
                    // TODO: send report
                    withContext(Dispatchers.IO) {
                        delay(1500L)
                    }
                    Result.success(Unit)
                },
            )
        }
        composableSharedAxis(ViewerScreens.Summary.route, axis = SharedAxis.X) {
            val summaryViewModel: SummaryViewModel = hiltViewModel()
            val summary by summaryViewModel.summary.collectAsState()
            val viewerStyle by summaryViewModel.viewerStyle.collectAsState()
            val typeface by summaryViewModel.typeface.collectAsState()
            val referenceLineHeight by summaryViewModel.referenceLineHeight.collectAsState()
            SummaryView(
                summary = summary,
                viewerStyle = viewerStyle,
                typeface = typeface,
                referenceLineHeight = referenceLineHeight,
                onBack = {
                    navController.popBackStack()
                },
            )
        }
    }
}
