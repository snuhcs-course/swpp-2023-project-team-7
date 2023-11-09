package com.example.readability.ui.screens.book

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.readability.ui.animation.SharedAxis
import com.example.readability.ui.animation.composableSharedAxis
import com.example.readability.ui.viewmodels.AddBookViewModel
import com.example.readability.ui.viewmodels.BookListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Thread.sleep

sealed class BookScreens(val route: String) {
    object BookList : BookScreens("book_list")
    object AddBook : BookScreens("add_book")
}

@Composable
fun BookScreen(
    onNavigateSettings: () -> Unit = {}, onNavigateViewer: (id: String) -> Unit = {}
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = BookScreens.BookList.route) {
        composableSharedAxis(BookScreens.BookList.route, axis = SharedAxis.X) {
            val bookListViewModel: BookListViewModel = viewModel()
            val bookCardDataList by bookListViewModel.bookCardDataList.collectAsState(initial = emptyList())
            BookListView(
                bookCardDataList = bookCardDataList,
                onNavigateAddBook = {
                    navController.navigate(BookScreens.AddBook.route)
                },
                onNavigateSettings = { /*onNavigateSettings()*/ },
                onNavigateViewer = onNavigateViewer,
            )
        }
        composableSharedAxis(BookScreens.AddBook.route, axis = SharedAxis.X) {
            val addBookViewModel: AddBookViewModel = viewModel()
            AddBookView(
                onBack = { navController.popBackStack() },
                onBookUploaded = { navController.popBackStack() },
                onAddBookClicked = {
                    // addBookViewModel.addBook(it) -> TODO: Implement this after adding UserModel
                    withContext(Dispatchers.IO) {
                        sleep(1000L)
                    }
                    Result.success(Unit)
                },
            )
        }
    }
}