package com.zuct.ngongolebook.ui.add

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.zuct.ngongolebook.data.DebtRepository
import com.zuct.ngongolebook.domain.Formatters
import com.zuct.ngongolebook.domain.Validators
import kotlinx.coroutines.launch

data class AddDebtForm(
    val name: String = "",
    val phone: String = "",
    val item: String = "",
    val amount: String = "",
    val dueDate: Long? = null
)

data class AddDebtErrors(
    val name: String? = null,
    val phone: String? = null,
    val item: String? = null,
    val amount: String? = null,
    val dueDate: String? = null
) {
    val hasAny: Boolean get() = name != null || phone != null || item != null || amount != null || dueDate != null
}

/**
 * Form state lives here, not in the composable, so rotating the phone
 * (which recreates the Activity) does not wipe what the user has typed.
 */
class AddDebtViewModel(private val repository: DebtRepository) : ViewModel() {

    var form by mutableStateOf(AddDebtForm())
        private set

    var errors by mutableStateOf(AddDebtErrors())
        private set

    var isSaving by mutableStateOf(false)
        private set

    fun onName(value: String) {
        form = form.copy(name = value)
        errors = errors.copy(name = null)
    }

    fun onPhone(value: String) {
        form = form.copy(phone = value)
        errors = errors.copy(phone = null)
    }

    fun onItem(value: String) {
        form = form.copy(item = value)
        errors = errors.copy(item = null)
    }

    fun onAmount(value: String) {
        form = form.copy(amount = value)
        errors = errors.copy(amount = null)
    }

    fun onDueDate(millis: Long?) {
        form = form.copy(dueDate = millis)
        errors = errors.copy(dueDate = null)
    }

    /** Validates every field; only saves (and calls [onSaved]) when all are fine. */
    fun save(onSaved: () -> Unit) {
        if (isSaving) return
        val today = Formatters.todayUtcMidnight()
        val found = AddDebtErrors(
            name = Validators.name(form.name),
            phone = Validators.phone(form.phone),
            item = Validators.item(form.item),
            amount = Validators.amount(form.amount),
            dueDate = Validators.dueDate(form.dueDate, today)
        )
        errors = found
        if (found.hasAny) return

        val amount = Validators.parseAmount(form.amount) ?: return
        val dueDate = form.dueDate ?: return
        viewModelScope.launch {
            isSaving = true
            repository.addDebt(form.name, form.phone, form.item, amount, dueDate)
            isSaving = false
            onSaved()
        }
    }

    companion object {
        fun factory(repository: DebtRepository) = viewModelFactory {
            initializer { AddDebtViewModel(repository) }
        }
    }
}
