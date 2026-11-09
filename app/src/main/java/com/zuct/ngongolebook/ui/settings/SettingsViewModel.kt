package com.zuct.ngongolebook.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.zuct.ngongolebook.notifications.ReminderManager

class SettingsViewModel(private val reminders: ReminderManager) : ViewModel() {

    var remindersEnabled by mutableStateOf(reminders.isEnabled)
        private set

    fun updateRemindersEnabled(enabled: Boolean) {
        reminders.setEnabled(enabled)
        remindersEnabled = enabled
    }

    fun sendTestNotification() = reminders.sendTestNow()

    companion object {
        fun factory(reminders: ReminderManager) = viewModelFactory {
            initializer { SettingsViewModel(reminders) }
        }
    }
}
