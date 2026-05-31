package com.appcitas.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.appcitas.app.ui.screens.auth.AuthViewModel
import com.appcitas.app.ui.screens.auth.ForgotPasswordScreen
import com.appcitas.app.ui.screens.auth.LoginScreen
import com.appcitas.app.ui.screens.auth.RegisterScreen
import com.appcitas.app.ui.screens.chat.ChatListScreen
import com.appcitas.app.ui.screens.chat.ChatScreen
import com.appcitas.app.ui.screens.chat.CommunityChatScreen
import com.appcitas.app.ui.screens.discover.DiscoverScreen
import com.appcitas.app.ui.screens.discover.MatchScreen
import com.appcitas.app.ui.screens.icebreaker.IcebreakerScreen
import com.appcitas.app.ui.screens.profile.EditProfileScreen
import com.appcitas.app.ui.screens.profile.ProfileScreen
import com.appcitas.app.ui.screens.profile.VerificationScreen
import com.appcitas.app.ui.screens.settings.SettingsScreen

sealed class Screen(val route: String) {
    // Auth
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")

    // Main
    object Discover : Screen("discover")
    object ChatList : Screen("chat_list")
    object Profile : Screen("profile")
    object Settings : Screen("settings")

    // Detail screens
    object Chat : Screen("chat/{userId}") {
        fun createRoute(userId: String) = "chat/$userId"
    }
    object Match : Screen("match/{matchId}") {
        fun createRoute(matchId: String) = "match/$matchId"
    }
    object EditProfile : Screen("edit_profile")
    object Verification : Screen("verification")
    object CommunityChat : Screen("community_chat/{channelId}") {
        fun createRoute(channelId: String) = "community_chat/$channelId"
    }
    object Icebreaker : Screen("icebreaker")
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Discover, "Descubrir", Icons.Filled.Explore, Icons.Outlined.Explore),
    BottomNavItem(Screen.ChatList, "Chats", Icons.Filled.Chat, Icons.Outlined.Chat),
    BottomNavItem(Screen.Profile, "Perfil", Icons.Filled.Person, Icons.Outlined.Person),
    BottomNavItem(Screen.Settings, "Ajustes", Icons.Filled.Settings, Icons.Outlined.Settings)
)

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination?.route in bottomNavItems.map { it.screen.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.screen.route) {
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
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Auth screens
            composable(Screen.Login.route) {
                LoginScreen(
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                    onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                    onLoginSuccess = {
                        navController.navigate(Screen.Discover.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    viewModel = authViewModel
                )
            }

            composable(Screen.Register.route) {
                RegisterScreen(
                    onNavigateToLogin = { navController.popBackStack() },
                    onRegisterSuccess = {
                        navController.navigate(Screen.Discover.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    },
                    viewModel = authViewModel
                )
            }

            composable(Screen.ForgotPassword.route) {
                ForgotPasswordScreen(
                    onBackClick = { navController.popBackStack() },
                    viewModel = authViewModel
                )
            }

            // Main screens
            composable(Screen.Discover.route) {
                DiscoverScreen(
                    onMatchFound = { matchId ->
                        navController.navigate(Screen.Match.createRoute(matchId))
                    },
                    onNavigateToChat = { userId ->
                        navController.navigate(Screen.Chat.createRoute(userId))
                    }
                )
            }

            composable(Screen.ChatList.route) {
                ChatListScreen(
                    onChatClick = { userId ->
                        navController.navigate(Screen.Chat.createRoute(userId))
                    },
                    onCommunityClick = { channelId ->
                        navController.navigate(Screen.CommunityChat.createRoute(channelId))
                    }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    onEditProfile = { navController.navigate(Screen.EditProfile.route) },
                    onVerify = { navController.navigate(Screen.Verification.route) }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onLogout = {
                        authViewModel.logout()
                    }
                )
            }

            // Detail screens
            composable(
                route = Screen.Chat.route,
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) {
                ChatScreen(
                    onBackClick = { navController.popBackStack() },
                    onNavigateToIcebreaker = { navController.navigate(Screen.Icebreaker.route) }
                )
            }

            composable(
                route = Screen.Match.route,
                arguments = listOf(navArgument("matchId") { type = NavType.StringType })
            ) {
                MatchScreen(
                    onChatClick = { userId ->
                        navController.navigate(Screen.Chat.createRoute(userId))
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.EditProfile.route) {
                EditProfileScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.Verification.route) {
                VerificationScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.CommunityChat.route,
                arguments = listOf(navArgument("channelId") { type = NavType.StringType })
            ) {
                CommunityChatScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.Icebreaker.route) {
                IcebreakerScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
