package com.zuct.ngongolebook.domain

/**
 * Pure input checks. Each function returns an error message, or null when the value is fine.
 * No Android classes here, so these are easy to unit test.
 */
object Validators {

    const val MAX_AMOUNT = 1_000_000.0

    // Zambian mobile numbers: 09x / 07x / 055 / 095..., optionally written +260...
    private val phoneRegex = Regex("^(\\+?260|0)(9[5-8]|7[5-8]|55)\\d{7}$")

    fun name(value: String): String? = when {
        value.isBlank() -> "Enter the customer's name"
        value.trim().length < 2 -> "Name is too short"
        else -> null
    }

    fun item(value: String): String? =
        if (value.isBlank()) "Say what they took, e.g. 2kg sugar" else null

    /** Phone is optional, but if typed it must look like a Zambian number. */
    fun phone(value: String): String? {
        val digits = value.filter { !it.isWhitespace() }
        if (digits.isEmpty()) return null
        return if (phoneRegex.matches(digits)) null else "Use a Zambian number like 0977 123 456"
    }

    fun amount(value: String): String? {
        val parsed = parseAmount(value) ?: return "Enter a valid amount, e.g. 150 or 150.50"
        return when {
            parsed <= 0.0 -> "Amount must be more than K0"
            parsed > MAX_AMOUNT -> "Amount is too large"
            else -> null
        }
    }

    fun dueDate(dueDate: Long?, today: Long): String? = when {
        dueDate == null -> "Choose the date the customer promised to pay"
        dueDate < today -> "Due date cannot be in the past"
        else -> null
    }

    fun payment(value: String, balance: Double): String? {
        val parsed = parseAmount(value) ?: return "Enter a valid amount"
        return when {
            parsed <= 0.0 -> "Payment must be more than K0"
            parsed > balance + 0.004 -> "That is more than the balance (${Formatters.money(balance)})"
            else -> null
        }
    }

    /** Accepts "1,250.50" and "1250.5"; rejects empty, NaN and infinity. */
    fun parseAmount(value: String): Double? {
        val parsed = value.trim().replace(",", "").toDoubleOrNull() ?: return null
        return if (parsed.isNaN() || parsed.isInfinite()) null else parsed
    }
}
