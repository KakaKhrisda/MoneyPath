package com.example.moneypath

import com.example.moneypath.data.DateUtils
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class DateUtilsTest {

    @Test
    fun testDateFormatAndParse() {
        val today = LocalDate.of(2026, 10, 24)
        val formatted = DateUtils.format(today)
        assertEquals("24 Oct 2026", formatted)

        val parsed = DateUtils.parse(formatted)
        assertNotNull(parsed)
        assertEquals(today, parsed)
    }

    @Test
    fun testParseIsoFormatFallback() {
        val parsed = DateUtils.parse("2025-02-24")
        assertNotNull(parsed)
        assertEquals(LocalDate.of(2025, 2, 24), parsed)
    }

    @Test
    fun testIsInCurrentMonth() {
        val nowStr = DateUtils.format(LocalDate.now())
        assertTrue(DateUtils.isInCurrentMonth(nowStr))

        val oldStr = DateUtils.format(LocalDate.now().minusYears(2))
        assertFalse(DateUtils.isInCurrentMonth(oldStr))
    }

    @Test
    fun testFriendlyDateText() {
        val today = LocalDate.now()
        val todayStr = DateUtils.format(today)
        assertTrue(DateUtils.friendly(todayStr).startsWith("Today"))

        val yesterdayStr = DateUtils.format(today.minusDays(1))
        assertTrue(DateUtils.friendly(yesterdayStr).startsWith("Yesterday"))
    }
}
