package com.zuct.ngongolebook.ui.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.zuct.ngongolebook.notifications.NotificationHelper
import com.zuct.ngongolebook.notifications.ReminderManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var message by rememberSaveable { mutableStateOf<String?>(null) }

    // Android 13+ asks the user at runtime. Denying must be handled gracefully.
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            message = null
            viewModel.updateRemindersEnabled(true) // Fixed method name
        } else {
            viewModel.updateRemindersEnabled(false) // Fixed method name
            message = "Notifications are blocked, so reminders are off. " +
                    "You can allow them in your phone's Settings > Apps > Ngongole Book > Notifications."
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Daily overdue reminder", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "A notification at ${ReminderManager.REMINDER_HOUR}:00 every morning " +
                                "if any customer has passed their promised date.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Switch(
                    checked = viewModel.remindersEnabled,
                    onCheckedChange = { wantOn ->
                        when {
                            !wantOn -> {
                                message = null
                                viewModel.updateRemindersEnabled(false) // Fixed method name
                            }

                            NotificationHelper.canNotify(context) -> {
                                message = null
                                viewModel.updateRemindersEnabled(true) // Fixed method name
                            }

                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                    !NotificationHelper.hasRuntimePermission(context) ->
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)

                            else -> {
                                message = "Notifications are turned off for this app in your phone's settings."
                            }
                        }
                    }
                )
            }

            OutlinedButton(
                onClick = {
                    if (NotificationHelper.canNotify(context)) {
                        message = null
                        viewModel.sendTestNotification()
                    } else {
                        message = "Allow notifications first (turn the reminder on above)."
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Send a test notification now")
            }

            message?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Text(
                "Dark mode follows your phone's setting.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}