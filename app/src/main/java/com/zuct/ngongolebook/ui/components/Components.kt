package com.zuct.ngongolebook.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zuct.ngongolebook.data.DebtWithPaid
import com.zuct.ngongolebook.domain.DebtStatus
import com.zuct.ngongolebook.domain.Formatters
import com.zuct.ngongolebook.domain.balance
import com.zuct.ngongolebook.domain.status

@Composable
fun SummaryCard(totalOwed: Double, overdueCount: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Total owed to you", style = MaterialTheme.typography.labelLarge)
            Text(
                Formatters.money(totalOwed),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            if (overdueCount > 0) {
                Text(
                    "$overdueCount overdue",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun DebtRow(
    item: DebtWithPaid,
    today: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val status = item.status(today)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    item.debt.customerName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (item.debt.item.isNotBlank()) {
                    Text(item.debt.item, style = MaterialTheme.typography.bodyMedium)
                }
                Text(
                    StatusLabel.text(status, item.debt.dueDate),
                    style = MaterialTheme.typography.bodySmall,
                    color = StatusLabel.color(status)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    Formatters.money(item.balance),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "of ${Formatters.money(item.debt.amount)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

object StatusLabel {
    fun text(status: DebtStatus, dueDate: Long): String = when (status) {
        DebtStatus.PAID -> "Fully paid"
        DebtStatus.OVERDUE -> "Overdue since ${Formatters.dueDate(dueDate)}"
        DebtStatus.OPEN -> "Due ${Formatters.dueDate(dueDate)}"
    }

    @Composable
    fun color(status: DebtStatus) = when (status) {
        DebtStatus.PAID -> MaterialTheme.colorScheme.primary
        DebtStatus.OVERDUE -> MaterialTheme.colorScheme.error
        DebtStatus.OPEN -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

/** Shown instead of a blank screen when there is nothing to list. */
@Composable
fun EmptyState(title: String, message: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
    }
}
