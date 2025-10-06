package com.virtualsandbox.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.virtualsandbox.app.R
import com.virtualsandbox.app.domain.model.SandboxProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSpaceDialog(
    profiles: List<SandboxProfile>,
    onDismiss: () -> Unit,
    onCreate: (String, String, SandboxProfile) -> Unit,
) {
    val (name, setName) = remember { mutableStateOf("") }
    val (description, setDescription) = remember { mutableStateOf("") }
    val (profile, setProfile) = remember { mutableStateOf(SandboxProfile.STANDARD) }
    val (expanded, setExpanded) = remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.dialog_create_space_title)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TextField(
                    value = name,
                    onValueChange = setName,
                    label = { Text(text = stringResource(id = R.string.dialog_create_space_name)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                )
                TextField(
                    value = description,
                    onValueChange = setDescription,
                    label = { Text(text = stringResource(id = R.string.dialog_create_space_description)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                )
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = setExpanded,
                ) {
                    TextField(
                        value = profile.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(id = R.string.dialog_create_space_profile)) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { setExpanded(false) },
                    ) {
                        profiles.forEach { candidate ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(candidate.label)
                                        Text(candidate.description)
                                    }
                                },
                                onClick = {
                                    setProfile(candidate)
                                    setExpanded(false)
                                },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onCreate(name, description, profile)
                    onDismiss()
                },
                enabled = name.isNotBlank(),
            ) {
                Text(text = stringResource(id = R.string.dialog_create_space_create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = android.R.string.cancel))
            }
        },
    )
}
