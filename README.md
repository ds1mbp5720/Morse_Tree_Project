# Morse Tree Decoder (모스 부호 디코더)

사용자가 버튼을 누르는 시간(단점 '.', 장점 '-')을 감지하여 실시간으로 문자를 해독하는 인터랙티브 애플리케이션입니다.

## 🚀 주요 기능
- **Dichotomous Tree 시각화**: 모스 부호의 이진 트리 구조를 시각적으로 보여주며 현재 입력 상태를 추적합니다.
- **실시간 디코딩**: 입력을 멈추면(1초 대기) 자동으로 가장 최근 노드의 문자를 메시지에 추가합니다.
- **오디오 및 진동 피드백**: 입력 시 실제 통신 장비와 같은 비프음과 햅틱 피드백을 제공합니다.
- **설정 메뉴**: 소리 및 진동 활성화 여부를 개별적으로 설정할 수 있습니다.

## 🛠 기술 스택
- **Web**: React 19, Vite, Tailwind CSS, Framer Motion (애니메이션)
- **Mobile**: Android Jetpack Compose, Kotlin, AudioTrack API (톤 생성)

## 📁 프로젝트 구조 (Android)
```
/android
├── MainActivity.kt          # 앱 진입점 및 테마 설정
├── ui/
│   ├── MorseScreen.kt       # 전체 화면 레이아웃 및 상태 관리
│   └── components/
│       ├── PulsePad.kt      # 메인 입력 버튼 (터치 감지)
│       └── MessageDisplay.kt # 결과 텍스트 출력 컴포넌트
└── audio/
    └── MorseToneGenerator.kt # 순수 사인파 기반 톤 발생 장치
```

## 📱 설치 및 실행 방법 (Android)
1. Android Studio를 실행합니다.
2. `Empty Compose Activity` 프로젝트를 생성합니다.
3. 패키지명을 `com.example.morsedecoder`로 설정합니다.
4. `/android` 폴더 내의 파일들을 해당하는 경로에 복사합니다.
5. `AndroidManifest.xml`에 진동 권한을 추가합니다:
   `<uses-permission android:name="android.permission.VIBRATE" />`
6. 실행 버튼을 누릅니다.
