package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

val Context.dailyCheckDataStore: DataStore<Preferences> by
  preferencesDataStore(name = "senior_daily_check")

interface DailyCheckRepository {
  val todayRecord: Flow<DailyHealthRecord>

  suspend fun toggleMedicine(slot: MedicineSlot)

  suspend fun incrementWater()

  suspend fun decrementWater()

  suspend fun refreshDateIfNeeded()
}

class DataStoreDailyCheckRepository(
  private val dataStore: DataStore<Preferences>,
  private val dateKeyProvider: () -> String = { currentLocalDateKey() },
) : DailyCheckRepository {

  private val activeDateKey = MutableStateFlow(dateKeyProvider())

  override val todayRecord: Flow<DailyHealthRecord> =
    combine(dataStore.data, activeDateKey) { prefs, todayKey ->
      val savedDate = prefs[KEY_DATE]
      if (savedDate == todayKey) {
        DailyHealthRecord(
          dateKey = todayKey,
          morningTaken = prefs[KEY_MORNING] ?: false,
          lunchTaken = prefs[KEY_LUNCH] ?: false,
          dinnerTaken = prefs[KEY_DINNER] ?: false,
          waterCount = (prefs[KEY_WATER_COUNT] ?: 0).coerceAtLeast(0),
        )
      } else {
        DailyHealthRecord(dateKey = todayKey)
      }
    }

  override suspend fun toggleMedicine(slot: MedicineSlot) {
    val todayKey = dateKeyProvider()
    activeDateKey.value = todayKey
    dataStore.edit { prefs ->
      resetIfDifferentDay(prefs, todayKey)
      val key =
        when (slot) {
          MedicineSlot.MORNING -> KEY_MORNING
          MedicineSlot.LUNCH -> KEY_LUNCH
          MedicineSlot.DINNER -> KEY_DINNER
        }
      val current = prefs[key] ?: false
      prefs[key] = !current
    }
  }

  override suspend fun incrementWater() {
    val todayKey = dateKeyProvider()
    activeDateKey.value = todayKey
    dataStore.edit { prefs ->
      resetIfDifferentDay(prefs, todayKey)
      val current = (prefs[KEY_WATER_COUNT] ?: 0).coerceAtLeast(0)
      prefs[KEY_WATER_COUNT] = current + 1
    }
  }

  override suspend fun decrementWater() {
    val todayKey = dateKeyProvider()
    activeDateKey.value = todayKey
    dataStore.edit { prefs ->
      resetIfDifferentDay(prefs, todayKey)
      val current = (prefs[KEY_WATER_COUNT] ?: 0).coerceAtLeast(0)
      if (current > 0) {
        prefs[KEY_WATER_COUNT] = current - 1
      }
    }
  }

  override suspend fun refreshDateIfNeeded() {
    val todayKey = dateKeyProvider()
    if (activeDateKey.value != todayKey) {
      activeDateKey.value = todayKey
    }
    dataStore.edit { prefs -> resetIfDifferentDay(prefs, todayKey) }
  }

  private fun resetIfDifferentDay(
    prefs: androidx.datastore.preferences.core.MutablePreferences,
    todayKey: String,
  ) {
    if (prefs[KEY_DATE] != todayKey) {
      prefs[KEY_DATE] = todayKey
      prefs[KEY_MORNING] = false
      prefs[KEY_LUNCH] = false
      prefs[KEY_DINNER] = false
      prefs[KEY_WATER_COUNT] = 0
    }
  }

  companion object {
    private val KEY_DATE = stringPreferencesKey("record_date")
    private val KEY_MORNING = booleanPreferencesKey("morning_taken")
    private val KEY_LUNCH = booleanPreferencesKey("lunch_taken")
    private val KEY_DINNER = booleanPreferencesKey("dinner_taken")
    private val KEY_WATER_COUNT = intPreferencesKey("water_count")

    fun currentLocalDateKey(): String {
      return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }
  }
}
