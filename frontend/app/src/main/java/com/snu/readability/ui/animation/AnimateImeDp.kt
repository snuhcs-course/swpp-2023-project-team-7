package com.snu.readability.ui.animation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun animateImeDp(label: String): State<Dp> {
    var lastImeBottom by remember { mutableIntStateOf(0) }
    var isImeVisible by remember { mutableStateOf(false) }
    if (WindowInsets.isImeVisible) {
        val imeBottom = WindowInsets.ime.getBottom(LocalDensity.current)
        if (imeBottom > lastImeBottom && !isImeVisible) {
            isImeVisible = true
        } else if (imeBottom < lastImeBottom && isImeVisible) {
            isImeVisible = false
        }
        lastImeBottom = imeBottom
    }
    return animateDpAsState(targetValue = if (isImeVisible) 0.dp else 1.dp, label = label)
}
