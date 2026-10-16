package com.mrkrob.news.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.mrkrob.news.R

private fun montserrat(weight: FontWeight) = Font(
    resId = R.font.montserrat,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

private val Montserrat = FontFamily(
    montserrat(FontWeight.Normal),
    montserrat(FontWeight.Medium),
    montserrat(FontWeight.SemiBold),
    montserrat(FontWeight.Bold),
)

private fun TextStyle.montserrat(weight: FontWeight) =
    copy(fontFamily = Montserrat, fontWeight = weight, letterSpacing = 0.sp)

internal val NewsTypography = Typography().run {
    Typography(
        displayLarge = displayLarge.montserrat(FontWeight.Bold),
        displayMedium = displayMedium.montserrat(FontWeight.Bold),
        displaySmall = displaySmall.montserrat(FontWeight.Bold),
        headlineLarge = headlineLarge.montserrat(FontWeight.Bold),
        headlineMedium = headlineMedium.montserrat(FontWeight.Bold),
        headlineSmall = headlineSmall.montserrat(FontWeight.Bold),
        titleLarge = titleLarge.montserrat(FontWeight.Bold),
        titleMedium = titleMedium.montserrat(FontWeight.SemiBold),
        titleSmall = titleSmall.montserrat(FontWeight.SemiBold),
        bodyLarge = bodyLarge.montserrat(FontWeight.Normal),
        bodyMedium = bodyMedium.montserrat(FontWeight.Normal),
        bodySmall = bodySmall.montserrat(FontWeight.Normal),
        labelLarge = labelLarge.montserrat(FontWeight.SemiBold),
        labelMedium = labelMedium.montserrat(FontWeight.SemiBold),
        labelSmall = labelSmall.montserrat(FontWeight.SemiBold),
    )
}
