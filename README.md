# Hermes Agent Android 🚀

A native Android client for [Hermes Agent](https://github.com/1panel/hermes-agent) — your AI agent mobile companion.

![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android)
![Language](https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin)
![UI](https://img.shields.io/badge/UI-Jetpack_Compose-4285F4?logo=jetpackcompose)
![Min SDK](https://img.shields.io/badge/minSdk-26-brightgreen)
![License](https://img.shields.io/badge/License-MIT-blue)
![Build](https://img.shields.io/badge/Build-GitHub_Actions-2088FF?logo=githubactions)

---

## ✨ Features

- **💬 Chat with your AI Agent** — Real-time messaging with Hermes Agent
- **🔐 Authentication** — Basic auth & token-based login support
- **📜 Conversation History** — Browse, revisit, and manage past conversations
- **⚙️ Configurable** — Server URL, model selection, dark mode, and more
- **🎨 Material You** — Modern Material 3 design with dynamic theming
- **🌙 Dark Mode** — System, light, or dark theme options
- **📡 Local Network Ready** — Pre-configured for LAN servers (192.168.x.x, 10.x.x.x, etc.)

## 📱 Screenshots

| Chat | Conversations | Settings | Login |
|------|--------------|----------|-------|
| *(coming soon)* | *(coming soon)* | *(coming soon)* | *(coming soon)* |

## 🛠️ Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | **Kotlin** |
| UI | **Jetpack Compose** + Material 3 |
| Architecture | **MVVM** (ViewModel + Repository) |
| Navigation | **Navigation Compose** |
| Networking | **Retrofit2** + **OkHttp** |
| Persistence | **DataStore Preferences** |
| Async | **Kotlin Coroutines** + **Flow** |
| Build | **Gradle Kotlin DSL** |

## 🚀 Getting Started

### 方式一：GitHub Actions（推荐 — 无需本地环境！）

直接用 GitHub 在线构建 APK，不需要装任何东西 👇

#### 1. 推送代码到 GitHub

```bash
# 在 GitHub 上新建一个仓库（例如 HermesAgent-Android）
# 然后在本地执行：
cd hermes-agent-android

git init
git add .
git commit -m "Initial commit: Hermes Agent Android app"

git remote add origin https://github.com/你的用户名/HermesAgent-Android.git
git branch -M main
git push -u origin main
```

#### 2. 触发构建

代码推送到 GitHub 后，进入仓库页面：

- **自动构建：** 每次 push 到 `main` 分支，Actions 自动跑
- **手动构建：** 点 **Actions** → **Build Hermes Agent APK** → **Run workflow** → 选择 `debug` 或 `release`

#### 3. 下载 APK

构建完成后，在 Action 运行结果页找到 **Artifacts** 区域 ⬇️

- 点击 `hermes-agent-debug-xxxxx` 下载 APK
- 传到手机直接安装即可！

> 💡 不需要 Android Studio，不需要 JDK，不需要 Android SDK — 全在 GitHub 服务器上完成。

---

### 方式二：本地 Android Studio 构建

### Prerequisites

- [Android Studio](https://developer.android.com/studio) Hedgehog (2023.1.1+) or later
- JDK 17+
- Android SDK 34

### Build & Run

```bash
cd hermes-agent-android

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug
```

Or simply open the `hermes-agent-android` folder in **Android Studio** and click **Run ▶️**.

### First Use

1. Open the app
2. Enter your Hermes Agent server URL (e.g. `http://192.168.50.196:9119`)
3. Enter your username and password
4. Tap **Connect**
5. Start chatting! 🎉

## 🔧 Configuration

### Default Settings

| Setting | Default Value |
|---------|--------------|
| Server URL | `http://192.168.50.196:9119` |
| Username | `admin` |
| Model | `deepseek-ai/DeepSeek-V3.2` |
| Theme | Follow System |

### Network Security

The app allows cleartext (HTTP) traffic to local network IP ranges:
- `192.168.0.0/16`
- `10.0.0.0/8`
- `172.16.0.0/12`
- `localhost`

This is configured in `app/src/main/res/xml/network_security_config.xml`.

## 📁 Project Structure

```
hermes-agent-android/
├── app/
│   ├── src/main/
│   │   ├── java/com/hermes/agent/
│   │   │   ├── HermesAgentApp.kt          # Application class
│   │   │   ├── MainActivity.kt            # Main entry point
│   │   │   ├── ui/
│   │   │   │   ├── theme/                 # Material 3 theme (Color, Type, Theme)
│   │   │   │   ├── navigation/            # NavGraph + bottom nav
│   │   │   │   ├── screens/               # Login, Chat, Conversations, Settings
│   │   │   │   └── components/            # MessageBubble, ChatInput, ConversationItem
│   │   │   ├── network/                   # Retrofit API service + config
│   │   │   ├── data/
│   │   │   │   ├── model/                 # Data classes (Message, Conversation, etc.)
│   │   │   │   └── repository/            # ChatRepository, SettingsRepository
│   │   │   └── viewmodel/                 # HermesViewModel
│   │   └── res/                           # Resources (strings, colors, themes, icons)
│   └── build.gradle.kts                   # App-level build config
├── build.gradle.kts                       # Project-level build config
├── settings.gradle.kts
├── gradle.properties
└── README.md
```

## 📋 API Endpoints Used

The app communicates with the Hermes Agent REST API:

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/health` | Server health check |
| POST | `/api/auth/login` | User authentication |
| POST | `/api/auth/verify` | Token verification |
| GET | `/api/conversations` | List conversations |
| GET | `/api/conversations/{id}` | Get conversation |
| DELETE | `/api/conversations/{id}` | Delete conversation |
| POST | `/api/chat` | Send chat message |
| POST | `/api/chat/stream` | Stream chat response |
| GET | `/api/models` | List available models |

## 🤝 Contributing

Contributions welcome! Feel free to open issues or submit PRs.

## 📄 License

MIT License — see [LICENSE](LICENSE) for details.

---

*Built with ❤️ for the Hermes Agent ecosystem*
