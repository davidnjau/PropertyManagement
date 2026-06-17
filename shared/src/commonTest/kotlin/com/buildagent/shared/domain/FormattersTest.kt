package com.buildagent.shared.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class FormattersTest {

    @Test
    fun `currency formats whole number correctly`() {
        assertEquals("A$100.00", Formatters.currency(100.0))
    }

    @Test
    fun `currency formats decimal correctly`() {
        assertEquals("A$99.99", Formatters.currency(99.99))
    }

    @Test
    fun `currency formats zero`() {
        assertEquals("A$0.00", Formatters.currency(0.0))
    }

    @Test
    fun `currency returns dash for null`() {
        assertEquals("—", Formatters.currency(null))
    }

    @Test
    fun `currency formats large amount`() {
        assertEquals("A$1500.00", Formatters.currency(1500.0))
    }

    @Test
    fun `occupancyRate appends percent`() {
        assertEquals("85%", Formatters.occupancyRate(85.7))
    }

    @Test
    fun `occupancyRate returns zero percent for zero`() {
        assertEquals("0%", Formatters.occupancyRate(0.0))
    }

    @Test
    fun `paymentTypeLabel maps RENT`() {
        assertEquals("Rent", Formatters.paymentTypeLabel("RENT"))
    }

    @Test
    fun `paymentTypeLabel maps BOND`() {
        assertEquals("Bond", Formatters.paymentTypeLabel("BOND"))
    }

    @Test
    fun `paymentTypeLabel maps WATER`() {
        assertEquals("Water", Formatters.paymentTypeLabel("WATER"))
    }

    @Test
    fun `paymentTypeLabel maps FEE`() {
        assertEquals("Fee", Formatters.paymentTypeLabel("FEE"))
    }

    @Test
    fun `paymentTypeLabel returns raw string for unknown type`() {
        assertEquals("CUSTOM", Formatters.paymentTypeLabel("CUSTOM"))
    }
}
