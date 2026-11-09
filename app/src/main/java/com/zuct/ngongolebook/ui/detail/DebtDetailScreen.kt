package com.zuct.ngongolebook.ui.detail

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zuct.ngongolebook.data.DebtWithPaid
import com.zuct.ngongolebook.data.Payment
import com.zuct.ngongolebook.domain.Formatters
import com.zuct.ngongolebook.domain.balance
import com.zuct.ngongolebook.domain.status
import com.zuct.ngongolebook.ui.components.EmptyState
import com.zuct.ngongolebook.ui.components.StatusLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtDetailScreen(
    viewModel: DebtDetailViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Debt details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state is DetailUiState.Loaded) {
                        IconButton(onClick = { confirmDelete = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete debt")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (val current = state) {
                DetailUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                DetailUiState.NotFound -> EmptyState(
                    title = "This debt no longer exists",
                    message = "It may have been deleted. Go back to see your list."
                )

                is DetailUiState.Loaded -> DetailContent(
                    debt = current.debt,
                    payments = current.payments,
                    viewModel = viewModel
                )
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete this debt?") },
            text = { Text("The debt and all its payments will be removed. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    viewModel.delete(onDeleted = onBack)
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun DetailContent(
    debt: DebtWithPaid,
    payments: List<Payment>,
    viewModel: DebtDetailViewModel
) {
    val context = LocalContext.current
    val today = Formatters.todayUtcMidnight()
    val status = debt.status(today)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        debt.debt.customerName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (debt.debt.item.isNotBlank()) Text(debt.debt.item)
                    if (debt.debt.phone.isNotBlank()) Text("Phone: ${debt.debt.phone}")
                    Text(
                        StatusLabel.text(status, debt.debt.dueDate),
                        color = StatusLabel.color(status),
                        fontWeight = FontWeight.SemiBold
                    )
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    AmountLine("Original amount", Formatters.money(debt.debt.amount))
                    AmountLine("Paid so far", Formatters.money(debt.paid))
                    AmountLine("Balance", Formatters.money(debt.balance), bold = true)
                }
            }
        }

        if (debt.debt.phone.isNotBlank()) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { dial(context, debt.debt.phone) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("  Call")
                    }
                    OutlinedButton(
                        onClick = { sendReminderSms(context, debt) },
                        enabled = debt.balance > 0.0,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("  SMS reminder")
                    }
                }
            }
        }

        if (debt.balance > 0.0) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Record a payment", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = viewModel.paymentText,
                            onValueChange = viewModel::onPaymentText,
                            label = { Text("Amount paid (Kwacha)") },
                            prefix = { Text("K") },
                            isError = viewModel.paymentError != null,
                            supportingText = { viewModel.paymentError?.let { Text(it) } },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { viewModel.recordPayment(debt.balance) }) {
                                Text("Record payment")
                            }
                            TextButton(onClick = { viewModel.fillFullBalance(debt.balance) }) {
                                Text("Full balance")
                            }
                        }
                    }
                }
            }
        }

        item {
            Text("Payment history", style = MaterialTheme.typography.titleMedium)
        }

        if (payments.isEmpty()) {
            item { Text("No payments yet.", style = MaterialTheme.typography.bodyMedium) }
        } else {
            items(payments, key = { it.id }) { payment ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(Formatters.moment(payment.paidAt))
                    Text(Formatters.money(payment.amount), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun AmountLine(label: String, value: String, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
        Text(value, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
    }
}

/** Opens the dialer with the number filled in. No CALL_PHONE permission needed. */
private fun dial(context: Context, phone: String) {
    openExternal(context, Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")), "No phone app found")
}

private fun sendReminderSms(context: Context, debt: DebtWithPaid) {
    val message = "Hello ${debt.debt.customerName}, a friendly reminder that you owe " +
        "${Formatters.money(debt.balance)}, promised for ${Formatters.dueDate(debt.debt.dueDate)}. Thank you."
    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${debt.debt.phone}")).apply {
        putExtra("sms_body", message)
    }
    openExternal(context, intent, "No messaging app found")
}

/** A phone without a dialer/SMS app (e.g. an emulator) must show a message, not crash. */
private fun openExternal(context: Context, intent: Intent, errorMessage: String) {
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
    }
}
