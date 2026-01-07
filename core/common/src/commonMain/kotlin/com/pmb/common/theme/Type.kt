package com.pmb.common.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import expenseshare.core.common.generated.resources.Res
import expenseshare.core.common.generated.resources.adlam_display_regular
import org.jetbrains.compose.resources.Font

@Composable
fun appTypography(): Typography {
    val appFontFamily = FontFamily(

        Font(resource = Res.font.adlam_display_regular, weight = FontWeight.Normal),
        Font(resource = Res.font.adlam_display_regular, weight = FontWeight.Medium),
        Font(resource = Res.font.adlam_display_regular, weight = FontWeight.Bold)
    )

    return remember(appFontFamily) {
        val base = Typography()

        Typography(
            displayLarge = base.displayLarge.copy(fontFamily = appFontFamily),
            displayMedium = base.displayMedium.copy(fontFamily = appFontFamily),
            displaySmall = base.displaySmall.copy(fontFamily = appFontFamily),

            headlineLarge = base.headlineLarge.copy(fontFamily = appFontFamily),
            headlineMedium = base.headlineMedium.copy(fontFamily = appFontFamily),
            headlineSmall = base.headlineSmall.copy(fontFamily = appFontFamily),

            titleLarge = base.titleLarge.copy(fontFamily = appFontFamily),
            titleMedium = base.titleMedium.copy(fontFamily = appFontFamily),
            titleSmall = base.titleSmall.copy(fontFamily = appFontFamily),

            bodyLarge = base.bodyLarge.copy(fontFamily = appFontFamily),
            bodyMedium = base.bodyMedium.copy(fontFamily = appFontFamily),
            bodySmall = base.bodySmall.copy(fontFamily = appFontFamily),

            labelLarge = base.labelLarge.copy(fontFamily = appFontFamily),
            labelMedium = base.labelLarge.copy(fontFamily = appFontFamily),
            labelSmall = base.labelSmall.copy(fontFamily = appFontFamily),
        )
    }
}
