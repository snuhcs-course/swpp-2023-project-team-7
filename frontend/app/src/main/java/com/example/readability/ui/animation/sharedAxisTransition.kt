package com.example.readability.ui.animation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

enum class SharedAxis {
    X, Y, Z
}

fun NavGraphBuilder.composableSharedAxis (
    route: String,
    axis: SharedAxis,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable() (AnimatedContentScope.(NavBackStackEntry) -> Unit)) {
    if (axis == SharedAxis.X) {
        composable(route = route,
            arguments = arguments,
            deepLinks = deepLinks,
            content = content,
            enterTransition = { slideInHorizontally (
                initialOffsetX = { it / 8 },
                animationSpec = tween(300, 0, CubicBezierEasing(0.4f, 0.0f, 0.2f, 1f))
            ) + fadeIn(
                animationSpec = tween(150, 150, CubicBezierEasing(0.4f, 0.0f, 0.2f, 1f))
            ) },
            exitTransition = { slideOutHorizontally (
                targetOffsetX = { -it / 8 },
                animationSpec = tween(300, 0, CubicBezierEasing(0.4f, 0.0f, 0.2f, 1f))
            ) + fadeOut(
                animationSpec = tween(150, 0, CubicBezierEasing(0.4f, 0.0f, 0.2f, 1f))
            ) },
            popEnterTransition = { slideInHorizontally (
                initialOffsetX = { -it / 8 },
                animationSpec = tween(300, 0, CubicBezierEasing(0.4f, 0.0f, 0.2f, 1f))
            ) + fadeIn(
                animationSpec = tween(150, 150, CubicBezierEasing(0.4f, 0.0f, 0.2f, 1f))
            ) },
            popExitTransition = { slideOutHorizontally (
                targetOffsetX = { it / 8 },
                animationSpec = tween(300, 0, CubicBezierEasing(0.4f, 0.0f, 0.2f, 1f))
            ) + fadeOut(
                animationSpec = tween(150, 0, CubicBezierEasing(0.4f, 0.0f, 0.2f, 1f))
            ) },
        )
    } else if (axis == SharedAxis.Y) {
        composable(route = route,
            arguments = arguments,
            deepLinks = deepLinks,
            content = content,
            enterTransition = { slideInVertically (
                initialOffsetY = { it / 8 },
                animationSpec = tween(300, 0, CubicBezierEasing(0.4f, 0.0f, 0.2f, 1f))
            ) + fadeIn(
                animationSpec = tween(150, 150, CubicBezierEasing(0.4f, 0.0f, 0.2f, 1f))
            ) },
            exitTransition = { slideOutVertically (
                targetOffsetY = { -it / 8 },
                animationSpec = tween(300, 0, CubicBezierEasing(0.4f, 0.0f, 0.2f, 1f))
            ) + fadeOut(
                animationSpec = tween(150, 0, CubicBezierEasing(0.4f, 0.0f, 0.2f, 1f))
            ) },
            popEnterTransition = { slideInVertically (
                initialOffsetY = { -it / 8 },
                animationSpec = tween(300, 0, CubicBezierEasing(0.4f, 0.0f, 0.2f, 1f))
            ) + fadeIn(
                animationSpec = tween(150, 150, CubicBezierEasing(0.4f, 0.0f, 0.2f, 1f))
            ) },
            popExitTransition = { slideOutVertically (
                targetOffsetY = { it / 8 },
                animationSpec = tween(300, 0, CubicBezierEasing(0.4f, 0.0f, 0.2f, 1f))
            ) + fadeOut(
                animationSpec = tween(150, 0, CubicBezierEasing(0.4f, 0.0f, 0.2f, 1f))
            ) },
        )
    } else {
        // TODO
    }
}