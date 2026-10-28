package com.example.moneypath

import android.app.Application
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.moneypath.ui.MoneyPathViewModel
import com.example.moneypath.ui.screens.SavingsGoalsScreen
import com.example.moneypath.ui.screens.TransactionsScreen
import com.example.moneypath.ui.theme.MoneyPathTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TransactionsAndGoalsScreenUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var viewModel: MoneyPathViewModel

    @Before
    fun setUp() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        viewModel = MoneyPathViewModel(app)
    }

    @Test
    fun testTransactionsScreenRendersAndFilters() {
        composeTestRule.setContent {
            MoneyPathTheme {
                TransactionsScreen(
                    viewModel = viewModel,
                    onNavigateToAddTransaction = {},
                    onNavigateToTransactionDetail = {}
                )
            }
        }

        // Verify Expenses title
        composeTestRule.onNodeWithText("Expenses").assertIsDisplayed()

        // Verify filter chips exist
        composeTestRule.onNodeWithText("All").assertIsDisplayed()
        composeTestRule.onNodeWithText("Expense").assertIsDisplayed()
        composeTestRule.onNodeWithText("Income").assertIsDisplayed()
    }

    @Test
    fun testSavingsGoalsScreenRendersTotalSavedAndGoalsList() {
        composeTestRule.setContent {
            MoneyPathTheme {
                SavingsGoalsScreen(
                    viewModel = viewModel,
                    onNavigateToAddGoal = {},
                    onNavigateToEditGoal = {}
                )
            }
        }

        // Verify Goals title
        composeTestRule.onNodeWithText("Goals").assertIsDisplayed()

        // Verify summary card
        composeTestRule.onNodeWithText("Total saved").assertIsDisplayed()

        // Verify Create Goal button
        composeTestRule.onNodeWithText("Create Goal").assertIsDisplayed()
    }
}
