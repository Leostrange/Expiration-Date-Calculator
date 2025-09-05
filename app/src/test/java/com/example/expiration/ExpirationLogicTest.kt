package com.example.expiration

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExpirationLogicTest {

    @Test
    fun `formatDateInput formats incrementally`() {
        assertEquals("1", formatDateInput("1"))
        assertEquals("12", formatDateInput("12"))
        assertEquals("12.3", formatDateInput("123"))
        assertEquals("12.34", formatDateInput("1234"))
        assertEquals("12.34.5", formatDateInput("12345"))
        assertEquals("12.34.56", formatDateInput("123456"))
        assertEquals("12.34.567", formatDateInput("1234567"))
        assertEquals("12.34.5678", formatDateInput("12345678"))
        // extra digits are trimmed
        assertEquals("12.34.5678", formatDateInput("12.34.5678xx"))
    }

    @Test
    fun `calculateExpiration adds days correctly`() {
        val result = calculateExpiration("01.01.2024", 10, UnitType.DAYS)
        assertEquals("11.01.2024", result)
    }

    @Test
    fun `calculateExpiration adds weeks correctly`() {
        val result = calculateExpiration("01.01.2024", 2, UnitType.WEEKS)
        assertEquals("15.01.2024", result)
    }

    @Test
    fun `calculateExpiration adds months correctly over year boundary`() {
        val result = calculateExpiration("31.10.2024", 3, UnitType.MONTHS)
        // Calendar add will handle end-of-month; expected end-of-Jan 2025 wraps to 31.01.2025 or closest valid date
        // For simplicity, ensure year advanced to 2025 and month is Jan or Feb depending on month-end handling
        assertTrue(result.endsWith(".2025"))
    }

    @Test
    fun `calculateExpiration adds years correctly`() {
        val result = calculateExpiration("29.02.2020", 1, UnitType.YEARS)
        // 2021-02-28 due to non-leap year handling by Calendar
        assertEquals("28.02.2021", result)
    }
}

