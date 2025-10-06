package com.virtualsandbox.app.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.virtualsandbox.app.R
import com.virtualsandbox.app.domain.model.SandboxProfile
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SpaceDetailRoute(
    onBack: () -> Unit,
    viewModel: SpaceDetailViewModel = hiltViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val showAddAppDialog = remember { mutableStateOf(false) }
    val showDeleteDialog = remember { mutableStateOf(false) }

    LaunchedEffect(state.value.errorMessage) {
        state.value.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.consumeError()
        }
    }

    SpaceDetailScreen(
        state = state.value,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onToggleNetwork = viewModel::toggleNetworkIsolation,
        onProfileSelected = viewModel::updateProfile,
        onLaunchApp = viewModel::launchApp,
        onRemoveApp = viewModel::removeVirtualApp,
        onAddAppClick = { showAddAppDialog.value = true },
        onDeleteSpace = {
            showDeleteDialog.value = false
            viewModel.deleteSpace(onBack)
        },
        showAddAppDialog = showAddAppDialog,
        showDeleteDialog = showDeleteDialog,
        onAddApp = viewModel::addVirtualApp,
    )
}

@Composable
fun SpaceDetailScreen(
    state: SpaceDetailUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onToggleNetwork: (Boolean) -> Unit,
    onProfileSelected: (SandboxProfile) -> Unit,
    onLaunchApp: (Long) -> Unit,
    onRemoveApp: (Long) -> Unit,
    onAddAppClick: () -> Unit,
    onDeleteSpace: () -> Unit,
    showAddAppDialog: MutableState<Boolean>,
    showDeleteDialog: MutableState<Boolean>,
    onAddApp: (InstalledAppInfo) -> Unit,
) {
    val space = state.space
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = space?.name ?: stringResource(id = R.string.app_name)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog.value = true }, enabled = space != null) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { padding ->
        if (space == null) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(text = stringResource(id = R.string.spaces_empty_title))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item { SummaryCard(space, state.storageUsage) }
                item { NetworkSection(space, onToggleNetwork) }
                item { ProfileSection(space, onProfileSelected) }
                item {
                    AppsSection(
                        space = space,
                        virtualizationSupported = state.virtualizationSupported,
                        onLaunchApp = onLaunchApp,
                        onRemoveApp = onRemoveApp,
                        onAddAppClick = onAddAppClick,
                    )
                }
            }
        }
    }

    if (showAddAppDialog.value) {
        AddAppDialog(
            apps = state.availableApps,
            onDismiss = { showAddAppDialog.value = false },
            onSelect = {
                onAddApp(it)
                showAddAppDialog.value = false
            },
        )
    }

    if (showDeleteDialog.value) {
        ConfirmDeleteDialog(
            onDismiss = { showDeleteDialog.value = false },
            onConfirm = onDeleteSpace,
        )
    }
}

@Composable
private fun SummaryCard(space: SandboxSpace, storageUsage: Long) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = space.name, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            Text(text = space.description, style = MaterialTheme.typography.bodyMedium)
            space.lastLaunchedAt?.let { timestamp ->
                Text(
                    text = stringResource(id = R.string.space_detail_last_launched, formatDate(timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            Text(
                text = stringResource(id = R.string.space_detail_storage) + ": " + formatStorage(storageUsage),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun NetworkSection(space: SandboxSpace, onToggleNetwork: (Boolean) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = stringResource(id = R.string.space_detail_network_isolation), style = MaterialTheme.typography.titleMedium)
                    Text(text = stringResource(id = R.string.space_detail_network_isolation_summary), style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = space.networkIsolation, onCheckedChange = onToggleNetwork)
            }
        }
    }
}

@Composable
private fun ProfileSection(space: SandboxSpace, onProfileSelected: (SandboxProfile) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = stringResource(id = R.string.space_detail_profile), style = MaterialTheme.typography.titleMedium)
            Divider(modifier = Modifier.padding(vertical = 12.dp))
            SandboxProfile.entries.forEach { profile ->
                ListItem(
                    headlineContent = { Text(profile.label) },
                    supportingContent = { Text(profile.description) },
                    leadingContent = { Icon(imageVector = Icons.Default.Security, contentDescription = null) },
                    trailingContent = {
                        if (profile == space.profile) {
                            Text(text = "●", color = MaterialTheme.colorScheme.primary)
                        } else {
                            TextButton(onClick = { onProfileSelected(profile) }) {
                                Text(text = stringResource(id = R.string.space_detail_select_profile))
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun AppsSection(
    space: SandboxSpace,
    virtualizationSupported: Boolean,
    onLaunchApp: (Long) -> Unit,
    onRemoveApp: (Long) -> Unit,
    onAddAppClick: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = stringResource(id = R.string.space_detail_launch), style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = onAddAppClick) {
                    Text(text = stringResource(id = R.string.space_detail_add_app))
                }
            }
            if (space.installedApps.isEmpty()) {
                Text(text = stringResource(id = R.string.space_detail_no_apps), style = MaterialTheme.typography.bodyMedium)
            } else {
                space.installedApps.forEach { app ->
                    AppRow(
                        appName = app.displayName,
                        packageName = app.packageName,
                        onLaunch = { onLaunchApp(app.id) },
                        onRemove = { onRemoveApp(app.id) },
                        enabled = virtualizationSupported,
                    )
                }
            }
            if (!virtualizationSupported) {
                Text(
                    text = stringResource(id = R.string.space_detail_virtualization_not_supported),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
        }
    }
}

@Composable
private fun AppRow(
    appName: String,
    packageName: String,
    onLaunch: () -> Unit,
    onRemove: () -> Unit,
    enabled: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = appName, style = MaterialTheme.typography.bodyLarge)
            Text(text = packageName, style = MaterialTheme.typography.bodySmall)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilledIconButton(onClick = onLaunch, enabled = enabled) {
                Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null)
            }
            FilledIconButton(onClick = onRemove) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = null)
            }
        }
    }
}

@Composable
private fun AddAppDialog(
    apps: List<InstalledAppInfo>,
    onDismiss: () -> Unit,
    onSelect: (InstalledAppInfo) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.space_detail_add_app)) },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(8.dp)) {
                items(apps) { app ->
                    ListItem(
                        headlineContent = { Text(app.displayName) },
                        supportingContent = { Text(app.packageName) },
                        modifier = Modifier.padding(vertical = 4.dp),
                        trailingContent = {
                            TextButton(onClick = { onSelect(app) }) {
                                Text(text = stringResource(id = R.string.space_detail_add_to_space))
                            }
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = android.R.string.cancel))
            }
        },
    )
}

@Composable
private fun ConfirmDeleteDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.space_detail_delete)) },
        text = { Text(text = stringResource(id = R.string.space_detail_delete_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(id = R.string.space_detail_delete_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = stringResource(id = android.R.string.cancel)) }
        }
    )
}

private fun formatDate(timestamp: Long): String {
    val date = Date(timestamp)
    val formatter = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
    return formatter.format(date)
}

private fun formatStorage(bytes: Long): String {
    val mb = bytes / 1024f / 1024f
    val formatter = DecimalFormat("#,##0.00")
    return formatter.format(mb) + " MB"
}
