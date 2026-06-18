package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.DriverDashboardScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.DriverViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val viewModel: DriverViewModel = viewModel()
        Surface(modifier = Modifier.fillMaxSize()) {
          DriverDashboardScreen(viewModel = viewModel)
        }
      }
    }
  }
}
