package com.zuct.ngongolebook.domain

import com.zuct.ngongolebook.data.DebtWithPaid
import kotlin.math.round

enum class DebtStatus { PAID, OVERDUE, OPEN }

/** What is still owed, rounded to ngwee so floating point noise never shows up. */
val DebtWithPaid.balance: Double
    get() = (round((debt.amount - paid) * 100.0) / 100.0).coerceAtLeast(0.0)

fun DebtWithPaid.status(today: Long): DebtStatus = when {
    balance <= 0.0 -> DebtStatus.PAID
    debt.dueDate < today -> DebtStatus.OVERDUE
    else -> DebtStatus.OPEN
}
