package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag

enum class NavigationTab(val title: String, val icon: ImageVector, val tag: String) {
    HOME("الرئيسية", Icons.Default.Home, "tab_home"),
    QUIZ("الاختبارات", Icons.Default.Quiz, "tab_quiz"),
    ANALYZER("المُحلل", Icons.Default.Psychology, "tab_analyzer"),
    MIND_MAP("الخرائط", Icons.Default.AccountTree, "tab_mindmap"),
    DEBATE("المناظرة", Icons.Default.Gavel, "tab_debate"),
    RESEARCH("البحوث", Icons.Default.LibraryBooks, "tab_research"),
    PLANNER("التخطيط", Icons.Default.Timer, "tab_planner"),
    ANALYTICS("الأداء", Icons.Default.BarChart, "tab_analytics")
}

@Composable
fun AppBottomNavigation(
    selectedTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.testTag("app_bottom_nav"),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = MaterialTheme.colorScheme.surfaceTint.run { 8.run { androidx.compose.ui.unit.Dp(8f) } }
    ) {
        NavigationTab.entries.forEach { tab ->
            val isSelected = selectedTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title
                    )
                },
                label = {
                    Text(
                        text = tab.title,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.testTag(tab.tag)
            )
        }
    }
}
