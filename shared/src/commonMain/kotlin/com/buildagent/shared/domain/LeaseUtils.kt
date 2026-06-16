package com.buildagent.shared.domain

import com.buildagent.shared.models.LeaseStatus

object LeaseUtils {
    fun computeStatus(storedStatus: LeaseStatus, endDate: String?, daysUntilEnd: Int?): LeaseStatus {
        if (storedStatus == LeaseStatus.VACATED || storedStatus == LeaseStatus.TERMINATED) return storedStatus
        if (endDate == null) return LeaseStatus.PERIODIC
        val days = daysUntilEnd ?: return LeaseStatus.ACTIVE
        return when {
            days < 0 -> LeaseStatus.EXPIRED
            days <= 60 -> LeaseStatus.EXPIRING_SOON
            else -> LeaseStatus.ACTIVE
        }
    }

    fun displayLabel(status: LeaseStatus): String = when (status) {
        LeaseStatus.ACTIVE -> "Active"
        LeaseStatus.PERIODIC -> "Periodic"
        LeaseStatus.EXPIRING_SOON -> "Expiring Soon"
        LeaseStatus.EXPIRED -> "Expired"
        LeaseStatus.VACATED -> "Vacated"
        LeaseStatus.TERMINATED -> "Terminated"
    }
}
