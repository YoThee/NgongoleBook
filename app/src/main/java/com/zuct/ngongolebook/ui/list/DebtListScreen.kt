package com.zuct.ngongolebook.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zuct.ngongolebook.ui.components.DebtRow
import com.zuct.ngongolebook.ui.components.EmptyState
import com.zuct.ngongolebook.ui.components.SummaryCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtListScreen(
    viewModel: DebtListViewModel,
    onAddDebt: () -> Unit,
    onOpenDebt: (Long) -> Unit,
    onOpenSettings: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ngongole Book") },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddDebt,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New debt") }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

                !state.hasAnyDebts -> EmptyState(
                    title = "No debts recorded yet",
                    message = "Tap \"New debt\" when a customer takes goods on credit. " +
                        "You will see who owes you and who is late."
                )

                else -> {
                    SummaryCard(
                        totalOwed = state.totalOwed,
                        overdueCount = state.overdueCount,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)
                    )
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = viewModel::onQueryChange,
                        label = { Text("Search customer") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    Row(
                        Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DebtFilter.entries.forEach { option ->
                            FilterChip(
                                selected = state.filter == option,
                                onClick = { viewModel.onFilterChange(option) },
                                label = { Text(option.label) }
                            )
                        }
                    }

                    if (state.items.isEmpty()) {
                        EmptyState(
                            title = "Nothing here",
                            message = "No debts match this search or filter."
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 96.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(state.items, key = { it.debt.id }) { item ->
                                DebtRow(
                                    item = item,
                                    today = state.today,
                                    onClick = { onOpenDebt(item.debt.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
