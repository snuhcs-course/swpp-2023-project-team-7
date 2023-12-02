package com.example.readability.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times

@Composable
fun CircularProgressIndicatorInButton(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onPrimary,
    strokeWidth: Dp = 2.dp,
    trackColor: Color = ProgressIndicatorDefaults.circularTrackColor,
    strokeCap: StrokeCap = ProgressIndicatorDefaults.CircularIndeterminateStrokeCap,
) {
    CircularProgressIndicator(
        modifier = modifier.size(24.dp),
        strokeWidth = strokeWidth,
        color = color,
        trackColor = trackColor,
        strokeCap = strokeCap,
    )
}

@Composable
fun RoundedRectButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    imeAnimation: State<Dp>? = null,
    loading: Boolean = false,
    content: @Composable RowScope.() -> Unit,
) {
    val disabledContainerColor by animateColorAsState(
        targetValue = if (loading) {
            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.48f)
                .compositeOver(MaterialTheme.colorScheme.primary)
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        },
        label = "RoundedRectButton.DisabledContainerColor",
    )

    val imeDp = imeAnimation?.value ?: 1.dp
    val imePaddingDp = imeAnimation?.value ?: 0.dp
    Button(
        onClick = onClick,
        modifier = modifier
            .padding(16 * imePaddingDp)
            .height(48.dp),
        enabled = enabled && !loading,
        shape = RoundedCornerShape(12 * imeDp),
        colors = ButtonDefaults.buttonColors(
            disabledContainerColor = disabledContainerColor,
        ),
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = if (loading) {
            {
                CircularProgressIndicatorInButton(
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        } else {
            content
        },
    )
}

@Composable
fun RoundedRectFilledTonalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    imeAnimation: State<Dp>? = null,
    loading: Boolean = false,
    content: @Composable RowScope.() -> Unit,
) {
    val disabledContainerColor by animateColorAsState(
        targetValue = if (loading) {
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.48f)
                .compositeOver(MaterialTheme.colorScheme.onSecondaryContainer)
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        },
        label = "RoundedRectFilledTonalButton.DisabledContainerColor",
    )

    val imeDp = imeAnimation?.value ?: 1.dp
    val imePaddingDp = imeAnimation?.value ?: 0.dp
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier
            .padding(16 * imePaddingDp)
            .height(48.dp),
        enabled = enabled && !loading,
        shape = RoundedCornerShape(12 * imeDp),
        colors = ButtonDefaults.filledTonalButtonColors(
            disabledContainerColor = disabledContainerColor,
        ),
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = if (loading) {
            {
                CircularProgressIndicatorInButton(
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        } else {
            content
        },
    )
}
