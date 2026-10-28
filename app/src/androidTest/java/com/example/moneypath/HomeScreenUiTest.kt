package com.example.moneypath

import android.app.Application
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.moneypath.ui.MoneyPathViewModel
import com.example.moneypath.ui.screens.HomeScreen
import com.example.moneypath.ui.theme.MoneyPathTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var viewModel: MoneyPathViewModel

    @Before
    fun setUp() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        viewModel = MoneyPathViewModel(app)
    }

    @Test
    fun testHomeScreenRendersTitleAndSections() {
        composeTestRule.setContent {
            MoneyPathTheme {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToTransactions = {},
                    onNavigateToAddTransaction = {},
                    onNavigateToTransactionDetail = {},
                    onNavigateToBudgets = {},
                    onNavigateToAnalytics = {},
                    onNavigateToNotifications = {},
                    onNavigateToGoals = {},
                    onNavigateToAddGoal = {},
                    onNavigateToBills = {}
                )
            }
        }

        // Verify MoneyPath App Title
        composeTestRule.onNodeWithText("MoneyPath").assertIsDisplayed()

        // Verify Available Balance label
        composeTestRule.onNodeWithText("Available balance").assertIsDisplayed()

        // Verify Quick Action buttons
        composeTestRule.onNodeWithText("Add Expense").assertIsDisplayed()
        composeTestRule.onNodeWithText("Add Goal").assertIsDisplayed()
        composeTestRule.onNodeWithText("Set Budget").assertIsDisplayed()
        composeTestRule.onNodeWithText("View Summary").assertIsDisplayed()

        // Verify Budget Overview section
        composeTestRule.onNodeWithText("Budget Overview").assertIsDisplayed()

        // Verify Top Spending Categories section
        composeTestRule.onNodeWithText("Top Spending Categories").assertIsDisplayed()

        // Verify Recent Expenses section
        composeTestRule.onNodeWithText("Recent Expenses").assertIsDisplayed()

        // Verify Upcoming Bills section
        composeTestRule.onNodeWithText("Upcoming Bills").assertIsDisplayed()

        // Verify Savings Goals section
        composeTestRule.onNodeWithText("Savings Goals").assertIsDisplayed()
    }

    @Test
    fun testQuickActionButtonTriggersNavigation() {
        var addExpenseClicked = false
        var addGoalClicked = false

        composeTestRule.setContent {
            MoneyPathTheme {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToTransactions = {},
                    onNavigateToAddTransaction = { addExpenseClicked = true },
                    onNavigateToTransactionDetail = {},
                    onNavigateToBudgets = {},
                    onNavigateToAnalytics = {},
                    onNavigateToNotifications = {},
                    onNavigateToGoals = {},
                    onNavigateToAddGoal = { addGoalClicked = true },
                    onNavigateToBills = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Add Expense").performClick()
        assert(addExpenseClicked)

        composeTestRule.onNodeWithText("Add Goal").performClick()
        assert(addGoalClicked)
    }
}
