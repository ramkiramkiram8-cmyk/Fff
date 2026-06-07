package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.game.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CityBusNavigationHost(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun CityBusNavigationHost(
    modifier: Modifier = Modifier,
    viewModel: BusGameViewModel = viewModel()
) {
    val state by viewModel.gameState.collectAsState()

    when (state) {
        GameState.MainMenu -> {
            MainMenuView(
                viewModel = viewModel,
                onEnterTerminal = { viewModel.selectState(GameState.Terminal) },
                onEnterGarage = { viewModel.selectState(GameState.Garage) },
                onEnterSettings = { viewModel.selectState(GameState.Settings) },
                onFreeRoam = { viewModel.selectState(GameState.Driving) }
            )
        }
        GameState.Terminal -> {
            TerminalView(
                viewModel = viewModel,
                onBack = { viewModel.selectState(GameState.MainMenu) }
            )
        }
        GameState.Garage -> {
            GarageView(
                viewModel = viewModel,
                onBack = { viewModel.selectState(GameState.MainMenu) }
            )
        }
        GameState.Settings -> {
            SettingsView(
                viewModel = viewModel,
                onBack = { viewModel.selectState(GameState.MainMenu) }
            )
        }
        GameState.Driving -> {
            GamePlayView(
                viewModel = viewModel,
                onOpenSettings = { viewModel.selectState(GameState.Settings) },
                onBackToMenu = { viewModel.selectState(GameState.MainMenu) }
            )
        }
        else -> {
            MainMenuView(
                viewModel = viewModel,
                onEnterTerminal = { viewModel.selectState(GameState.Terminal) },
                onEnterGarage = { viewModel.selectState(GameState.Garage) },
                onEnterSettings = { viewModel.selectState(GameState.Settings) },
                onFreeRoam = { viewModel.selectState(GameState.Driving) }
            )
        }
    }
}

