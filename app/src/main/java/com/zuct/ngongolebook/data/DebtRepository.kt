package com.zuct.ngongolebook.data

import com.zuct.ngongolebook.domain.balance
import kotlinx.coroutines.flow.Flow

/**
 * The only class the rest of the app talks to for data.
 * Screens and ViewModels never see Room or the DAO directly.
 */
class DebtRepository(private val dao: DebtDao) {

    val debts: Flow<List<DebtWithPaid>> = dao.observeAll()

    fun debt(id: Long): Flow<DebtWithPaid?> = dao.observeOne(id)

    fun payments(debtId: Long): Flow<List<Payment>> = dao.observePayments(debtId)

    suspend fun addDebt(
        customerName: String,
        phone: String,
        item: String,
        amount: Double,
        dueDate: Long
    ): Long = dao.insertDebt(
        Debt(
            customerName = customerName.trim(),
            phone = phone.filter { !it.isWhitespace() },
            item = item.trim(),
            amount = amount,
            createdAt = System.currentTimeMillis(),
            dueDate = dueDate
        )
    )

    suspend fun addPayment(debtId: Long, amount: Double) {
        dao.insertPayment(Payment(debtId = debtId, amount = amount, paidAt = System.currentTimeMillis()))
    }

    suspend fun deleteDebt(id: Long) = dao.deleteDebt(id)

    /** Debts whose due day has passed and which still have a balance. */
    suspend fun overdue(today: Long): List<DebtWithPaid> =
        dao.getPastDue(today).filter { it.balance > 0.0 }
}
