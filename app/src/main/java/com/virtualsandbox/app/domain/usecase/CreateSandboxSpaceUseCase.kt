package com.virtualsandbox.app.domain.usecase

import com.virtualsandbox.app.data.preferences.SandboxPreferencesRepository
import com.virtualsandbox.app.domain.model.SandboxProfile
import com.virtualsandbox.app.domain.model.SandboxSpace
import com.virtualsandbox.app.domain.repository.SandboxRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class CreateSandboxSpaceUseCase @Inject constructor(
    private val repository: SandboxRepository,
    private val preferencesRepository: SandboxPreferencesRepository,
) {
    suspend operator fun invoke(
        name: String,
        description: String,
        profile: SandboxProfile,
    ): Long {
        val defaults = preferencesRepository.preferences.first()
        val space = SandboxSpace(
            id = 0,
            name = name,
            description = description,
            storageRoot = "",
            profile = profile,
            networkIsolation = defaults.defaultNetworkIsolation,
            lastLaunchedAt = null,
            createdAt = System.currentTimeMillis(),
        )
        return repository.createSpace(space)
    }
}
