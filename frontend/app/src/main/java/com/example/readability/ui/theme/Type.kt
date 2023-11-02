package com.example.readability.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.readability.R

@OptIn(ExperimentalTextApi::class)
val Gabarito = FontFamily(
    Font(
        R.font.gabarito
    )
)

@OptIn(ExperimentalTextApi::class)
val Lora = FontFamily(
    Font(
        R.font.lora
    )
)

// Set of Material typography styles to start with
private val defaultTypoGraphy = Typography()
val Typography = Typography(
    displayLarge = defaultTypoGraphy.displayLarge.copy(fontFamily = Lora, fontWeight = FontWeight.SemiBold),
    displayMedium = defaultTypoGraphy.displayMedium.copy(fontFamily = Lora, fontWeight = FontWeight.SemiBold),
    displaySmall = defaultTypoGraphy.displaySmall.copy(fontFamily = Lora, fontWeight = FontWeight.SemiBold),
    headlineLarge = defaultTypoGraphy.headlineLarge.copy(fontFamily = Lora, fontWeight = FontWeight.SemiBold),
    headlineMedium = defaultTypoGraphy.headlineMedium.copy(fontFamily = Lora, fontWeight = FontWeight.SemiBold),
    headlineSmall = defaultTypoGraphy.headlineSmall.copy(fontFamily = Lora, fontWeight = FontWeight.SemiBold),
    titleLarge = defaultTypoGraphy.titleLarge.copy(fontFamily = Lora, fontWeight = FontWeight.SemiBold),
    titleMedium = defaultTypoGraphy.titleMedium.copy(fontFamily = Lora, fontWeight = FontWeight.SemiBold),
    titleSmall = defaultTypoGraphy.titleSmall.copy(fontFamily = Lora, fontWeight = FontWeight.SemiBold),
    bodyLarge = defaultTypoGraphy.bodyLarge.copy(fontFamily = Gabarito),
    bodyMedium = defaultTypoGraphy.bodyMedium.copy(fontFamily = Gabarito),
    bodySmall = defaultTypoGraphy.bodySmall.copy(fontFamily = Gabarito),
    labelLarge = defaultTypoGraphy.labelLarge.copy(fontFamily = Gabarito),
    labelMedium = defaultTypoGraphy.labelMedium.copy(fontFamily = Gabarito),
    labelSmall = defaultTypoGraphy.labelSmall.copy(fontFamily = Gabarito),
)