package com.zuct.ngongolebook.ui.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.zuct.ngongolebook.data.DebtRepository
import com.zuct.ngongolebook.data.DebtWithPaid
import com.zuct.ngongolebook.data.Payment
import com.zuct.ngongolebook.domain.Validators
import com.zuct.ngongolebook.domain.balance
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data object NotFound : DetailUiState
    data class Loaded(val debt: DebtWithPaid, val payments: List<Payment>) : DetailUiState
}

class DebtDetailViewModel(
    private val repository: DebtRepository,
    private val debtId: Long
) : ViewModel() {

    val state: StateFlow<DetailUiState> =
        combine(repository.debt(debtId), repository.payments(debtId)) { debt, payments ->
            if (debt == null) DetailUiState.NotFound else DetailUiState.Loaded(debt, payments)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetailUiState.Loading)

    var paymentText by mutableStateOf("")
        private set

    var paymentError by mutableStateOf<String?>(null)
        private set

    fun onPaymentText(value: String) {
        paymentText = value
        paymentError = null
    }

    fun fillFullBalance(balance: Double) {
        paymentText = String.format(java.util.Locale.US, "%.2f", balance)
        paymentError = null
    }

    fun recordPayment(balance: Double) {
        val error = Validators.payment(paymentText, balance)
        paymentError = error
        if (error != null) return
        val amount = Validators.parseAmount(paymentText) ?: return
        viewModelScope.launch {
            // Never store more than what is still owed.
            repository.addPayment(debtId, minOf(amount, balance))
            paymentText = ""
        }
    }

    fun delete(onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteDebt(debtId)
            onDeleted()
        }
    }

    companion object {
        fun factory(repository: DebtRepository, debtId: Long) = viewModelFactory {
            initializer { DebtDetailViewModel(repository, debtId) }
        }
    }
}
