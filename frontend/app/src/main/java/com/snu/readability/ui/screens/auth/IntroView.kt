package com.snu.readability.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snu.readability.R
import com.snu.readability.ui.animation.DURATION_EMPHASIZED
import com.snu.readability.ui.animation.EASING_EMPHASIZED
import com.snu.readability.ui.theme.Gabarito
import com.snu.readability.ui.theme.ReadabilityTheme
import kotlinx.coroutines.delay

@Composable
@Preview(showBackground = true, device = "id:pixel_5")
fun IntroPreview() {
    ReadabilityTheme {
        IntroView()
    }
}

@Composable
fun IntroView(
    onContinueWithEmailClicked: () -> Unit = {},
    onPrivacyPolicyClicked: () -> Unit = {},
    onTermsOfUseClicked: () -> Unit = {},
) {
    var controlsVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(500L)
        controlsVisible = true
    }

    val navPadding = with(LocalDensity.current) {
        WindowInsets.navigationBars.getBottom(this).toDp()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(vertical = navPadding),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom,
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Image(
                modifier = Modifier.size(192.dp),
                painter = painterResource(id = R.drawable.ic_launcher_foreground_x1_5),
                contentDescription = "App Logo",
            )
            AnimatedVisibility(
                visible = controlsVisible,
                enter = fadeIn(
                    tween(DURATION_EMPHASIZED, 0, EASING_EMPHASIZED),
                ) + expandVertically(
                    tween(DURATION_EMPHASIZED, 0, EASING_EMPHASIZED),
                ),
                exit = fadeOut(
                    tween(DURATION_EMPHASIZED, 0, EASING_EMPHASIZED),
                ) + shrinkVertically(
                    tween(DURATION_EMPHASIZED, 0, EASING_EMPHASIZED),
                ),
            ) {
                Text(
                    "Readability",
                    style = MaterialTheme.typography.displayMedium.copy(
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Normal,
                        fontFamily = Gabarito,
                    ),
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            AnimatedVisibility(
                visible = controlsVisible,
                enter = fadeIn(
                    tween(DURATION_EMPHASIZED, 0, EASING_EMPHASIZED),
                ) + expandVertically(
                    tween(DURATION_EMPHASIZED, 0, EASING_EMPHASIZED),
                ),
                exit = fadeOut(
                    tween(DURATION_EMPHASIZED, 0, EASING_EMPHASIZED),
                ) + shrinkVertically(
                    tween(DURATION_EMPHASIZED, 0, EASING_EMPHASIZED),
                ),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    InformText(
                        onPrivacyPolicyClicked = onPrivacyPolicyClicked,
                        onTermsOfUseClicked = onTermsOfUseClicked,
                    )
                    Button(
                        onClick = onContinueWithEmailClicked,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(48.dp)
                            .testTag("ContinueWithEmailButton"),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.email),
                            contentDescription = "email",
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Continue with email")
                    }
                }
            }
        }
    }
}

@Composable
fun InformText(onPrivacyPolicyClicked: () -> Unit = {}, onTermsOfUseClicked: () -> Unit = {}) {
    val textStyle = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontFamily = Gabarito,
        fontWeight = FontWeight.Normal,
        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.4f),
        textAlign = TextAlign.Center,
        letterSpacing = 0.5.sp,
    )

    val annotatedString = buildAnnotatedString {
        append("By Continuing I agree with\nthe Privacy Policy, Term of Use")
    }

    ClickableText(text = annotatedString, onClick = {
        annotatedString.getStringAnnotations(tag = "privacy", start = it, end = it).firstOrNull()
            ?.let {
                onPrivacyPolicyClicked()
            }
        annotatedString.getStringAnnotations(tag = "terms", start = it, end = it).firstOrNull()
            ?.let {
                onTermsOfUseClicked()
            }
    }, style = textStyle)
}
