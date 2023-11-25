package com.example.readability.ui.screens.book

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.readability.ui.animation.SharedAxis
import com.example.readability.ui.animation.composableSharedAxis
import com.example.readability.ui.viewmodels.AddBookViewModel
import com.example.readability.ui.viewmodels.BookListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class BookScreens(val route: String) {
    object BookList : BookScreens("book_list")
    object AddBook : BookScreens("add_book")
}

@Composable
fun BookScreen(onNavigateSettings: () -> Unit = {}, onNavigateViewer: (id: Int) -> Unit = {}) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = BookScreens.BookList.route) {
        composableSharedAxis(BookScreens.BookList.route, axis = SharedAxis.X) {
            val bookListViewModel: BookListViewModel = hiltViewModel()
            val bookCardDataList by bookListViewModel.bookCardDataList.collectAsState()
            BookListView(
                bookCardDataList = bookCardDataList,
                onNavigateAddBook = {
                    navController.navigate(BookScreens.AddBook.route)
                },
                onNavigateSettings = { onNavigateSettings() },
                onLoadContent = { id ->
                    bookListViewModel.getContentData(id)
                },
                onLoadImage = { id ->
                    bookListViewModel.getCoverImageData(id)
                },
                onNavigateViewer = { id ->
                    onNavigateViewer(id)
                },
                onProgressChanged = { id, progress ->
                    bookListViewModel.updateProgress(id, progress)
                },
                onBookDeleted = {id ->
                    bookListViewModel.deleteBook(id)

                },
                onRefresh = {
                    bookListViewModel.updateBookList()
                },
            )
        }
        composableSharedAxis(BookScreens.AddBook.route, axis = SharedAxis.X) {
            val addBookViewModel: AddBookViewModel = hiltViewModel()
            AddBookView(
                onBack = { navController.popBackStack() },
                onBookUploaded = { navController.popBackStack() },
                onAddBookClicked = {
                    withContext(Dispatchers.IO) {
                        addBookViewModel.addBook(it)
                    }
                },
            )
        }
    }
}
