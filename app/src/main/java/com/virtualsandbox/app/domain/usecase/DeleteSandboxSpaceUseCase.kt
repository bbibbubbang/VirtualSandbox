package com.virtualsandbox.app.domain.usecase

import com.virtualsandbox.app.domain.repository.SandboxRepository
import javax.inject.Inject

class DeleteSandboxSpaceUseCase @Inject constructor(
    private val repository: SandboxRepository,
) {
    suspend operator fun invoke(spaceId: Long) {
        repository.deleteSpace(spaceId)
    }
}
