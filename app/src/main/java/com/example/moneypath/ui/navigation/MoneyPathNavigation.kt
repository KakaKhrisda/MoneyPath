package com.example.moneypath.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.moneypath.ui.MoneyPathViewModel
import com.example.moneypath.ui.screens.*
import com.example.moneypath.ui.theme.PrimaryGreen

// Moves between the five footer tabs. Every tab pops back to "home",
// which is always the bottom of the stack once the user is logged in.
private fun NavHostController.navigateToTab(route: String) {
    if (route == "home") {
        // Remove everything above Home and keep Home itself
        val reachedHome = popBackStack("home", inclusive = false)
        if (!reachedHome) {
            navigate("home") {
                launchSingleTop = true
            }
        }
    } else {
        navigate(route) {
            popUpTo("home") { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
}

@Composable
fun MoneyPathAppNavigation(
    viewModel: MoneyPathViewModel,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf("home", "transactions", "budgets", "savings_goals", "profile")

    val primaryGreen = PrimaryGreen
    val textSecondary = Color(0xFF6B7280)

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Home - Fixed to always navigate reliably to home
                        CustomBottomNavItem(
                            title = "Home",
                            icon = Icons.Default.Home,
                            selected = currentRoute == "home",
                            primaryColor = primaryGreen,
                            unselectedColor = textSecondary,
                            onClick = {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        )

                        // 2. Expenses
                        CustomBottomNavItem(
                            title = "Expenses",
                            icon = Icons.AutoMirrored.Filled.ReceiptLong,
                            selected = currentRoute == "transactions",
                            primaryColor = primaryGreen,
                            unselectedColor = textSecondary,
                            onClick = {
                                navController.navigate("transactions") {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )

                        // 3. Center Plus Button Inside Footer (Takes you directly to Add Expense)
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(primaryGreen)
                                .clickable {
                                    navController.navigate("add_transaction/new") {
                                        launchSingleTop = true
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Expense",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // 4. Budget
                        CustomBottomNavItem(
                            title = "Budget",
                            icon = Icons.Default.AccountBalanceWallet,
                            selected = currentRoute == "budgets",
                            primaryColor = primaryGreen,
                            unselectedColor = textSecondary,
                            onClick = {
                                navController.navigate("budgets") {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )

                        // 5. More
                        CustomBottomNavItem(
                            title = "More",
                            icon = Icons.Default.MoreHoriz,
                            selected = currentRoute == "profile",
                            primaryColor = primaryGreen,
                            unselectedColor = textSecondary,
                            onClick = {
                                navController.navigate("profile") {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
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
            startDestination = "onboarding",
            modifier = Modifier.padding(innerPadding)
        ) {
            // Screen #1: Onboarding
            composable("onboarding") {
                OnboardingScreen(
                    onGetStarted = { navController.navigate("login") },
                    onLogin = { navController.navigate("login") }
                )
            }

            // Screen #2: Login
            composable("login") {
                LoginScreen(
                    viewModel = viewModel,
                    onLoginSuccess = { navController.navigate("home") { popUpTo("onboarding") { inclusive = true } } },
                    onNavigateToRegister = { navController.navigate("register") },
                    onNavigateToForgotPassword = { navController.navigate("forgot_password") }
                )
            }

            // Screen #3: Register
            composable("register") {
                RegisterScreen(
                    viewModel = viewModel,
                    onRegisterSuccess = { navController.navigate("home") { popUpTo("login") { inclusive = true } } },
                    onNavigateToLogin = { navController.navigate("login") }
                )
            }

            // Screen #4: Home Dashboard
            composable("home") {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToTransactions = { navController.navigate("transactions") },
                    onNavigateToAddTransaction = { navController.navigate("add_transaction/new") },
                    onNavigateToTransactionDetail = { id -> navController.navigate("transaction_detail/$id") },
                    onNavigateToBudgets = { navController.navigate("budgets") },
                    onNavigateToAnalytics = { navController.navigate("analytics") },
                    onNavigateToNotifications = { navController.navigate("notifications") },
                    onNavigateToGoals = { navController.navigate("savings_goals") },
                    onNavigateToAddGoal = { navController.navigate("add_goal") },
                    onNavigateToBills = { navController.navigate("bills") }
                )
            }

            // Screen #5: Transactions List
            composable("transactions") {
                TransactionsScreen(
                    viewModel = viewModel,
                    onNavigateToAddTransaction = { navController.navigate("add_transaction/new") },
                    onNavigateToTransactionDetail = { id -> navController.navigate("transaction_detail/$id") }
                )
            }

            // Screen #6: Add/Edit Transaction
            // "new" = blank expense, "new_income" = blank income, anything else = an existing id
            composable("add_transaction/{transactionId}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("transactionId")
                AddEditTransactionScreen(
                    viewModel = viewModel,
                    transactionId = id,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Screen #7: Transaction Detail
            composable("transaction_detail/{transactionId}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("transactionId") ?: ""
                TransactionDetailScreen(
                    viewModel = viewModel,
                    transactionId = id,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { editId -> navController.navigate("add_transaction/$editId") }
                )
            }

            // Screen #8: Budgets Overview
            composable("budgets") {
                BudgetsScreen(
                    viewModel = viewModel,
                    onNavigateToAddBudget = { navController.navigate("add_budget") },
                    onNavigateToAddSpecificBudget = { navController.navigate("add_category_budget") }
                )
            }

            // Screen #9: Add Budget (Move Funds)
            composable("add_budget") {
                AddBudgetScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Screen #25: Add Category Budget
            composable("add_category_budget") {
                AddCategoryBudgetScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Screen #10: Analytics
            composable("analytics") {
                AnalyticsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Screen #11: Savings Goals
            composable("savings_goals") {
                SavingsGoalsScreen(
                    viewModel = viewModel,
                    onNavigateToAddGoal = { navController.navigate("add_goal") },
                    onNavigateToEditGoal = { goalId -> navController.navigate("add_goal/$goalId") }
                )
            }

            // Screen #12: Add / Edit Goal
            composable("add_goal") {
                AddGoalScreen(
                    viewModel = viewModel,
                    goalId = null,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("add_goal/{goalId}") { backStackEntry ->
                val goalId = backStackEntry.arguments?.getString("goalId")
                AddGoalScreen(
                    viewModel = viewModel,
                    goalId = goalId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Screen #13: Accounts & Wallets
            composable("accounts") {
                AccountsScreen(
                    viewModel = viewModel,
                    onNavigateToAddAccount = { navController.navigate("add_account") }
                )
            }

            // Screen #14: Add Account
            composable("add_account") {
                AddAccountScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Screen #15: Categories Management
            composable("categories") {
                CategoriesScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Screen #16: Profile & Settings
            composable("profile") {
                ProfileScreen(
                    viewModel = viewModel,
                    onNavigateToGoals = { navController.navigate("savings_goals") },
                    onNavigateToAddIncome = { navController.navigate("income_screen") },
                    onNavigateToCategories = { navController.navigate("categories") },
                    onNavigateToAnalytics = { navController.navigate("analytics") },
                    onNavigateToBills = { navController.navigate("bills") },
                    onNavigateToNotifications = { navController.navigate("notifications") },
                    onNavigateToCurrency = { navController.navigate("currency_selection") },
                    onNavigateToOfflineData = { navController.navigate("offline_data") },
                    onNavigateToSettings = { navController.navigate("settings") },
                    onNavigateToHelpSupport = { navController.navigate("help_support") },
                    onNavigateToAbout = { navController.navigate("about") },
                    onLogout = { navController.navigate("login") { popUpTo(0) } }
                )
            }

            // Screen #27: Income Screen Management
            composable("income_screen") {
                IncomeScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Screen #24: Bills Management
            composable("bills") {
                BillsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Screen #17: Notifications
            composable("notifications") {
                NotificationsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onOpenTarget = { target ->
                        when (target) {
                            "budgets", "savings_goals", "transactions" -> navController.navigateToTab(target)
                            else -> navController.navigate(target)
                        }
                    }
                )
            }

            // Screen #18: Currency Selection
            composable("currency_selection") {
                CurrencySelectionScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Screen #19: Offline Data
            composable("offline_data") {
                OfflineDataScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Screen #20: Settings
            composable("settings") {
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEditProfile = { navController.navigate("edit_profile") },
                    onNavigateToChangePassword = { navController.navigate("change_password") },
                    onNavigateToLanguage = { navController.navigate("language_selection") }
                )
            }

            // Screen #26: Edit Profile
            composable("edit_profile") {
                EditProfileScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Screen #27: Change Password
            composable("change_password") {
                ChangePasswordScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Screen #28: Language Selection
            composable("language_selection") {
                LanguageSelectionScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Screen #21: Help and Support
            composable("help_support") {
                HelpSupportScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Screen #22: About MoneyPath
            composable("about") {
                AboutScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Screen #23: Forgot Password
            composable("forgot_password") {
                ForgotPasswordScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun CustomBottomNavItem(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    primaryColor: Color,
    unselectedColor: Color,
    onClick: () -> Unit
) {
    val tint = if (selected) primaryColor else unselectedColor

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = tint,
                fontSize = 11.sp
            )
        )
    }
}