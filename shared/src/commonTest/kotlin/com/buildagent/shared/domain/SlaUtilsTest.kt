package com.buildagent.shared.domain

import com.buildagent.shared.models.MaintenancePriority
import com.buildagent.shared.models.MaintenanceStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SlaUtilsTest {

    @Test
    fun `slaDaysFor EMERGENCY returns 1`() {
        assertEquals(1, SlaUtils.slaDaysFor(MaintenancePriority.EMERGENCY))
    }

    @Test
    fun `slaDaysFor URGENT returns 3`() {
        assertEquals(3, SlaUtils.slaDaysFor(MaintenancePriority.URGENT))
    }

    @Test
    fun `slaDaysFor ROUTINE returns 10`() {
        assertEquals(10, SlaUtils.slaDaysFor(MaintenancePriority.ROUTINE))
    }

    @Test
    fun `slaDaysFor LOW returns 30`() {
        assertEquals(30, SlaUtils.slaDaysFor(MaintenancePriority.LOW))
    }

    @Test
    fun `isSlaBreached returns false when slaTargetDate is null`() {
        assertFalse(SlaUtils.isSlaBreached(null, MaintenanceStatus.REPORTED))
    }

    @Test
    fun `isSlaBreached returns false when status is COMPLETED`() {
        assertFalse(SlaUtils.isSlaBreached("2025-01-01", MaintenanceStatus.COMPLETED))
    }

    @Test
    fun `isSlaBreached returns false when status is CLOSED`() {
        assertFalse(SlaUtils.isSlaBreached("2025-01-01", MaintenanceStatus.CLOSED))
    }

    @Test
    fun `isSlaBreached returns false when status is CANCELLED`() {
        assertFalse(SlaUtils.isSlaBreached("2025-01-01", MaintenanceStatus.CANCELLED))
    }

    @Test
    fun `isSlaBreached returns true for open status with target date`() {
        assertTrue(SlaUtils.isSlaBreached("2025-01-01", MaintenanceStatus.REPORTED))
        assertTrue(SlaUtils.isSlaBreached("2025-01-01", MaintenanceStatus.IN_PROGRESS))
        assertTrue(SlaUtils.isSlaBreached("2025-01-01", MaintenanceStatus.ASSIGNED))
    }
}
