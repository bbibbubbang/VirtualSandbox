package com.virtualsandbox.app.domain.usecase

import com.virtualsandbox.app.domain.repository.SandboxRepository
import javax.inject.Inject

class RemoveVirtualAppUseCase @Inject constructor(
    private val repository: SandboxRepository,
) {
    suspend operator fun invoke(appId: Long) {
        repository.removeVirtualApp(appId)
    }
}
