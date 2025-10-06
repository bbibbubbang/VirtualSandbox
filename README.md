# VirtualSandbox

VirtualSandbox는 Android 15(바닐라 아이스크림) 이상에서 실행되는 가상 샌드박스 환경 앱 예제 프로젝트입니다. 앱 내에서 격리된 공간을 만들고, 가상의 앱을 설치하거나 실행하는 흐름을 통해 Android Virtualization Framework을 활용한 보안/격리 시나리오를 설계할 수 있습니다.

## 주요 기능

- **샌드박스 공간 관리**: 별도의 저장소를 가지는 가상 공간을 생성/삭제하고, 네트워크 차단 및 격리 프로필을 관리합니다.
- **가상 앱 설치 및 실행 흐름**: 실제 단말에 설치된 앱을 선택하여 샌드박스에 등록하고, Android 15의 가상화 프레임워크를 이용해 실행을 시도합니다.
- **보안 옵션**: 기본 네트워크 차단, 생체 인증 요구 등 전역 보안 옵션을 DataStore에 저장하여 제어합니다.
- **Jetpack Compose UI**: Material 3 기반의 최신 Compose UI와 Hilt, Room, DataStore, Navigation을 사용한 모던 아키텍처 예제입니다.

## 프로젝트 구조

```
VirtualSandbox/
├── app/                     # Android 애플리케이션 모듈
│   ├── build.gradle.kts     # 앱 모듈 빌드 스크립트
│   └── src/main/java/...    # Kotlin 소스 코드, Compose UI, ViewModel, DI 등
├── build.gradle.kts         # 루트 빌드 스크립트
├── settings.gradle.kts      # Gradle 설정
├── gradle/                  # Gradle Wrapper
└── README.md
```

## 개발 환경

- Android Studio Iguana 이상 권장
- Android Gradle Plugin 8.5.0, Kotlin 1.9.24, Compose BOM 2024.05.00
- 최소 SDK 26, 타깃 SDK 35

## 빌드 방법

1. 최초 클론 이후 Gradle Wrapper JAR가 없는 환경에서는 다음 스크립트로 다운로드합니다.

   ```bash
   ./scripts/bootstrap_gradle_wrapper.sh
   ```

2. 이후 일반적인 Gradle 명령을 실행합니다.

   ```bash
   ./gradlew assembleDebug
   ```

## 참고

- 실제 가상화 실행은 Android 15 (Vanilla Ice Cream) 이상의 기기에서 `PackageManager.FEATURE_VIRTUALIZATION_FRAMEWORK`를 지원해야 동작합니다.
- 예제에서는 Room, DataStore 등을 활용하여 기본적인 데이터 흐름을 구성했으며, 필요에 따라 실 서비스에 맞게 확장하세요.
- 저장소 정책상 `gradle/wrapper/gradle-wrapper.jar`는 포함하지 않으며, `scripts/bootstrap_gradle_wrapper.sh` 스크립트가 동일한 버전을 자동으로 내려받습니다.
