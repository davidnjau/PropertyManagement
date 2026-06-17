package com.buildagent.ui.utils

import kotlin.test.Test
import kotlin.test.assertEquals

class NumberFormatTest {

    @Test
    fun `fmt2dp formats whole number with two decimal places`() {
        assertEquals("100.00", 100.0.fmt2dp())
    }

    @Test
    fun `fmt2dp formats number with existing decimals`() {
        assertEquals("99.99", 99.99.fmt2dp())
    }

    @Test
    fun `fmt2dp formats zero`() {
        assertEquals("0.00", 0.0.fmt2dp())
    }

    @Test
    fun `fmt2dp rounds down correctly`() {
        assertEquals("10.12", 10.124.fmt2dp())
    }

    @Test
    fun `fmt2dp rounds up correctly`() {
        assertEquals("10.13", 10.125.fmt2dp())
    }

    @Test
    fun `fmt1dp formats number with one decimal`() {
        assertEquals("85.7", 85.7.fmt1dp())
    }

    @Test
    fun `fmt1dp formats whole number`() {
        assertEquals("50.0", 50.0.fmt1dp())
    }

    @Test
    fun `fmt0dp formats whole number without decimals`() {
        assertEquals("1500", 1500.0.fmt0dp())
    }

    @Test
    fun `fmt0dp truncates fractional part`() {
        assertEquals("99", 99.9.fmt0dp())
    }

    @Test
    fun `fmt0dp handles zero`() {
        assertEquals("0", 0.0.fmt0dp())
    }
}
