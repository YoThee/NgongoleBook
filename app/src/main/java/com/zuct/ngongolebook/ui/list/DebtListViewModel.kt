package com.zuct.ngongolebook.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.zuct.ngongolebook.data.DebtRepository
import com.zuct.ngongolebook.data.DebtWithPaid
import com.zuct.ngongolebook.domain.DebtStatus
import com.zuct.ngongolebook.domain.Formatters
import com.zuct.ngongolebook.domain.balance
import com.zuct.ngongolebook.domain.status
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class DebtFilter(val label: String) {
    OWING("Owing"),
    OVERDUE("Overdue"),
    PAID("Paid"),
    ALL("All")
}

data class DebtListUiState(
    val isLoading: Boolean = true,
    val items: List<DebtWithPaid> = emptyList(),
    val hasAnyDebts: Boolean = false,
    val totalOwed: Double = 0.0,
    val overdueCount: Int = 0,
    val today: Long = 0L,
    val query: String = "",
    val filter: DebtFilter = DebtFilter.OWING
)

/**
 * Holds the list screen's state. Because it lives in a ViewModel, the search text,
 * the selected filter and the list all survive screen rotation.
 */
class DebtListViewModel(repository: DebtRepository) : ViewModel() {

    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow(DebtFilter.OWING)

    val state: StateFlow<DebtListUiState> =
        combine(repository.debts, query, filter) { all, q, f ->
            val today = Formatters.todayUtcMidnight()
            val visible = all
                .filter { debt ->
                    when (f) {
                        DebtFilter.OWING -> debt.status(today) != DebtStatus.PAID
                        DebtFilter.OVERDUE -> debt.status(today) == DebtStatus.OVERDUE
                        DebtFilter.PAID -> debt.status(today) == DebtStatus.PAID
                        DebtFilter.ALL -> true
                    }
                }
                .filter { q.isBlank() || it.debt.customerName.contains(q.trim(), ignoreCase = true) }

            DebtListUiState(
                isLoading = false,
                items = visible,
                hasAnyDebts = all.isNotEmpty(),
                totalOwed = all.sumOf { it.balance },
                overdueCount = all.count { it.status(today) == DebtStatus.OVERDUE },
                today = today,
                query = q,
                filter = f
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DebtListUiState())

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun onFilterChange(value: DebtFilter) {
        filter.value = value
    }

    companion object {
        fun factory(repository: DebtRepository) = viewModelFactory {
            initializer { DebtListViewModel(repository) }
        }
    }
}
