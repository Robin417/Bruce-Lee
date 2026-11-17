package com.example

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.test.core.app.ApplicationProvider
import com.example.data.DataStoreDailyCheckRepository
import com.example.data.MedicineSlot
import com.example.ui.DailyCheckScreen
import com.example.ui.DailyCheckUiState
import com.example.ui.DailyCheckViewModel
import com.example.ui.theme.SeniorCareTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule val tempFolder = TemporaryFolder()

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("오늘 약과 물 체크", appName)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `repository persists today medicine and water across restart and resets on new day`() =
    runTest {
      val testDispatcher = UnconfinedTestDispatcher(testScheduler)
      val scope = TestScope(testDispatcher)
      val storeFile = File(tempFolder.newFolder(), "test_daily_check.preferences_pb")
      val dataStore = PreferenceDataStoreFactory.create(scope = scope) { storeFile }

      var currentDay = "2026-11-16"
      val repoFirstSession =
        DataStoreDailyCheckRepository(dataStore = dataStore, dateKeyProvider = { currentDay })

      repoFirstSession.toggleMedicine(MedicineSlot.MORNING)
      repoFirstSession.toggleMedicine(MedicineSlot.LUNCH)
      repoFirstSession.toggleMedicine(MedicineSlot.DINNER)
      repoFirstSession.incrementWater()
      repoFirstSession.incrementWater()

      // Simulate app restart with a brand new repository instance reading the same DataStore file
      val repoAfterRestart =
        DataStoreDailyCheckRepository(dataStore = dataStore, dateKeyProvider = { currentDay })
      val restored = repoAfterRestart.todayRecord.first()
      assertTrue(restored.morningTaken)
      assertTrue(restored.lunchTaken)
      assertTrue(restored.dinnerTaken)
      assertEquals(2, restored.waterCount)

      // Simulate midnight rollover to next day and verify DataStore persists the reset state
      currentDay = "2026-11-17"
      repoAfterRestart.refreshDateIfNeeded()
      val nextDayRecord = repoAfterRestart.todayRecord.first()
      assertEquals("2026-11-17", nextDayRecord.dateKey)
      assertFalse(nextDayRecord.morningTaken)
      assertFalse(nextDayRecord.lunchTaken)
      assertFalse(nextDayRecord.dinnerTaken)
      assertEquals(0, nextDayRecord.waterCount)
    }

  @Test
  fun `millisUntilNextMidnight calculates exact delay until local midnight`() {
    val tz = TimeZone.getTimeZone("Asia/Seoul")
    val cal =
      Calendar.getInstance(tz).apply {
        set(2026, Calendar.NOVEMBER, 16, 23, 59, 50)
        set(Calendar.MILLISECOND, 0)
      }
    val waitMillis = DailyCheckViewModel.millisUntilNextMidnight(cal.timeInMillis, tz)
    assertEquals(10_050L, waitMillis)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `viewModel automatically resets medicine and water when midnight passes while app is open`() =
    runTest {
      val testDispatcher = StandardTestDispatcher(testScheduler)
      Dispatchers.setMain(testDispatcher)
      try {
        val tz = TimeZone.getTimeZone("Asia/Seoul")
        val initialCal =
          Calendar.getInstance(tz).apply {
            set(2026, Calendar.NOVEMBER, 16, 23, 59, 50)
            set(Calendar.MILLISECOND, 0)
          }
        val baseWallMillis = initialCal.timeInMillis
        val nowProvider: () -> Long = { baseWallMillis + testScheduler.currentTime }
        val dateFormat =
          SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = tz }
        val dateKeyProvider: () -> String = { dateFormat.format(Date(nowProvider())) }

        val storeFile = File(tempFolder.newFolder(), "midnight_ui.preferences_pb")
        val dataStore =
          PreferenceDataStoreFactory.create(scope = TestScope(testDispatcher)) { storeFile }
        val repository =
          DataStoreDailyCheckRepository(dataStore = dataStore, dateKeyProvider = dateKeyProvider)

        val viewModel =
          DailyCheckViewModel(
            repository = repository,
            currentTimeMillis = nowProvider,
            timeZoneProvider = { tz },
          )

        val collectorJob = launch { viewModel.uiState.collect {} }
        runCurrent()

        viewModel.toggleMedicine(MedicineSlot.MORNING)
        viewModel.incrementWater()
        viewModel.incrementWater()
        runCurrent()

        val beforeMidnight = viewModel.uiState.value as DailyCheckUiState.Ready
        assertEquals("2026-11-16", beforeMidnight.record.dateKey)
        assertTrue(beforeMidnight.record.morningTaken)
        assertEquals(2, beforeMidnight.record.waterCount)

        // Advance virtual clock across midnight (10_050 ms -> 2026-11-17 00:00:00.050)
        advanceTimeBy(10_100L)
        runCurrent()

        val afterMidnight = viewModel.uiState.value as DailyCheckUiState.Ready
        assertEquals("2026-11-17", afterMidnight.record.dateKey)
        assertFalse(afterMidnight.record.morningTaken)
        assertFalse(afterMidnight.record.lunchTaken)
        assertFalse(afterMidnight.record.dinnerTaken)
        assertEquals(0, afterMidnight.record.waterCount)

        collectorJob.cancel()
      } finally {
        Dispatchers.resetMain()
      }
    }

  @Test
  fun `daily check screen toggles medicine and increments water`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val storeFile = File(tempFolder.newFolder(), "ui_test.preferences_pb")
    val dataStoreScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    val dataStore = PreferenceDataStoreFactory.create(scope = dataStoreScope) { storeFile }
    val repository =
      DataStoreDailyCheckRepository(dataStore = dataStore, dateKeyProvider = { "2026-11-16" })
    val viewModel = DailyCheckViewModel(repository)

    composeTestRule.setContent {
      SeniorCareTheme {
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        DailyCheckScreen(
          uiState = state,
          onToggleMedicine = viewModel::toggleMedicine,
          onIncrementWater = viewModel::incrementWater,
          onDecrementWater = viewModel::decrementWater,
        )
      }
    }

    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule
        .onAllNodes(androidx.compose.ui.test.hasTestTag("date_header_card"))
        .fetchSemanticsNodes()
        .isNotEmpty()
    }
    composeTestRule.waitForIdle()

    // Verify date and 3 medicine buttons exist
    composeTestRule.onNodeWithTag("date_header_card").assertIsDisplayed()
    composeTestRule.onNodeWithText("2026년 11월 16일 (월요일)").assertIsDisplayed()
    composeTestRule.onNodeWithTag("morning_medicine_button").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("lunch_medicine_button").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("dinner_medicine_button").performScrollTo().assertIsDisplayed()

    // Tap Morning Medicine -> shows '복용 완료'
    composeTestRule.onNodeWithTag("morning_medicine_button").performScrollTo().performClick()
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule
        .onAllNodes(
          androidx.compose.ui.test.hasText(context.getString(R.string.medicine_taken_status))
        )
        .fetchSemanticsNodes()
        .isNotEmpty()
    }
    composeTestRule
      .onNodeWithText(context.getString(R.string.medicine_taken_status))
      .performScrollTo()
      .assertIsDisplayed()

    // Tap Lunch and Dinner Medicine -> shows '오늘도 건강 챙기기 성공!'
    composeTestRule.onNodeWithTag("lunch_medicine_button").performScrollTo().performClick()
    composeTestRule.onNodeWithTag("dinner_medicine_button").performScrollTo().performClick()
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule
        .onAllNodes(androidx.compose.ui.test.hasTestTag("all_medicines_done_banner"))
        .fetchSemanticsNodes()
        .isNotEmpty()
    }
    composeTestRule
      .onNodeWithText("오늘도 건강 챙기기 성공!")
      .performScrollTo()
      .assertIsDisplayed()

    // Scroll to water button and tap twice -> shows 2잔
    composeTestRule.onNodeWithTag("add_water_button").performScrollTo().performClick()
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule
        .onAllNodes(androidx.compose.ui.test.hasText("1잔"))
        .fetchSemanticsNodes()
        .isNotEmpty()
    }
    composeTestRule.onNodeWithTag("add_water_button").performScrollTo().performClick()
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule
        .onAllNodes(androidx.compose.ui.test.hasText("2잔"))
        .fetchSemanticsNodes()
        .isNotEmpty()
    }
    composeTestRule.onNodeWithTag("water_count_text").performScrollTo().assertTextContains("2잔")
  }
}
