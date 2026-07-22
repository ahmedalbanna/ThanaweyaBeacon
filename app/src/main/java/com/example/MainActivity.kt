package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AppBottomNavigation
import com.example.ui.components.NavigationTab
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.AnalyzerScreen
import com.example.ui.screens.DebateScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MindMapScreen
import com.example.ui.screens.PlannerScreen
import com.example.ui.screens.QuizScreen
import com.example.ui.screens.ResearchScreen
import com.example.ui.theme.ThanaweyaBeaconTheme
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ThanaweyaBeaconTheme {
                ThanaweyaAppContent()
            }
        }
    }
}

@Composable
fun ThanaweyaAppContent(
    viewModel: MainViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(NavigationTab.HOME) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            AppBottomNavigation(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                NavigationTab.HOME -> HomeScreen(
                    viewModel = viewModel,
                    onNavigateToTab = { selectedTab = it }
                )
                NavigationTab.QUIZ -> QuizScreen(viewModel = viewModel)
                NavigationTab.ANALYZER -> AnalyzerScreen(viewModel = viewModel)
                NavigationTab.MIND_MAP -> MindMapScreen(viewModel = viewModel)
                NavigationTab.DEBATE -> DebateScreen(viewModel = viewModel)
                NavigationTab.RESEARCH -> ResearchScreen(viewModel = viewModel)
                NavigationTab.PLANNER -> PlannerScreen(viewModel = viewModel)
                NavigationTab.ANALYTICS -> AnalyticsScreen(viewModel = viewModel)
            }
        }
    }
}

