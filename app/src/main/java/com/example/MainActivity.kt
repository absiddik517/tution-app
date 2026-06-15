package com.example

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.TuitionDatabase
import com.example.data.TuitionRepository
import com.example.ui.TuitionMainScreen
import com.example.ui.TuitionViewModel
import com.example.ui.TuitionViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val context = LocalContext.current
        val database = TuitionDatabase.getDatabase(context.applicationContext)
        val repository = TuitionRepository(database.tuitionDao())
        val application = context.applicationContext as Application
        
        val viewModel: TuitionViewModel = viewModel(
          factory = TuitionViewModelFactory(application, repository)
        )

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          // MainScreen manages its internal padding for edge-to-edge layout cleanly
          TuitionMainScreen(viewModel = viewModel)
        }
      }
    }
  }
}

