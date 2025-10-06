package com.virtualsandbox.app.ui.detail

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.virtualsandbox.app.core.SandboxFileManager
import com.virtualsandbox.app.data.virtualization.VirtualEnvironmentController
import com.virtualsandbox.app.domain.model.SandboxProfile
import com.virtualsandbox.app.domain.model.SandboxSpace
import com.virtualsandbox.app.domain.model.VirtualApp
import com.virtualsandbox.app.domain.repository.SandboxRepository
import com.virtualsandbox.app.domain.usecase.DeleteSandboxSpaceUseCase
import com.virtualsandbox.app.domain.usecase.LaunchVirtualAppUseCase
import com.virtualsandbox.app.domain.usecase.RegisterVirtualAppUseCase
import com.virtualsandbox.app.domain.usecase.RemoveVirtualAppUseCase
import com.virtualsandbox.app.domain.usecase.UpdateSandboxSpaceUseCase
import com.virtualsandbox.app.ui.navigation.SandboxDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SpaceDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: SandboxRepository,
    private val updateSandboxSpaceUseCase: UpdateSandboxSpaceUseCase,
    private val registerVirtualAppUseCase: RegisterVirtualAppUseCase,
    private val removeVirtualAppUseCase: RemoveVirtualAppUseCase,
    private val deleteSandboxSpaceUseCase: DeleteSandboxSpaceUseCase,
    private val launchVirtualAppUseCase: LaunchVirtualAppUseCase,
    private val controller: VirtualEnvironmentController,
    private val fileManager: SandboxFileManager,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val spaceId: Long = savedStateHandle.get<Long>(SandboxDestination.SpaceDetail.ARG_SPACE_ID)
        ?: savedStateHandle.get<String>(SandboxDestination.SpaceDetail.ARG_SPACE_ID)?.toLong()
        ?: error("spaceId is required")

    private val _state = MutableStateFlow(SpaceDetailUiState())
    val state: StateFlow<SpaceDetailUiState> = _state.asStateFlow()

    init {
        observeSpace()
        loadInstalledApps()
    }

    private fun observeSpace() {
        viewModelScope.launch {
            repository.observeSpace(spaceId)
                .filterNotNull()
                .collect { space ->
                    _state.update {
                        it.copy(
                            space = space,
                            virtualizationSupported = controller.isVirtualizationSupported(),
                            launchSupported = controller.hasLaunchCapability(),
                            storageUsage = fileManager.calculateDirectorySize(space.storageRoot),
                        )
                    }
                }
        }
    }

    private fun loadInstalledApps() {
        viewModelScope.launch {
            val pm = context.packageManager
            val applications = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getInstalledApplications(0)
            }
            val packages = applications
                .filter { it.packageName != context.packageName }
                .map { appInfo ->
                    val label = appInfo.loadLabel(pm).toString()
                    InstalledAppInfo(
                        packageName = appInfo.packageName,
                        displayName = label,
                        permissions = getPermissions(pm, appInfo.packageName),
                        iconUri = null,
                    )
                }
                .sortedBy { it.displayName }
            _state.update { it.copy(availableApps = packages) }
        }
    }

    private fun getPermissions(pm: PackageManager, packageName: String): List<String> {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val info = pm.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong()),
                )
                info.requestedPermissions?.toList() ?: emptyList()
            } else {
                @Suppress("DEPRECATION")
                val info = pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
                info.requestedPermissions?.toList() ?: emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun toggleNetworkIsolation(enabled: Boolean) {
        val space = _state.value.space ?: return
        viewModelScope.launch {
            updateSandboxSpaceUseCase(space.copy(networkIsolation = enabled))
        }
    }

    fun updateProfile(profile: SandboxProfile) {
        val space = _state.value.space ?: return
        viewModelScope.launch {
            updateSandboxSpaceUseCase(space.copy(profile = profile))
        }
    }

    fun addVirtualApp(app: InstalledAppInfo) {
        val space = _state.value.space ?: return
        viewModelScope.launch {
            val virtualApp = VirtualApp(
                id = 0,
                spaceId = space.id,
                packageName = app.packageName,
                displayName = app.displayName,
                iconUri = app.iconUri,
                installTime = System.currentTimeMillis(),
                permissionSnapshot = app.permissions,
            )
            registerVirtualAppUseCase(space.id, virtualApp)
        }
    }

    fun removeVirtualApp(appId: Long) {
        viewModelScope.launch {
            removeVirtualAppUseCase(appId)
        }
    }

    fun deleteSpace(onDeleted: () -> Unit) {
        viewModelScope.launch {
            deleteSandboxSpaceUseCase(spaceId)
            onDeleted()
        }
    }

    fun launchApp(appId: Long) {
        val space = _state.value.space ?: return
        val app = space.installedApps.firstOrNull { it.id == appId } ?: return
        val launched = launchVirtualAppUseCase(space, app)
        if (launched) {
            viewModelScope.launch {
                updateSandboxSpaceUseCase(space.copy(lastLaunchedAt = System.currentTimeMillis()))
            }
        } else {
            _state.update { it.copy(errorMessage = context.getString(com.virtualsandbox.app.R.string.space_detail_launch_failed)) }
        }
    }

    fun consumeError() {
        _state.update { it.copy(errorMessage = null) }
    }
}

data class SpaceDetailUiState(
    val space: SandboxSpace? = null,
    val availableApps: List<InstalledAppInfo> = emptyList(),
    val virtualizationSupported: Boolean = false,
    val launchSupported: Boolean = true,
    val storageUsage: Long = 0,
    val errorMessage: String? = null,
)

data class InstalledAppInfo(
    val packageName: String,
    val displayName: String,
    val permissions: List<String>,
    val iconUri: String?,
)
