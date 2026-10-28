package com.example.moneypath.data

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {
    private val displayFormat = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)

    fun format(date: LocalDate): String = displayFormat.format(date)

    // Understands "24 Oct 2026" and the older "2025-10-16"
    fun parse(text: String): LocalDate? =
        runCatching { LocalDate.parse(text.trim(), displayFormat) }.getOrNull()
            ?: runCatching { LocalDate.parse(text.trim()) }.getOrNull()

    fun isInCurrentMonth(text: String): Boolean {
        val d = parse(text) ?: return false
        return YearMonth.from(d) == YearMonth.now()
    }

    fun friendly(text: String): String {
        val d = parse(text) ?: return text
        val today = LocalDate.now()
        return when (d) {
            today -> "Today · ${format(d)}"
            today.minusDays(1) -> "Yesterday · ${format(d)}"
            else -> format(d)
        }
    }
}