package com.example.anasdemoapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.anasdemoapplication.navigation.Fund
import com.example.anasdemoapplication.navigation.Invest
import com.example.anasdemoapplication.navigation.Order
import com.example.anasdemoapplication.navigation.Portfolio
import com.example.anasdemoapplication.navigation.WatchList
import com.example.anasdemoapplication.navigation.bottomNavItems
import com.example.anasdemoapplication.ui.screens.FundScreen
import com.example.anasdemoapplication.ui.screens.InvestScreen
import com.example.anasdemoapplication.ui.screens.OrderScreen
import com.example.anasdemoapplication.ui.screens.PortfolioScreen
import com.example.anasdemoapplication.ui.screens.WatchListScreen
import com.example.anasdemoapplication.ui.theme.DemoApplicationTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val backStack = rememberNavBackStack(Fund)
            val currentKey = backStack.lastOrNull()

            BackHandler(enabled = currentKey != Fund) {
                backStack.removeLastOrNull()
                backStack.add(Fund)
            }

            DemoApplicationTheme {
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            bottomNavItems.forEach { item ->
                                val isSelected = currentKey == item.key
                                NavigationBarItem(
                                    icon = {
                                        Icon(
                                            imageVector = if (isSelected) {
                                                item.selectedIcon
                                            } else {
                                                item.unselectedIcon
                                            },
                                            contentDescription = item.label
                                        )
                                    },
                                    label = { Text(item.label) },
                                    selected = isSelected,
                                    onClick = {
                                        if (isSelected.not()) {
                                            backStack.removeLastOrNull()
                                            backStack.add(item.key)
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { paddingValues ->
                    Box(modifier = Modifier.padding(paddingValues)) {
                        NavDisplay(
                            backStack = backStack,
                            entryDecorators = listOf(
                                rememberSaveableStateHolderNavEntryDecorator(),
                                rememberViewModelStoreNavEntryDecorator()
                            ),
                            onBack = { backStack.removeLastOrNull() },
                            entryProvider = { key ->
                                when (key) {
                                    is Fund -> {
                                        NavEntry(key = key) { FundScreen(backStack = backStack) }
                                    }

                                    is Order -> {
                                        NavEntry(key = key) { OrderScreen(backStack = backStack) }
                                    }

                                    is Portfolio -> {
                                        NavEntry(key = key) { PortfolioScreen(backStack = backStack) }
                                    }

                                    is Invest -> {
                                        NavEntry(key = key) { InvestScreen(backStack = backStack) }
                                    }

                                    is WatchList -> {
                                        NavEntry(key = key) { WatchListScreen(backStack = backStack) }
                                    }

                                    else -> {
                                        NavEntry(key = key) {
                                            Text("Unknown route")
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}