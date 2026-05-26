package com.kabindra.mobile.iptv.presentation.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.kabindra.mobile.iptv.R

@Composable
fun AppTypography() = Typography().run {
    val fontFamily = ManRopeFontFamily()

    copy(
        displayLarge = displayLarge.copy(
            fontSize = 57.sp,
            lineHeight = 64.sp,
            fontFamily = fontFamily
        ),
        displayMedium = displayMedium.copy(
            fontSize = 45.sp,
            lineHeight = 52.sp,
            fontFamily = fontFamily
        ),
        displaySmall = displaySmall.copy(
            fontSize = 36.sp,
            lineHeight = 44.sp,
            fontFamily = fontFamily
        ),
        headlineLarge = headlineLarge.copy(
            fontSize = 32.sp,
            lineHeight = 40.sp,
            fontFamily = fontFamily
        ),
        headlineMedium = headlineMedium.copy(
            fontSize = 28.sp,
            lineHeight = 36.sp,
            fontFamily = fontFamily
        ),
        headlineSmall = headlineSmall.copy(
            fontSize = 24.sp,
            lineHeight = 32.sp,
            fontFamily = fontFamily
        ),
        titleLarge = titleLarge.copy(fontSize = 22.sp, lineHeight = 28.sp, fontFamily = fontFamily),
        titleMedium = titleMedium.copy(
            fontSize = 16.sp,
            lineHeight = 24.sp,
            fontFamily = fontFamily
        ),
        titleSmall = titleSmall.copy(fontSize = 14.sp, lineHeight = 20.sp, fontFamily = fontFamily),
        bodyLarge = bodyLarge.copy(fontSize = 16.sp, lineHeight = 24.sp, fontFamily = fontFamily),
        bodyMedium = bodyMedium.copy(fontSize = 14.sp, lineHeight = 20.sp, fontFamily = fontFamily),
        bodySmall = bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp, fontFamily = fontFamily),
        labelLarge = labelLarge.copy(fontSize = 14.sp, lineHeight = 20.sp, fontFamily = fontFamily),
        labelMedium = labelMedium.copy(
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontFamily = fontFamily
        ),
        labelSmall = labelSmall.copy(fontSize = 11.sp, lineHeight = 16.sp, fontFamily = fontFamily),
    )
}

@Composable
fun ManRopeFontFamily() = FontFamily(
    Font(R.font.manrope_light, weight = FontWeight.Light),
    Font(R.font.manrope_regular, weight = FontWeight.Normal),
    Font(R.font.manrope_medium, weight = FontWeight.Medium),
    Font(R.font.manrope_semi_bold, weight = FontWeight.SemiBold),
    Font(R.font.manrope_bold, weight = FontWeight.Bold),
    Font(R.font.manrope_extra_bold, weight = FontWeight.ExtraBold),
    Font(R.font.manrope_extra_light, weight = FontWeight.ExtraLight)
)
