package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class SeniorSemanticColors(
  val ivoryBackground: Color,
  val cardSurface: Color,
  val cardBorder: Color,
  val warmSurfaceVariant: Color,
  val inkPrimary: Color,
  val inkSecondary: Color,
  val takenGreen: Color,
  val takenGreenBorder: Color,
  val takenGreenSoft: Color,
  val takenGreenText: Color,
  val onTakenGreen: Color,
  val morningIconBg: Color,
  val morningIconTint: Color,
  val lunchIconBg: Color,
  val lunchIconTint: Color,
  val dinnerIconBg: Color,
  val dinnerIconTint: Color,
  val waterPrimary: Color,
  val onWaterPrimary: Color,
  val waterCardBg: Color,
  val waterCardBorder: Color,
  val waterFillLight: Color,
  val waterFillDeep: Color,
  val waterGlassOutline: Color,
  val waterGlassInterior: Color,
  val undoButtonBg: Color,
  val undoButtonText: Color,
)

val DefaultSeniorColors =
  SeniorSemanticColors(
    ivoryBackground = IvoryBackground,
    cardSurface = WarmCreamSurface,
    cardBorder = WarmBorder,
    warmSurfaceVariant = WarmIvoryVariant,
    inkPrimary = InkPrimary,
    inkSecondary = InkSecondary,
    takenGreen = TakenGreen,
    takenGreenBorder = TakenGreenBorder,
    takenGreenSoft = TakenGreenSoft,
    takenGreenText = TakenGreenText,
    onTakenGreen = OnTakenGreen,
    morningIconBg = MorningIconBg,
    morningIconTint = MorningIconTint,
    lunchIconBg = LunchIconBg,
    lunchIconTint = LunchIconTint,
    dinnerIconBg = DinnerIconBg,
    dinnerIconTint = DinnerIconTint,
    waterPrimary = WaterPrimary,
    onWaterPrimary = OnWaterPrimary,
    waterCardBg = WaterCardBackground,
    waterCardBorder = WaterCardBorder,
    waterFillLight = WaterFillLight,
    waterFillDeep = WaterFillDeep,
    waterGlassOutline = WaterGlassOutline,
    waterGlassInterior = WaterGlassInterior,
    undoButtonBg = UndoButtonBg,
    undoButtonText = UndoButtonText,
  )

val LocalSeniorColors = staticCompositionLocalOf { DefaultSeniorColors }

private val IvoryLightColorScheme =
  lightColorScheme(
    primary = WaterPrimary,
    onPrimary = OnWaterPrimary,
    primaryContainer = WaterCardBackground,
    onPrimaryContainer = InkPrimary,
    secondary = TakenGreen,
    onSecondary = OnTakenGreen,
    secondaryContainer = TakenGreenSoft,
    onSecondaryContainer = TakenGreenText,
    background = IvoryBackground,
    onBackground = InkPrimary,
    surface = WarmCreamSurface,
    onSurface = InkPrimary,
    surfaceVariant = WarmIvoryVariant,
    onSurfaceVariant = InkSecondary,
    outline = WarmBorder,
  )

private val SeniorShapes =
  Shapes(
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
  )

object SeniorTheme {
  val colors: SeniorSemanticColors
    @Composable get() = LocalSeniorColors.current
}

@Composable
fun SeniorCareTheme(content: @Composable () -> Unit) {
  CompositionLocalProvider(LocalSeniorColors provides DefaultSeniorColors) {
    MaterialTheme(
      colorScheme = IvoryLightColorScheme,
      typography = SeniorTypography,
      shapes = SeniorShapes,
      content = content,
    )
  }
}
