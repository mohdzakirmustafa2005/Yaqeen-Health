package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.ai.LocalPlaceholderAiBriefingRepository
import com.example.data.local.YaqeenDatabase
import com.example.data.preferences.AppPreferencesRepository
import com.example.data.repository.WorkforceHealthRepository
import com.example.ui.MainViewModel
import com.example.ui.YaqeenApp
import com.example.ui.theme.YaqeenHealthTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContext = applicationContext
        val database = YaqeenDatabase.getInstance(appContext)
        val repository = WorkforceHealthRepository(database)
        val prefsRepository = AppPreferencesRepository(appContext)
        val aiBriefingRepository = LocalPlaceholderAiBriefingRepository()

        setContent {
            YaqeenHealthTheme {
                val viewModel: MainViewModel = viewModel(
                    factory = MainViewModel.factory(
                        repository = repository,
                        prefsRepo = prefsRepository,
                        aiBriefingRepo = aiBriefingRepository
                    )
                )
                YaqeenApp(viewModel = viewModel)
            }
        }
    }
}
