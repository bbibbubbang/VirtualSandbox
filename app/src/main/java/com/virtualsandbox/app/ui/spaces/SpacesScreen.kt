package com.virtualsandbox.app.ui.spaces

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.virtualsandbox.app.R
import com.virtualsandbox.app.domain.model.SandboxProfile
import com.virtualsandbox.app.domain.model.SandboxSpace
import com.virtualsandbox.app.ui.components.CreateSpaceDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SpacesRoute(
    onSpaceSelected: (Long) -> Unit,
    viewModel: SpacesViewModel = hiltViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val showDialog = remember { mutableStateOf(false) }

    LaunchedEffect(state.value.error) {
        state.value.error?.let { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    SpacesScreen(
        state = state.value,
        snackbarHostState = snackbarHostState,
        showDialogState = showDialog,
        onCreateSpace = { name, description, profile ->
            viewModel.createSpace(name, description, profile)
        },
        onSpaceSelected = onSpaceSelected,
    )
}

@Composable
fun SpacesScreen(
    state: SpacesUiState,
    snackbarHostState: SnackbarHostState,
    showDialogState: MutableState<Boolean>,
    onCreateSpace: (String, String, SandboxProfile) -> Unit,
    onSpaceSelected: (Long) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialogState.value = true }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { padding ->
        Surface(modifier = Modifier.padding(padding)) {
            when {
                state.spaces.isEmpty() -> EmptyState(state)
                else -> SpacesList(
                    spaces = state.spaces,
                    onSpaceSelected = onSpaceSelected,
                )
            }
        }
    }

    if (showDialogState.value) {
        CreateSpaceDialog(
            profiles = SandboxProfile.entries,
            onDismiss = { showDialogState.value = false },
            onCreate = { name, description, profile ->
                onCreateSpace(name, description, profile)
                showDialogState.value = false
            },
        )
    }
}

@Composable
private fun EmptyState(state: SpacesUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = stringResource(id = R.string.spaces_empty_title), style = MaterialTheme.typography.titleLarge)
        Text(
            text = stringResource(id = R.string.spaces_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 16.dp),
        )
        if (!state.virtualizationSupported) {
            Text(
                text = stringResource(id = R.string.spaces_virtualization_not_supported),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 24.dp),
            )
        } else if (!state.virtualizationEnabled) {
            Text(
                text = stringResource(id = R.string.spaces_virtualization_disabled),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 24.dp),
            )
        }
    }
}

@Composable
private fun SpacesList(
    spaces: List<SandboxSpace>,
    onSpaceSelected: (Long) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(spaces, key = { it.id }) { space ->
            SpaceCard(space = space, onClick = { onSpaceSelected(space.id) })
        }
    }
}

@Composable
private fun SpaceCard(
    space: SandboxSpace,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = space.name, style = MaterialTheme.typography.titleMedium)
            Text(text = space.description, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = stringResource(id = R.string.space_card_apps_count, space.installedApps.size),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                text = stringResource(
                    id = R.string.space_card_created_at,
                    formatDate(space.createdAt),
                ),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val date = Date(timestamp)
    val formatter = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
    return formatter.format(date)
}
