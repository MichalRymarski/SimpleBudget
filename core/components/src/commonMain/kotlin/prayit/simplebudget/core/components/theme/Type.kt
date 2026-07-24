package prayit.simplebudget.core.components.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import simplebudget.core.resources.generated.resources.CormorantGaramond_Bold
import simplebudget.core.resources.generated.resources.CormorantGaramond_Regular
import simplebudget.core.resources.generated.resources.Res
import simplebudget.core.resources.generated.resources.RobotoFlex
import org.jetbrains.compose.resources.Font

@Composable
fun appTypography(): Typography {
    val bodyFontFamily = FontFamily(
        Font(Res.font.RobotoFlex, weight = FontWeight.Normal),
    )

    val displayFontFamily = FontFamily(
        Font(Res.font.CormorantGaramond_Regular, weight = FontWeight.Normal),
        Font(Res.font.CormorantGaramond_Bold, weight = FontWeight.Bold),
    )

    val baseline = Typography()

    return Typography(
        displayLarge = baseline.displayLarge.copy(fontFamily = displayFontFamily),
        displayMedium = baseline.displayMedium.copy(fontFamily = displayFontFamily),
        displaySmall = baseline.displaySmall.copy(fontFamily = displayFontFamily),
        headlineLarge = baseline.headlineLarge.copy(fontFamily = displayFontFamily),
        headlineMedium = baseline.headlineMedium.copy(fontFamily = displayFontFamily),
        headlineSmall = baseline.headlineSmall.copy(fontFamily = displayFontFamily),
        titleLarge = baseline.titleLarge.copy(fontFamily = displayFontFamily),
        titleMedium = baseline.titleMedium.copy(fontFamily = displayFontFamily),
        titleSmall = baseline.titleSmall.copy(fontFamily = displayFontFamily),
        bodyLarge = baseline.bodyLarge.copy(fontFamily = bodyFontFamily),
        bodyMedium = baseline.bodyMedium.copy(fontFamily = bodyFontFamily),
        bodySmall = baseline.bodySmall.copy(fontFamily = bodyFontFamily),
        labelLarge = baseline.labelLarge.copy(fontFamily = bodyFontFamily),
        labelMedium = baseline.labelMedium.copy(fontFamily = bodyFontFamily),
        labelSmall = baseline.labelSmall.copy(fontFamily = bodyFontFamily),
    )
}
