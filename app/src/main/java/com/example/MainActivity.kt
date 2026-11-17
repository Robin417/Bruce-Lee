package com.example

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DataStoreDailyCheckRepository
import com.example.data.dailyCheckDataStore
import com.example.ui.DailyCheckScreen
import com.example.ui.DailyCheckViewModel
import com.example.ui.theme.IvoryBackground
import com.example.ui.theme.SeniorCareTheme

class MainActivity : ComponentActivity() {

  private val repository by lazy {
    DataStoreDailyCheckRepository(applicationContext.dailyCheckDataStore)
  }

  private val viewModel: DailyCheckViewModel by viewModels {
    DailyCheckViewModel.provideFactory(repository)
  }

  private val dateChangeReceiver =
    object : BroadcastReceiver() {
      override fun onReceive(context: Context?, intent: Intent?) {
        viewModel.refreshDateIfNeeded()
      }
    }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val ivoryArgb = IvoryBackground.toArgb()
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.light(ivoryArgb, ivoryArgb),
      navigationBarStyle = SystemBarStyle.light(ivoryArgb, ivoryArgb),
    )
    setContent {
      SeniorCareTheme {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        Scaffold(
          modifier = Modifier.fillMaxSize(),
          containerColor = MaterialTheme.colorScheme.background,
        ) { innerPadding ->
          DailyCheckScreen(
            uiState = uiState,
            onToggleMedicine = viewModel::toggleMedicine,
            onIncrementWater = viewModel::incrementWater,
            onDecrementWater = viewModel::decrementWater,
            modifier = Modifier.padding(innerPadding),
          )
        }
      }
    }
  }

  override fun onStart() {
    super.onStart()
    val filter =
      IntentFilter().apply {
        addAction(Intent.ACTION_DATE_CHANGED)
        addAction(Intent.ACTION_TIME_CHANGED)
        addAction(Intent.ACTION_TIMEZONE_CHANGED)
      }
    ContextCompat.registerReceiver(
      this,
      dateChangeReceiver,
      filter,
      ContextCompat.RECEIVER_EXPORTED,
    )
  }

  override fun onResume() {
    super.onResume()
    viewModel.refreshDateIfNeeded()
  }

  override fun onStop() {
    unregisterReceiver(dateChangeReceiver)
    super.onStop()
  }
}
