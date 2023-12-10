package com.snu.readability.ui.animation

import android.content.res.Resources
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
import kotlin.math.roundToInt

enum class SharedAxis {
    X,
    Y,
    Z,
}
fun lerp(startFraction: Float, endFraction: Float, fraction: Float): Float {
    if (fraction <= startFraction) {
        return 0f
    }
    if (fraction >= endFraction) {
        return 1f
    }
    return (fraction - startFraction) / (endFraction - startFraction)
}

fun NavGraphBuilder.composableSharedAxis(
    route: String,
    axis: SharedAxis,
    distance: Dp = 30.dp,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content:
    @Composable()
    (AnimatedContentScope.(NavBackStackEntry) -> Unit),
) {
    val distancePx = (Resources.getSystem().displayMetrics.density * distance.value).roundToInt()

    if (axis == SharedAxis.X) {
        composable(
            route = route,
            arguments = arguments,
            deepLinks = deepLinks,
            content = content,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { distancePx },
                    animationSpec = tween(
                        DURATION_EMPHASIZED, 0, EASING_EMPHASIZED,
                    ),
                ) + fadeIn(
                    animationSpec = tween(
                        DURATION_EMPHASIZED,
                    ) {
                        lerp(FADE_THROUGH_THRESHOLD, 1f, EASING_EMPHASIZED.transform(it))
                    },
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -distancePx },
                    animationSpec = tween(
                        DURATION_EMPHASIZED, 0, EASING_EMPHASIZED,
                    ),
                ) + fadeOut(
                    animationSpec = tween(
                        DURATION_EMPHASIZED,
                    ) {
                        lerp(0f, FADE_THROUGH_THRESHOLD, EASING_EMPHASIZED.transform(it))
                    },
                )
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -distancePx },
                    animationSpec = tween(
                        DURATION_EMPHASIZED, 0, EASING_EMPHASIZED,
                    ),
                ) + fadeIn(
                    animationSpec = tween(
                        DURATION_EMPHASIZED,
                    ) {
                        lerp(FADE_THROUGH_THRESHOLD, 1f, EASING_EMPHASIZED.transform(it))
                    },
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { distancePx },
                    animationSpec = tween(
                        DURATION_EMPHASIZED, 0, EASING_EMPHASIZED,
                    ),
                ) + fadeOut(
                    animationSpec = tween(
                        DURATION_EMPHASIZED,
                    ) {
                        lerp(0f, FADE_THROUGH_THRESHOLD, EASING_EMPHASIZED.transform(it))
                    },
                )
            },
        )
    } else if (axis == SharedAxis.Y) {
        // TODO
    } else {
        // TODO
    }
}
