package com.buildagent.shared.domain

object Formatters {
    fun currency(amount: Double?): String {
        if (amount == null) return "—"
        return "A$${String.format("%.2f", amount)}"
    }

    fun occupancyRate(rate: Double): String = "${rate.toInt()}%"

    fun paymentTypeLabel(type: String): String = when (type) {
        "RENT" -> "Rent"
        "BOND" -> "Bond"
        "WATER" -> "Water"
        "FEE" -> "Fee"
        else -> type
    }
}
