package com.example.readability.ui.screens.book

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.readability.ui.animation.SharedAxis
import com.example.readability.ui.animation.composableSharedAxis

sealed class BookScreen (val route: String) {
    object BookList : BookScreen("book_list")
    object AddBook : BookScreen("add_book")
}

@Composable
fun BookView() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = BookScreen.BookList.route) {
        composableSharedAxis(BookScreen.BookList.route, axis = SharedAxis.X) {
            BookListView()
        }
        composableSharedAxis(BookScreen.AddBook.route, axis = SharedAxis.X) {
            AddBookView()
        }
    }
}