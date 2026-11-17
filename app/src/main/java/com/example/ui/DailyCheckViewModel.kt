package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.DailyCheckRepository
import com.example.data.MedicineSlot
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class DailyCheckViewModel(
  private val repository: DailyCheckRepository,
  private val currentTimeMillis: () -> Long = { System.currentTimeMillis() },
  private val timeZoneProvider: () -> TimeZone = { TimeZone.getDefault() },
) : ViewModel() {

  val uiState: StateFlow<DailyCheckUiState> =
    repository.todayRecord
      .map { record ->
        val (dateLabel, weekdayLabel) = formatKoreanDateLabels(record.dateKey)
        DailyCheckUiState.Ready(
          formattedDate = dateLabel,
          formattedWeekday = weekdayLabel,
          record = record,
        )
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DailyCheckUiState.Loading,
      )

  init {
    startMidnightWatcher()
  }

  private fun startMidnightWatcher() {
    viewModelScope.launch {
      repository.refreshDateIfNeeded()
      while (isActive) {
        val waitMillis = millisUntilNextMidnight(currentTimeMillis(), timeZoneProvider())
        delay(waitMillis)
        repository.refreshDateIfNeeded()
      }
    }
  }

  fun toggleMedicine(slot: MedicineSlot) {
    viewModelScope.launch { repository.toggleMedicine(slot) }
  }

  fun incrementWater() {
    viewModelScope.launch { repository.incrementWater() }
  }

  fun decrementWater() {
    viewModelScope.launch { repository.decrementWater() }
  }

  fun refreshDateIfNeeded() {
    viewModelScope.launch { repository.refreshDateIfNeeded() }
  }

  companion object {
    fun provideFactory(repository: DailyCheckRepository): ViewModelProvider.Factory =
      object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
          return DailyCheckViewModel(repository) as T
        }
      }

    internal fun millisUntilNextMidnight(
      nowMillis: Long,
      timeZone: TimeZone = TimeZone.getDefault(),
    ): Long {
      val nextMidnight =
        Calendar.getInstance(timeZone).apply {
          timeInMillis = nowMillis
          add(Calendar.DAY_OF_YEAR, 1)
          set(Calendar.HOUR_OF_DAY, 0)
          set(Calendar.MINUTE, 0)
          set(Calendar.SECOND, 0)
          set(Calendar.MILLISECOND, 50)
        }
      return (nextMidnight.timeInMillis - nowMillis).coerceAtLeast(1_000L)
    }

    internal fun formatKoreanDateLabels(dateKey: String): Pair<String, String> {
      val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
      val parsedDate = runCatching { parser.parse(dateKey) }.getOrNull() ?: Date()
      val dateFormatter = SimpleDateFormat("yyyy년 M월 d일", Locale.KOREAN)
      val weekdayFormatter = SimpleDateFormat("EEEE", Locale.KOREAN)
      return dateFormatter.format(parsedDate) to weekdayFormatter.format(parsedDate)
    }
  }
}
