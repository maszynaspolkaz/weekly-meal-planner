package com.weeklymeal.planner.core.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.weeklymeal.planner.R

@Composable
fun ConfirmationDialog(titleRes: Int, message: String, confirmRes: Int, onConfirm: () -> Unit, onDismiss: () -> Unit, dismissRes: Int = R.string.cancel) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(titleRes)) }, text = { Column(Modifier.verticalScroll(rememberScrollState())) { Text(message) } },
        confirmButton = { ActionText(confirmRes, onConfirm) }, dismissButton = { ActionText(dismissRes, onDismiss) })
}
