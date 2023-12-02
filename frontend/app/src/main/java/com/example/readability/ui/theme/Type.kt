package com.example.readability.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.example.readability.R

@OptIn(ExperimentalTextApi::class)
val Gabarito = FontFamily(
    Font(
        R.font.gabarito,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(FontWeight.Normal.weight),
        ),
    ),
)

@OptIn(ExperimentalTextApi::class)
val GabaritoMedium = FontFamily(
    Font(
        R.font.gabarito,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(FontWeight.Medium.weight),
        ),
    ),
)

@OptIn(ExperimentalTextApi::class)
val LoraSemiBold = FontFamily(
    Font(
        R.font.lora,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(FontWeight.SemiBold.weight),
        ),
    ),
)

// Set of Material typography styles to start with
private val defaultTypoGraphy = Typography()
val Typography = Typography(
    displayLarge = defaultTypoGraphy.displayLarge.copy(fontFamily = LoraSemiBold),
    displayMedium = defaultTypoGraphy.displayMedium.copy(fontFamily = LoraSemiBold),
    displaySmall = defaultTypoGraphy.displaySmall.copy(fontFamily = LoraSemiBold),
    headlineLarge = defaultTypoGraphy.headlineLarge.copy(fontFamily = LoraSemiBold),
    headlineMedium = defaultTypoGraphy.headlineMedium.copy(fontFamily = LoraSemiBold),
    headlineSmall = defaultTypoGraphy.headlineSmall.copy(fontFamily = LoraSemiBold),
    titleLarge = defaultTypoGraphy.titleLarge.copy(fontFamily = LoraSemiBold),
    titleMedium = defaultTypoGraphy.titleMedium.copy(fontFamily = LoraSemiBold),
    titleSmall = defaultTypoGraphy.titleSmall.copy(fontFamily = LoraSemiBold),
    bodyLarge = defaultTypoGraphy.bodyLarge.copy(fontFamily = Gabarito),
    bodyMedium = defaultTypoGraphy.bodyMedium.copy(fontFamily = Gabarito),
    bodySmall = defaultTypoGraphy.bodySmall.copy(fontFamily = Gabarito),
    labelLarge = defaultTypoGraphy.labelLarge.copy(fontFamily = Gabarito),
    labelMedium = defaultTypoGraphy.labelMedium.copy(fontFamily = Gabarito),
    labelSmall = defaultTypoGraphy.labelSmall.copy(fontFamily = Gabarito),
)
