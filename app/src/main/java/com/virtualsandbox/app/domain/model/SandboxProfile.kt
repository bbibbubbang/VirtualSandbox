package com.virtualsandbox.app.domain.model

enum class SandboxProfile(val label: String, val description: String) {
    STANDARD(
        label = "표준 격리",
        description = "앱 데이터를 별도의 저장소에 보관하고 최소한의 리소스를 격리합니다.",
    ),
    SECURE(
        label = "고급 보안",
        description = "네트워크 접근을 기본적으로 차단하고, 민감한 데이터를 암호화합니다.",
    ),
    PERFORMANCE(
        label = "고성능",
        description = "그래픽 및 멀티미디어 처리를 최적화하여 가상 앱 실행 성능을 향상합니다.",
    );

    companion object {
        fun fromKey(key: String): SandboxProfile = entries.firstOrNull { it.name == key } ?: STANDARD
    }
}
