package com.example.readability.ui.animation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

fun NavGraphBuilder.composableFadeThrough(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content:
    @Composable()
    (AnimatedContentScope.(NavBackStackEntry) -> Unit),
) {
    composable(
        route = route,
        arguments = arguments,
        deepLinks = deepLinks,
        content = content,
        enterTransition = {
            scaleIn(
                animationSpec = tween(DURATION_EMPHASIZED, 0, EASING_EMPHASIZED), initialScale = DEFAULT_START_SCALE,
            ) + fadeIn(
                animationSpec = tween(
                    (DURATION_EMPHASIZED * (1 - FADE_THROUGH_THRESHOLD)).toInt(),
                    (DURATION_EMPHASIZED * FADE_THROUGH_THRESHOLD).toInt(),
                    EASING_EMPHASIZED,
                ),
            )
        },
        exitTransition = {
            scaleOut(
                animationSpec = tween(DURATION_EMPHASIZED, 0, EASING_EMPHASIZED), targetScale = DEFAULT_START_SCALE,
            ) + fadeOut(
                animationSpec = tween(
                    (DURATION_EMPHASIZED * FADE_THROUGH_THRESHOLD).toInt(), 0, EASING_EMPHASIZED,
                ),
            )
        },
        popEnterTransition = {
            scaleIn(
                animationSpec = tween(DURATION_EMPHASIZED, 0, EASING_EMPHASIZED), initialScale = DEFAULT_START_SCALE,
            ) + fadeIn(
                animationSpec = tween(
                    (DURATION_EMPHASIZED * (1 - FADE_THROUGH_THRESHOLD)).toInt(),
                    (DURATION_EMPHASIZED * FADE_THROUGH_THRESHOLD).toInt(),
                    EASING_EMPHASIZED,
                ),
            )
        },
        popExitTransition = {
            scaleOut(

                animationSpec = tween(DURATION_EMPHASIZED, 0, EASING_EMPHASIZED), targetScale = DEFAULT_START_SCALE,
            ) + fadeOut(
                animationSpec = tween(
                    (DURATION_EMPHASIZED * FADE_THROUGH_THRESHOLD).toInt(), 0, EASING_EMPHASIZED,
                ),
            )
        },
    )
}
