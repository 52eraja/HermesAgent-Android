package com.hermes.agent.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hermes.agent.ui.screens.*
import com.hermes.agent.viewmodel.HermesViewModel

/**
 * Bottom navigation destinations.
 */
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Chat : BottomNavItem("chat", "Chat", Icons.Default.Chat)
    data object Conversations : BottomNavItem("conversations", "History", Icons.Default.History)
    data object Settings : BottomNavItem("settings", "Settings", Icons.Default.Settings)
}

private val bottomNavItems = listOf(
    BottomNavItem.Chat,
    BottomNavItem.Conversations,
    BottomNavItem.Settings
)

/**
 * Main navigation graph with bottom navigation bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HermesNavGraph(
    viewModel: HermesViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val connectionState by viewModel.connectionState.collectAsState()
    val chatState by viewModel.chatState.collectAsState()
    val conversations by viewModel.conversations.collectAsState()
    val settings by viewModel.settings.collectAsState()

    // Track current route for bottom nav highlighting
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any {
                        it.route == item.route
                    } == true

                    NavigationBarItem(
                        icon = {
                            Icon(item.icon, contentDescription = item.title)
                        },
                        label = { Text(item.title) },
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (connectionState.isConnected) "chat" else "login",
            modifier = Modifier.padding(innerPadding)
        ) {
            // Login screen
            composable("login") {
                LoginScreen(
                    connectionState = connectionState,
                    settings = settings,
                    onServerUrlChange = { viewModel.updateSettings(serverUrl = it) },
                    onUsernameChange = { viewModel.updateSettings(username = it) },
                    onPasswordChange = { viewModel.updateSettings(password = it) },
                    onConnect = {
                        viewModel.saveSettings()
                        viewModel.connect()
                    }
                )

                // Navigate to chat when connection is established
                LaunchedEffect(connectionState.isConnected) {
                    if (connectionState.isConnected) {
                        navController.navigate("chat") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                }
            }

            // Chat screen
            composable("chat") {
                ChatScreen(
                    chatState = chatState,
                    connectionState = connectionState,
                    onInputChange = { viewModel.updateInputText(it) },
                    onSend = { viewModel.sendMessage() },
                    onNewConversation = { viewModel.newConversation() },
                    onRefresh = { viewModel.refreshConversations() }
                )
            }

            // Conversations screen
            composable("conversations") {
                ConversationsScreen(
                    conversations = conversations,
                    activeConversationId = chatState.currentConversationId,
                    connectionState = connectionState,
                    onConversationClick = { conversation ->
                        viewModel.loadConversation(conversation)
                        navController.navigate("chat")
                    },
                    onDeleteConversation = { id ->
                        viewModel.deleteConversation(id)
                    },
                    onRefresh = { viewModel.refreshConversations() }
                )
            }

            // Settings screen
            composable("settings") {
                SettingsScreen(
                    settings = settings,
                    connectionState = connectionState,
                    onServerUrlChange = { viewModel.updateSettings(serverUrl = it) },
                    onUsernameChange = { viewModel.updateSettings(username = it) },
                    onPasswordChange = { viewModel.updateSettings(password = it) },
                    onModelChange = { viewModel.updateSettings(defaultModel = it) },
                    onDarkModeChange = { viewModel.updateSettings(darkMode = it) },
                    onSave = { viewModel.saveSettings() },
                    onDisconnect = {
                        viewModel.disconnect()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
