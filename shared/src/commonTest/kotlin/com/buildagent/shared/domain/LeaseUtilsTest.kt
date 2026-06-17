package com.buildagent.shared.domain

import com.buildagent.shared.models.LeaseStatus
import kotlin.test.Test
import kotlin.test.assertEquals

class LeaseUtilsTest {

    // computeStatus — terminal statuses are preserved regardless of other inputs
    @Test
    fun `computeStatus returns VACATED when stored status is VACATED`() {
        assertEquals(LeaseStatus.VACATED, LeaseUtils.computeStatus(LeaseStatus.VACATED, "2025-01-01", 10))
    }

    @Test
    fun `computeStatus returns TERMINATED when stored status is TERMINATED`() {
        assertEquals(LeaseStatus.TERMINATED, LeaseUtils.computeStatus(LeaseStatus.TERMINATED, "2025-01-01", 10))
    }

    // No end date → PERIODIC regardless of stored status
    @Test
    fun `computeStatus returns PERIODIC when endDate is null`() {
        assertEquals(LeaseStatus.PERIODIC, LeaseUtils.computeStatus(LeaseStatus.ACTIVE, null, null))
    }

    // Expired (negative days remaining)
    @Test
    fun `computeStatus returns EXPIRED when days is negative`() {
        assertEquals(LeaseStatus.EXPIRED, LeaseUtils.computeStatus(LeaseStatus.ACTIVE, "2024-01-01", -1))
    }

    // Expiring soon (0..60 days)
    @Test
    fun `computeStatus returns EXPIRING_SOON when days is zero`() {
        assertEquals(LeaseStatus.EXPIRING_SOON, LeaseUtils.computeStatus(LeaseStatus.ACTIVE, "2025-01-01", 0))
    }

    @Test
    fun `computeStatus returns EXPIRING_SOON when days is 60`() {
        assertEquals(LeaseStatus.EXPIRING_SOON, LeaseUtils.computeStatus(LeaseStatus.ACTIVE, "2025-01-01", 60))
    }

    // Active (>60 days)
    @Test
    fun `computeStatus returns ACTIVE when days is 61`() {
        assertEquals(LeaseStatus.ACTIVE, LeaseUtils.computeStatus(LeaseStatus.ACTIVE, "2025-01-01", 61))
    }

    @Test
    fun `computeStatus returns ACTIVE when days is large`() {
        assertEquals(LeaseStatus.ACTIVE, LeaseUtils.computeStatus(LeaseStatus.ACTIVE, "2025-01-01", 365))
    }

    // No days provided but end date exists → treat as ACTIVE
    @Test
    fun `computeStatus returns ACTIVE when daysUntilEnd is null but endDate exists`() {
        assertEquals(LeaseStatus.ACTIVE, LeaseUtils.computeStatus(LeaseStatus.ACTIVE, "2025-12-31", null))
    }

    // displayLabel covers all statuses
    @Test
    fun `displayLabel returns correct labels for all statuses`() {
        assertEquals("Active", LeaseUtils.displayLabel(LeaseStatus.ACTIVE))
        assertEquals("Periodic", LeaseUtils.displayLabel(LeaseStatus.PERIODIC))
        assertEquals("Expiring Soon", LeaseUtils.displayLabel(LeaseStatus.EXPIRING_SOON))
        assertEquals("Expired", LeaseUtils.displayLabel(LeaseStatus.EXPIRED))
        assertEquals("Vacated", LeaseUtils.displayLabel(LeaseStatus.VACATED))
        assertEquals("Terminated", LeaseUtils.displayLabel(LeaseStatus.TERMINATED))
    }
}
