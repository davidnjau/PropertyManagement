package com.buildagent.shared.domain

object Formatters {
    fun currency(amount: Double?): String {
        if (amount == null) return "—"
        val rounded = (amount * 100).toLong() / 100.0
        val whole = rounded.toLong()
        val cents = ((rounded - whole) * 100).toLong()
        return "A$$whole.${cents.toString().padStart(2, '0')}"
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
