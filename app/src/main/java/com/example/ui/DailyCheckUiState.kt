package com.example.ui

import com.example.data.DailyHealthRecord

sealed interface DailyCheckUiState {
  data object Loading : DailyCheckUiState

  data class Ready(
    val formattedDate: String,
    val formattedWeekday: String,
    val record: DailyHealthRecord,
  ) : DailyCheckUiState
}
