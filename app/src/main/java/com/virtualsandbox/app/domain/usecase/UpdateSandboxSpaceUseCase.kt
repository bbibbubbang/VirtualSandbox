package com.virtualsandbox.app.domain.usecase

import com.virtualsandbox.app.domain.model.SandboxSpace
import com.virtualsandbox.app.domain.repository.SandboxRepository
import javax.inject.Inject

class UpdateSandboxSpaceUseCase @Inject constructor(
    private val repository: SandboxRepository,
) {
    suspend operator fun invoke(space: SandboxSpace) {
        repository.updateSpace(space)
    }
}
