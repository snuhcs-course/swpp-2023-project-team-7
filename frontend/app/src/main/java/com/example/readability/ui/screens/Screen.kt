package com.example.readability.ui.screens

sealed class Screen (val route: String) {
    object Auth : Screen("auth")
    object Books : Screen("books")
    object Settings : Screen("settings")
    object Viewer : Screen("viewer")
}
