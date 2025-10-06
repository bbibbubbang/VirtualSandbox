package com.virtualsandbox.app.domain.usecase

import com.virtualsandbox.app.domain.model.VirtualApp
import com.virtualsandbox.app.domain.repository.SandboxRepository
import javax.inject.Inject

class RegisterVirtualAppUseCase @Inject constructor(
    private val repository: SandboxRepository,
) {
    suspend operator fun invoke(spaceId: Long, app: VirtualApp): Long {
        return repository.addVirtualApp(spaceId, app)
    }
}
