package com.example.readability.ui.screens

sealed class Screens (val route: String) {
    object Auth : Screens("auth")
    object Book : Screens("book")
    object Settings : Screens("settings")
    object Viewer : Screens("viewer/{book_id}") {
        fun createRoute(bookId: String) = "viewer/$bookId"
    }
}
