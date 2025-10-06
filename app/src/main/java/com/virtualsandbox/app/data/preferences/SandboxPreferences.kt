package com.virtualsandbox.app.data.preferences

data class SandboxPreferences(
    val virtualizationEnabled: Boolean = true,
    val biometricUnlockEnabled: Boolean = false,
    val defaultNetworkIsolation: Boolean = true,
)
