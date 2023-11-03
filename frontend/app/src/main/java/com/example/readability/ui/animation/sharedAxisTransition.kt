package com.example.readability.ui.animation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.readability.ReadabilityApplication

enum class SharedAxis {
    X, Y, Z
}


fun NavGraphBuilder.composableSharedAxis(
    route: String,
    axis: SharedAxis,
    distance: Dp = 30.dp,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable() (AnimatedContentScope.(NavBackStackEntry) -> Unit)
) {

    val distancePx =
        ((ReadabilityApplication.instance?.applicationContext?.resources?.displayMetrics?.density
            ?: 0f) * distance.value).toInt()

    if (axis == SharedAxis.X) {
        composable(
            route = route,
            arguments = arguments,
            deepLinks = deepLinks,
            content = content,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { distancePx },
                    animationSpec = tween(DURATION_LONG, 0, EASING_EMPHASIZED)
                ) + fadeIn(
                    animationSpec = tween(
                        (DURATION_LONG * (1 - FADE_THROUGH_THRESHOLD)).toInt(),
                        (DURATION_LONG * FADE_THROUGH_THRESHOLD).toInt(),
                        EASING_EMPHASIZED
                    )
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -distancePx },
                    animationSpec = tween(DURATION_LONG, 0, EASING_EMPHASIZED)
                ) + fadeOut(
                    animationSpec = tween(
                        (DURATION_LONG * (1 - FADE_THROUGH_THRESHOLD)).toInt(), 0, EASING_EMPHASIZED
                    )
                )
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -distancePx },
                    animationSpec = tween(DURATION_LONG, 0, EASING_EMPHASIZED)
                ) + fadeIn(
                    animationSpec = tween(
                        (DURATION_LONG * (1 - FADE_THROUGH_THRESHOLD)).toInt(),
                        (DURATION_LONG * FADE_THROUGH_THRESHOLD).toInt(),
                        EASING_EMPHASIZED
                    )
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { distancePx }, animationSpec = tween(DURATION_LONG, 0, EASING_EMPHASIZED)
                ) + fadeOut(
                    animationSpec = tween(
                        (DURATION_LONG * (1 - FADE_THROUGH_THRESHOLD)).toInt(), 0, EASING_EMPHASIZED
                    )
                )
            },
        )
    } else if (axis == SharedAxis.Y) {
        // TODO
    } else {
        // TODO
    }
}