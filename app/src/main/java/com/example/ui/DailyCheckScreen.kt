package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.MedicineSlot
import com.example.ui.theme.SeniorTheme

@Composable
fun DailyCheckScreen(
  uiState: DailyCheckUiState,
  onToggleMedicine: (MedicineSlot) -> Unit,
  onIncrementWater: () -> Unit,
  onDecrementWater: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val colors = SeniorTheme.colors

  Box(
    modifier = modifier.fillMaxSize().background(colors.ivoryBackground),
    contentAlignment = Alignment.TopCenter,
  ) {
    when (uiState) {
      DailyCheckUiState.Loading -> {
        Column(
          modifier = Modifier.fillMaxSize().padding(24.dp),
          verticalArrangement = Arrangement.Center,
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          CircularProgressIndicator(
            color = colors.takenGreen,
            strokeWidth = 5.dp,
            modifier = Modifier.size(56.dp),
          )
          Spacer(modifier = Modifier.height(20.dp))
          Text(
            text = stringResource(R.string.loading_message),
            style = MaterialTheme.typography.titleMedium,
            color = colors.inkPrimary,
          )
        }
      }

      is DailyCheckUiState.Ready -> {
        val record = uiState.record
        val scrollState = rememberScrollState()

        Column(
          modifier =
            Modifier.widthIn(max = 600.dp)
              .fillMaxWidth()
              .verticalScroll(scrollState)
              .padding(horizontal = 18.dp, vertical = 14.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
          // 1. Top Date Header
          DateHeaderCard(
            formattedDate = uiState.formattedDate,
            formattedWeekday = uiState.formattedWeekday,
            takenCount = record.takenMedicineCount,
          )

          // 2. Medication Section ('아침 약', '점심 약', '저녁 약')
          Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
              text = stringResource(R.string.medicine_section_title),
              style = MaterialTheme.typography.titleLarge,
              color = colors.inkPrimary,
              modifier = Modifier.padding(horizontal = 4.dp),
            )

            MedicineCheckButton(
              title = stringResource(R.string.morning_medicine),
              isTaken = record.morningTaken,
              icon = Icons.Rounded.WbTwilight,
              iconContentDescription = stringResource(R.string.cd_morning_icon),
              untakenIconBg = colors.morningIconBg,
              untakenIconTint = colors.morningIconTint,
              onClick = { onToggleMedicine(MedicineSlot.MORNING) },
              testTag = "morning_medicine_button",
            )

            MedicineCheckButton(
              title = stringResource(R.string.lunch_medicine),
              isTaken = record.lunchTaken,
              icon = Icons.Rounded.WbSunny,
              iconContentDescription = stringResource(R.string.cd_lunch_icon),
              untakenIconBg = colors.lunchIconBg,
              untakenIconTint = colors.lunchIconTint,
              onClick = { onToggleMedicine(MedicineSlot.LUNCH) },
              testTag = "lunch_medicine_button",
            )

            MedicineCheckButton(
              title = stringResource(R.string.dinner_medicine),
              isTaken = record.dinnerTaken,
              icon = Icons.Rounded.NightsStay,
              iconContentDescription = stringResource(R.string.cd_dinner_icon),
              untakenIconBg = colors.dinnerIconBg,
              untakenIconTint = colors.dinnerIconTint,
              onClick = { onToggleMedicine(MedicineSlot.DINNER) },
              testTag = "dinner_medicine_button",
            )

            AnimatedVisibility(
              visible = record.allMedicinesTaken,
              enter =
                fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
                  expandVertically(spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessMediumLow)),
              exit = fadeOut() + shrinkVertically(),
            ) {
              Surface(
                modifier = Modifier.fillMaxWidth().testTag("all_medicines_done_banner"),
                shape = RoundedCornerShape(22.dp),
                color = colors.takenGreenSoft,
                border = BorderStroke(2.5.dp, colors.takenGreen),
                shadowElevation = 3.dp,
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                  Box(
                    modifier =
                      Modifier.size(56.dp).clip(CircleShape).background(colors.takenGreen),
                    contentAlignment = Alignment.Center,
                  ) {
                    Icon(
                      imageVector = Icons.Rounded.Celebration,
                      contentDescription = null,
                      tint = colors.onTakenGreen,
                      modifier = Modifier.size(34.dp),
                    )
                  }
                  Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                  ) {
                    Text(
                      text = stringResource(R.string.all_medicines_done),
                      style = MaterialTheme.typography.headlineMedium,
                      color = colors.takenGreenText,
                    )
                    Text(
                      text = stringResource(R.string.all_medicines_done_sub),
                      style = MaterialTheme.typography.bodyLarge,
                      color = colors.takenGreenText,
                    )
                  }
                }
              }
            }
          }

          // 3. Bottom Water Drinking Section
          WaterTrackerCard(
            waterCount = record.waterCount,
            onIncrementWater = onIncrementWater,
            onDecrementWater = onDecrementWater,
          )

          Spacer(modifier = Modifier.height(6.dp))
        }
      }
    }
  }
}

@Composable
private fun DateHeaderCard(
  formattedDate: String,
  formattedWeekday: String,
  takenCount: Int,
) {
  val colors = SeniorTheme.colors

  Surface(
    modifier = Modifier.fillMaxWidth().testTag("date_header_card"),
    shape = RoundedCornerShape(22.dp),
    color = colors.cardSurface,
    border = BorderStroke(2.dp, colors.cardBorder),
    tonalElevation = 0.dp,
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = stringResource(R.string.today_label),
          style = MaterialTheme.typography.bodyLarge,
          color = colors.inkSecondary,
          fontWeight = FontWeight.SemiBold,
        )
        Surface(
          shape = RoundedCornerShape(50),
          color = if (takenCount == 3) colors.takenGreenSoft else colors.warmSurfaceVariant,
          border =
            BorderStroke(
              1.5.dp,
              if (takenCount == 3) colors.takenGreen else colors.cardBorder,
            ),
        ) {
          Text(
            text = stringResource(R.string.medicine_progress_format, takenCount),
            style = MaterialTheme.typography.labelMedium,
            color = if (takenCount == 3) colors.takenGreenText else colors.inkPrimary,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
          )
        }
      }

      Text(
        text = "$formattedDate ($formattedWeekday)",
        style = MaterialTheme.typography.headlineLarge,
        color = colors.inkPrimary,
      )
    }
  }
}

@Composable
private fun MedicineCheckButton(
  title: String,
  isTaken: Boolean,
  icon: ImageVector,
  iconContentDescription: String,
  untakenIconBg: Color,
  untakenIconTint: Color,
  onClick: () -> Unit,
  testTag: String,
) {
  val colors = SeniorTheme.colors
  val haptic = LocalHapticFeedback.current

  val containerColor by
    animateColorAsState(
      targetValue = if (isTaken) colors.takenGreen else colors.cardSurface,
      animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
      label = "medicine_card_bg",
    )
  val borderColor by
    animateColorAsState(
      targetValue = if (isTaken) colors.takenGreenBorder else colors.cardBorder,
      animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
      label = "medicine_card_border",
    )
  val titleColor by
    animateColorAsState(
      targetValue = if (isTaken) colors.onTakenGreen else colors.inkPrimary,
      label = "medicine_title_color",
    )
  val subTextColor by
    animateColorAsState(
      targetValue =
        if (isTaken) colors.onTakenGreen.copy(alpha = 0.92f) else colors.inkSecondary,
      label = "medicine_subtext_color",
    )

  Surface(
    onClick = {
      haptic.performHapticFeedback(HapticFeedbackType.LongPress)
      onClick()
    },
    modifier =
      Modifier.fillMaxWidth()
        .heightIn(min = 88.dp)
        .testTag(testTag)
        .semantics { role = Role.Checkbox },
    shape = RoundedCornerShape(20.dp),
    color = containerColor,
    border = BorderStroke(2.5.dp, borderColor),
    shadowElevation = if (isTaken) 4.dp else 1.dp,
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      // Left time-of-day icon badge
      Box(
        modifier =
          Modifier.size(60.dp)
            .clip(CircleShape)
            .background(
              if (isTaken) colors.onTakenGreen.copy(alpha = 0.20f) else untakenIconBg
            ),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = icon,
          contentDescription = iconContentDescription,
          tint = if (isTaken) colors.onTakenGreen else untakenIconTint,
          modifier = Modifier.size(34.dp),
        )
      }

      Spacer(modifier = Modifier.width(18.dp))

      // Center Title + Status
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.headlineMedium,
          color = titleColor,
        )
        Text(
          text =
            if (isTaken) {
              stringResource(R.string.medicine_taken_status)
            } else {
              stringResource(R.string.medicine_untaken_status)
            },
          style = MaterialTheme.typography.bodyLarge,
          fontWeight = FontWeight.SemiBold,
          color = subTextColor,
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      // Right Checkmark indicator
      if (isTaken) {
        Icon(
          imageVector = Icons.Rounded.CheckCircle,
          contentDescription = stringResource(R.string.cd_check_done),
          tint = colors.onTakenGreen,
          modifier = Modifier.size(48.dp),
        )
      } else {
        Icon(
          imageVector = Icons.Outlined.Circle,
          contentDescription = stringResource(R.string.cd_check_pending),
          tint = colors.cardBorder,
          modifier = Modifier.size(48.dp),
        )
      }
    }
  }
}

@Composable
private fun WaterTrackerCard(
  waterCount: Int,
  onIncrementWater: () -> Unit,
  onDecrementWater: () -> Unit,
) {
  val colors = SeniorTheme.colors
  val haptic = LocalHapticFeedback.current

  Surface(
    modifier = Modifier.fillMaxWidth().testTag("water_section_card"),
    shape = RoundedCornerShape(24.dp),
    color = colors.waterCardBg,
    border = BorderStroke(2.dp, colors.waterCardBorder),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(22.dp),
      verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = stringResource(R.string.water_section_title),
          style = MaterialTheme.typography.titleLarge,
          color = colors.inkPrimary,
        )
        Text(
          text = stringResource(R.string.water_goal_hint),
          style = MaterialTheme.typography.bodyLarge,
          color = colors.inkSecondary,
        )
      }

      // Cup Graphic + Large Counter Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp),
      ) {
        WaterCupGraphic(waterCount = waterCount)

        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Text(
              text = stringResource(R.string.water_count_format, waterCount),
              style =
                MaterialTheme.typography.displayLarge.copy(
                  fontSize = 46.sp,
                  lineHeight = 54.sp,
                ),
              color = colors.waterPrimary,
              modifier = Modifier.testTag("water_count_text"),
            )
            Text(
              text = stringResource(R.string.water_drank_label),
              style = MaterialTheme.typography.titleMedium,
              color = colors.inkPrimary,
              modifier = Modifier.padding(bottom = 6.dp),
            )
          }

          if (waterCount > 0) {
            OutlinedButton(
              onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onDecrementWater()
              },
              modifier = Modifier.heightIn(min = 52.dp).testTag("undo_water_button"),
              shape = RoundedCornerShape(14.dp),
              colors =
                ButtonDefaults.outlinedButtonColors(
                  containerColor = colors.undoButtonBg,
                  contentColor = colors.undoButtonText,
                ),
              border = BorderStroke(1.5.dp, colors.cardBorder),
              contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            ) {
              Icon(
                imageVector = Icons.Rounded.Remove,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = stringResource(R.string.water_undo_button),
                style = MaterialTheme.typography.labelMedium,
              )
            }
          }
        }
      }

      // Big "+1 Cup" Action Button
      Button(
        onClick = {
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          onIncrementWater()
        },
        modifier = Modifier.fillMaxWidth().heightIn(min = 78.dp).testTag("add_water_button"),
        shape = RoundedCornerShape(20.dp),
        colors =
          ButtonDefaults.buttonColors(
            containerColor = colors.waterPrimary,
            contentColor = colors.onWaterPrimary,
          ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
      ) {
        Icon(
          imageVector = Icons.Rounded.WaterDrop,
          contentDescription = stringResource(R.string.cd_water_drop),
          modifier = Modifier.size(32.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
          text = stringResource(R.string.water_add_button),
          style = MaterialTheme.typography.labelLarge,
        )
      }
    }
  }
}
