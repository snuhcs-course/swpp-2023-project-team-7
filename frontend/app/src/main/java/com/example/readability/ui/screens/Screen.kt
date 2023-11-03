package com.example.readability.ui.screens

sealed class Screen (val route: String) {
    object Auth : Screen("auth")
    object Book : Screen("book")
    object Settings : Screen("settings")
    object Viewer : Screen("viewer/{book_id}") {
        fun createRoute(bookId: String) = "viewer/$bookId"
    }
}
