package com.virtualsandbox.app.domain.model

data class SandboxSpace(
    val id: Long,
    val name: String,
    val description: String,
    val storageRoot: String,
    val profile: SandboxProfile,
    val networkIsolation: Boolean,
    val lastLaunchedAt: Long?,
    val createdAt: Long,
    val installedApps: List<VirtualApp> = emptyList(),
)

val SandboxSpace.isSecure: Boolean
    get() = profile == SandboxProfile.SECURE || networkIsolation
