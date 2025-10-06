package com.virtualsandbox.app.ui.spaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.virtualsandbox.app.data.preferences.SandboxPreferencesRepository
import com.virtualsandbox.app.data.virtualization.VirtualEnvironmentController
import com.virtualsandbox.app.domain.model.SandboxProfile
import com.virtualsandbox.app.domain.model.SandboxSpace
import com.virtualsandbox.app.domain.repository.SandboxRepository
import com.virtualsandbox.app.domain.usecase.CreateSandboxSpaceUseCase
import com.virtualsandbox.app.domain.usecase.DeleteSandboxSpaceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SpacesViewModel @Inject constructor(
    private val repository: SandboxRepository,
    private val createSandboxSpaceUseCase: CreateSandboxSpaceUseCase,
    private val deleteSandboxSpaceUseCase: DeleteSandboxSpaceUseCase,
    private val preferencesRepository: SandboxPreferencesRepository,
    private val virtualEnvironmentController: VirtualEnvironmentController,
) : ViewModel() {

    private val _state = MutableStateFlow(SpacesUiState())
    val state: StateFlow<SpacesUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.observeSpaces(),
                preferencesRepository.preferences,
            ) { spaces, prefs ->
                { current: SpacesUiState ->
                    current.copy(
                        spaces = spaces,
                        virtualizationEnabled = prefs.virtualizationEnabled,
                        virtualizationSupported = virtualEnvironmentController.isVirtualizationSupported(),
                    )
                }
            }.collect { transform ->
                _state.update(transform)
            }
        }
    }

    fun createSpace(name: String, description: String, profile: SandboxProfile) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                createSandboxSpaceUseCase(name, description, profile)
            } catch (t: Throwable) {
                _state.update { it.copy(error = t.message) }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun deleteSpace(spaceId: Long) {
        viewModelScope.launch {
            try {
                deleteSandboxSpaceUseCase(spaceId)
            } catch (t: Throwable) {
                _state.update { it.copy(error = t.message) }
            }
        }
    }
}

data class SpacesUiState(
    val spaces: List<SandboxSpace> = emptyList(),
    val virtualizationEnabled: Boolean = true,
    val virtualizationSupported: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
)
