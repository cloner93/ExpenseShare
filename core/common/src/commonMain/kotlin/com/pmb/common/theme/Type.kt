package com.pmb.common.theme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import expenseshare.core.common.generated.resources.ADLaMDisplay_Regular
import expenseshare.core.common.generated.resources.Res
import org.jetbrains.compose.resources.Font

@Composable
fun appTypography(): Typography {
    val bodyFont = FontFamily(
        Font(Res.font.ADLaMDisplay_Regular, weight = FontWeight.Normal)
    )

    val displayFont = FontFamily(
        Font(Res.font.ADLaMDisplay_Regular, weight = FontWeight.Bold)
    )

    val base = Typography()

    return Typography(
        displayLarge = base.displayLarge.copy(fontFamily = displayFont),
        displayMedium = base.displayMedium.copy(fontFamily = displayFont),
        displaySmall = base.displaySmall.copy(fontFamily = displayFont),
        headlineLarge = base.headlineLarge.copy(fontFamily = displayFont),
        headlineMedium = base.headlineMedium.copy(fontFamily = displayFont),
        headlineSmall = base.headlineSmall.copy(fontFamily = displayFont),
        titleLarge = base.titleLarge.copy(fontFamily = displayFont),
        titleMedium = base.titleMedium.copy(fontFamily = displayFont),
        titleSmall = base.titleSmall.copy(fontFamily = displayFont),
        bodyLarge = base.bodyLarge.copy(fontFamily = bodyFont),
        bodyMedium = base.bodyMedium.copy(fontFamily = bodyFont),
        bodySmall = base.bodySmall.copy(fontFamily = bodyFont),
        labelLarge = base.labelLarge.copy(fontFamily = bodyFont),
        labelMedium = base.labelMedium.copy(fontFamily = bodyFont),
        labelSmall = base.labelSmall.copy(fontFamily = bodyFont),
    )
}
