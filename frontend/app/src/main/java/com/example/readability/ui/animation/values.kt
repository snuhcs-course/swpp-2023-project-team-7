package com.example.readability.ui.animation

import android.graphics.Path
import android.view.animation.PathInterpolator
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing

const val FADE_THROUGH_THRESHOLD = 0.35f
const val DEFAULT_START_SCALE = 0.92f

const val DURATION_LONG = 300

// M 0,0 C 0.05, 0, 0.133333, 0.06, 0.166666, 0.4 C 0.208333, 0.82, 0.25, 1, 1, 1
val EASING_EMPHASIZED_PATH = Path().apply {
    moveTo(0f, 0f)
    cubicTo(0.05f, 0f, 0.133333f, 0.06f, 0.166666f, 0.4f)
    cubicTo(0.208333f, 0.82f, 0.25f, 1f, 1f, 1f)
}
val EASING_EMPHASIZED_PATH_INTERPOLATOR = PathInterpolator(EASING_EMPHASIZED_PATH)
val EASING_EMPHASIZED = Easing {
    EASING_EMPHASIZED_PATH_INTERPOLATOR.getInterpolation(it)
}
val DURATION_EMPHASIZED = 500

val EASING_EMPHASIZED_ACCELERATE = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)
val DURATION_EMPHASIZED_ACCELERATE = 200

val EASING_EMPHASIZED_DECELERATE = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
val DURATION_EMPHASIZED_DECELERATE = 400

val EASING_LEGACY = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1f)