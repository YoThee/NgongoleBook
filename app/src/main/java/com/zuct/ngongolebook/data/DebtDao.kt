package com.zuct.ngongolebook.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtDao {

    @Query(
        """
        SELECT d.*, COALESCE((SELECT SUM(p.amount) FROM payments p WHERE p.debtId = d.id), 0) AS paid
        FROM debts d
        ORDER BY d.dueDate ASC, d.id ASC
        """
    )
    fun observeAll(): Flow<List<DebtWithPaid>>

    @Query(
        """
        SELECT d.*, COALESCE((SELECT SUM(p.amount) FROM payments p WHERE p.debtId = d.id), 0) AS paid
        FROM debts d
        WHERE d.id = :id
        """
    )
    fun observeOne(id: Long): Flow<DebtWithPaid?>

    /** One-shot read used by the background worker. */
    @Query(
        """
        SELECT d.*, COALESCE((SELECT SUM(p.amount) FROM payments p WHERE p.debtId = d.id), 0) AS paid
        FROM debts d
        WHERE d.dueDate < :today
        """
    )
    suspend fun getPastDue(today: Long): List<DebtWithPaid>

    @Query("SELECT * FROM payments WHERE debtId = :debtId ORDER BY paidAt DESC, id DESC")
    fun observePayments(debtId: Long): Flow<List<Payment>>

    @Insert
    suspend fun insertDebt(debt: Debt): Long

    @Insert
    suspend fun insertPayment(payment: Payment): Long

    @Query("DELETE FROM debts WHERE id = :id")
    suspend fun deleteDebt(id: Long)
}
