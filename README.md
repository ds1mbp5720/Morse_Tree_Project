# Morse Labs - Signal Processor & Wifi Messenger

모스 부호를 직관적으로 시각화하고, 실시간으로 해독 및 전송하며, 로컬 네트워크(Wifi)를 통해 기기 간 메시지를 주고받을 수 있는 안드로이드 애플리케이션입니다.

## 📱 주요 기능

### 1. Morse Decoder (해독기)
- **실시간 트리 시각화**: 입력 중인 모스 부호 경로를 이진 트리 구조로 실시간 하이라이트하여 보여줍니다.
- **Pulse Pad**: 원형 패드를 터치하여 점(.)과 선(-)을 입력합니다. 누르는 길이에 따라 자동으로 구분됩니다.
- **실시간 스트림**: 입력된 부호가 즉시 텍스트로 변환되어 상단 스트림에 표시됩니다.
- **Playback**: 해독된 메시지를 다시 모스 부호 소리와 진동으로 출력해볼 수 있습니다.

### 2. Morse Encoder (생성기)
- **텍스트-모스 변환**: 일반 텍스트를 입력하면 즉시 모스 부호로 변환합니다.
- **현장감 있는 출력**: 변환된 부호를 소리와 진동으로 즉시 재생할 수 있습니다.

### 3. Wifi Chat (메신저)
- **P2P 통신**: 같은 와이파이 네트워크에 연결된 기기들을 자동으로 찾아 연결합니다. (NSD 기술 활용)
- **모스 페이로드**: 메시지 전송 시 실제 텍스트와 함께 모스 부호 데이터를 함께 패킷으로 실어 보냅니다.
- **실시간 채팅**: 별도의 서버 없이 기기 간 직접 소켓 통신을 통해 메시지를 주고받습니다.

## 🏗️ 프로젝트 구조 (Multi-Module Clean Architecture)

본 프로젝트는 유지보수와 확장성을 위해 **Gradle Multi-Module** 기반의 **Clean Architecture** 패턴을 적용했습니다.

### Module 구성
- **`:app`**: UI 및 프레젠테이션 레이어 (`MorseScreen`, `ChatViewModel`, `Compose UI`).
- **`:domain`**: 비즈니스 로직 및 모델 (`MorseMessage`, `ChatRepository` 인터페이스).
- **`:data`**: 데이터 소스 및 저장소 구현 (`WifiChatRepositoryImpl`, NSD 및 소켓 통신).

### 의존성 규칙
- `:app` -> `:data` & `:domain`
- `:data` -> `:domain`
- `:domain` 은 아무런 내부 모듈 의존성을 가지지 않는 순수 비즈니스 로직 레이어입니다.

## 🛠️ 기술 스택
- **Language**: Kotlin / TypeScript (Build Config)
- **UI**: Jetpack Compose (Material 3)
- **Network**: NSD (Network Service Discovery), Socket Programming
- **Audio/Vibe**: Android ToneGenerator & Vibrator Service
- **Build System**: Gradle 8.4+

## 🚀 시작하기
1. 같은 와이파이에 두 대 이상의 안드로이드 기기를 연결합니다.
2. 기기에서 "Wifi Chat" 탭으로 이동합니다.
3. 자동으로 주변 기기를 탐색하여 메시지를 주고받을 준비를 마칩니다.
