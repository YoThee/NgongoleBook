package com.zuct.ngongolebook.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** One credit sale: a customer took goods and owes [amount] kwacha, due on [dueDate]. */
@Entity(tableName = "debts")
data class Debt(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerName: String,
    val phone: String,
    val item: String,
    val amount: Double,
    /** Epoch millis when the debt was recorded. */
    val createdAt: Long,
    /** Epoch millis, UTC midnight of the chosen due day (same format the DatePicker returns). */
    val dueDate: Long
)

/** A part-payment (or full payment) made against a [Debt]. Deleted automatically with its debt. */
@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = Debt::class,
            parentColumns = ["id"],
            childColumns = ["debtId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("debtId")]
)
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val debtId: Long,
    val amount: Double,
    val paidAt: Long
)

/** Result of the JOIN query: a debt plus the total paid so far. */
data class DebtWithPaid(
    @Embedded val debt: Debt,
    val paid: Double
)
