package com.buildagent.shared.domain

import com.buildagent.shared.models.MaintenancePriority
import com.buildagent.shared.models.MaintenanceStatus

object SlaUtils {
    private val slaDays = mapOf(
        MaintenancePriority.EMERGENCY to 1,
        MaintenancePriority.URGENT to 3,
        MaintenancePriority.ROUTINE to 10,
        MaintenancePriority.LOW to 30
    )

    fun slaDaysFor(priority: MaintenancePriority): Int = slaDays[priority] ?: 10

    fun isSlaBreached(slaTargetDate: String?, status: MaintenanceStatus): Boolean {
        if (slaTargetDate == null) return false
        if (status in listOf(MaintenanceStatus.COMPLETED, MaintenanceStatus.CLOSED, MaintenanceStatus.CANCELLED)) return false
        return true // Platform-specific date comparison done in expect/actual or passed in
    }
}
