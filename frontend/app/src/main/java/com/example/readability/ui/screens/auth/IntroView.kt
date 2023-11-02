package com.example.readability.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.readability.R
import com.example.readability.ui.theme.Gabarito
import com.example.readability.ui.theme.ReadabilityTheme

@Composable
@Preview(showBackground = true, device = "id:pixel_5")
fun IntroPreview() {
    ReadabilityTheme {
        IntroView()
    }
}


@Composable
fun IntroView(
    onContinueWithEmailClicked: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .imePadding()
            .systemBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier.fillMaxHeight()
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                "Readability",
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.headlineLarge.copy(color = MaterialTheme.colorScheme.onPrimaryContainer),
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.weight(1f))
            InformText()
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onContinueWithEmailClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.email), contentDescription = "email"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Continue with email")
            }

        }
    }

}

@Composable
fun InformText(
    onPrivacyPolicyClicked: () -> Unit = {}, onTermsOfUseClicked: () -> Unit = {}
) {
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
        append("By Continuing I agree with\nthe ")
        pushStringAnnotation("privacy", "privacy")
        withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
            append("Privacy Policy")
        }
        pop()
        append(", ")
        pushStringAnnotation("terms", "terms")
        withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
            append("Term of Use")
        }
        pop()
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


