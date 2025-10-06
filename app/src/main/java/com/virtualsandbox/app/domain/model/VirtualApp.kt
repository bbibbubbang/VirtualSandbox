package com.virtualsandbox.app.domain.model

data class VirtualApp(
    val id: Long,
    val spaceId: Long,
    val packageName: String,
    val displayName: String,
    val iconUri: String?,
    val installTime: Long,
    val permissionSnapshot: List<String>,
)
