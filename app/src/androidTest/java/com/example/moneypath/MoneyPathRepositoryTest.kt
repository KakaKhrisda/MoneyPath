package com.example.moneypath

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.moneypath.data.DateUtils
import com.example.moneypath.data.MoneyPathRepository
import com.example.moneypath.data.models.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class MoneyPathRepositoryTest {

    private lateinit var repository: MoneyPathRepository

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // Clear test preferences before each test
        context.getSharedPreferences("moneypath_data", 0).edit().clear().commit()
        repository = MoneyPathRepository(context)
    }

    @Test
    fun testAuthenticationRegisterAndLogin() {
        val registered = repository.registerAccount("Test User", "testuser@example.com", "password123")
        assertTrue(registered)

        val duplicateRegistered = repository.registerAccount("Test User 2", "testuser@example.com", "password123")
        assertFalse("Duplicate email registration should fail", duplicateRegistered)

        val loginSuccess = repository.loginAccount("testuser@example.com", "password123")
        assertTrue(loginSuccess)

        val loginWrongPass = repository.loginAccount("testuser@example.com", "wrongpass")
        assertFalse(loginWrongPass)
    }

    @Test
    fun testAddAndUpdateExpenseTransaction() {
        val todayStr = DateUtils.format(LocalDate.now())
        val tx = Transaction(
            id = "test_tx_1",
            title = "Lunch Meal",
            amount = 4500.0,
            category = "Food & Dining",
            date = todayStr,
            time = "01:00 PM",
            type = TransactionType.EXPENSE,
            accountName = "Cash Wallet",
            note = "Campus canteen"
        )

        val added = repository.addTransaction(tx)
        assertTrue(added)

        val found = repository.transactions.value.find { it.id == "test_tx_1" }
        assertNotNull(found)
        assertEquals("Lunch Meal", found?.title)
        assertEquals(4500.0, found?.amount ?: 0.0, 0.01)

        val updated = found!!.copy(title = "Buffet Lunch", amount = 5000.0)
        val updateSuccess = repository.updateTransaction(updated)
        assertTrue(updateSuccess)

        val foundUpdated = repository.transactions.value.find { it.id == "test_tx_1" }
        assertEquals("Buffet Lunch", foundUpdated?.title)
        assertEquals(5000.0, foundUpdated?.amount ?: 0.0, 0.01)

        repository.deleteTransaction("test_tx_1")
        assertNull(repository.transactions.value.find { it.id == "test_tx_1" })
    }

    @Test
    fun testAddIncomeTransactionUpdatesAvailableBalance() {
        val initialTxCount = repository.transactions.value.size
        val todayStr = DateUtils.format(LocalDate.now())
        val incomeTx = Transaction(
            id = "test_inc_1",
            title = "Freelance Design",
            amount = 120000.0,
            category = "Freelance",
            date = todayStr,
            time = "10:00 AM",
            type = TransactionType.INCOME,
            accountName = "Main Checking",
            note = "Client payment"
        )

        val added = repository.addTransaction(incomeTx)
        assertTrue(added)
        assertEquals(initialTxCount + 1, repository.transactions.value.size)
    }

    @Test
    fun testSaveBudgetPlan() {
        val allocations = mapOf(
            "Food & Dining" to 40000.0,
            "Transportation" to 20000.0,
            "Entertainment" to 15000.0
        )
        val saved = repository.saveBudgetPlan(100000.0, allocations)
        assertTrue(saved)
        assertEquals(100000.0, repository.totalBudget.value, 0.01)

        val foodBudget = repository.budgets.value.find { it.category == "Food & Dining" }
        assertNotNull(foodBudget)
        assertEquals(40000.0, foodBudget?.monthlyLimit ?: 0.0, 0.01)
    }

    @Test
    fun testMoveBudgetFunds() {
        val initialFood = repository.budgets.value.find { it.category == "Food & Dining" }?.monthlyLimit ?: 0.0
        val initialTransport = repository.budgets.value.find { it.category == "Transportation" }?.monthlyLimit ?: 0.0

        val moved = repository.moveBudgetFunds("Food & Dining", "Transportation", 5000.0)
        assertTrue(moved)

        val newFood = repository.budgets.value.find { it.category == "Food & Dining" }?.monthlyLimit ?: 0.0
        val newTransport = repository.budgets.value.find { it.category == "Transportation" }?.monthlyLimit ?: 0.0

        assertEquals(initialFood - 5000.0, newFood, 0.01)
        assertEquals(initialTransport + 5000.0, newTransport, 0.01)
    }

    @Test
    fun testGoalsAddAndDeposit() {
        val goal = Goal(
            id = "test_goal_1",
            title = "New Phone",
            targetAmount = 150000.0,
            currentAmount = 20000.0,
            targetDate = DateUtils.format(LocalDate.now().plusMonths(3)),
            iconName = "PhoneAndroid",
            colorHex = 0xFFF97316
        )

        val added = repository.addGoal(goal)
        assertTrue(added)

        val deposited = repository.addDepositToGoal("test_goal_1", 30000.0)
        assertTrue(deposited)

        val updatedGoal = repository.goals.value.find { it.id == "test_goal_1" }
        assertEquals(50000.0, updatedGoal?.currentAmount ?: 0.0, 0.01)
    }

    @Test
    fun testAddAndToggleBillPaid() {
        val bill = BillItem(
            id = "test_bill_1",
            title = "Internet Subscription",
            amount = 25000.0,
            dueDate = "Due 30 Nov 2026",
            category = "Bills & Utilities",
            isPaid = false
        )

        repository.addBill(bill)
        val found = repository.bills.value.find { it.id == "test_bill_1" }
        assertNotNull(found)
        assertFalse(found!!.isPaid)

        repository.toggleBillPaid("test_bill_1")
        val toggled = repository.bills.value.find { it.id == "test_bill_1" }
        assertTrue(toggled!!.isPaid)
    }

    @Test
    fun testNotificationsMarkAllRead() {
        repository.markAllNotificationsAsRead()
        val unreadCount = repository.notifications.value.count { !it.isRead }
        assertEquals(0, unreadCount)
    }
}
