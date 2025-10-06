package com.virtualsandbox.app.domain.usecase

import com.virtualsandbox.app.data.virtualization.VirtualEnvironmentController
import com.virtualsandbox.app.domain.model.SandboxSpace
import com.virtualsandbox.app.domain.model.VirtualApp
import javax.inject.Inject

class LaunchVirtualAppUseCase @Inject constructor(
    private val controller: VirtualEnvironmentController,
) {
    operator fun invoke(space: SandboxSpace, app: VirtualApp): Boolean {
        return controller.launchVirtualApp(space, app)
    }
}
