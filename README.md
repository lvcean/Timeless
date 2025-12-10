# ⏳ Timeless - 捕捉每一个永恒瞬间

> **"不仅仅是记录，更是对生活的致敬。"**

<p align="center">
  <img src="https://img.shields.io/badge/Android-Jetpack%20Compose-success?style=for-the-badge&logo=android" />
  <img src="https://img.shields.io/badge/Backend-Supabase-green?style=for-the-badge&logo=supabase" />
  <img src="https://img.shields.io/badge/Language-Kotlin-purple?style=for-the-badge&logo=kotlin" />
  <img src="https://img.shields.io/badge/Design-Material%203-blue?style=for-the-badge&logo=materialdesign" />
</p>

## 📱 项目简介 (Introduction)

**Timeless** 是一款极简主义的习惯追踪与事件记录应用。它不仅拥有精致的 Material Design 3 界面，更拥有强大的 **云端同步核心**。

不同于市面上只能本地记录的 Demo，本项目实现了完整的 **Android + Web 多端实时互通**。无论你在手机 App 还是网页端创建事件，数据都会通过 **Supabase (PostgreSQL)** 毫秒级同步，并受到企业级 RLS (Row Level Security) 的安全保护。

## ✨ 核心亮点 (Key Features)

### 1. ☁️ 真·云端实时同步 (Real-time Cloud Sync)
*   告别本地数据孤岛！集成 **Supabase** 后端服务。
*   **双向同步**：Android 端的新增、修改、删除操作，瞬间同步至云端数据库。
*   **多端一致**：Web 端与 App 端共享同一套数据源，无缝切换设备。

### 2. 🔐 企业级安全认证 (Secure Auth)
*   **邮箱验证登录**：集成 Supabase Auth (GoTrue)，支持 OTP 验证码登录，安全无忧。
*   **数据隔离**：数据库启用 **RLS (行级安全策略)**，确保用户只能访问自己的私有数据，隐私绝对安全。
*   **游客模式**：提供免登录体验入口，利用 Room 本地数据库实现离线功能。

### 3. 🎨 极致的视觉体验 (Premium UI/UX)
*   **Mint Green 主题**：全应用采用定制的“薄荷绿”配色体系，清新治愈。
*   **Jetpack Compose**：使用最先进的声明式 UI 框架构建，动画流畅丝滑。
*   **沉浸式 Web 官网**：包含一个具备 Glassmorphism (玻璃拟态) 风格的 Web Landing Page，支持在线登录与 App 互动。

### 4. 🏗️ 现代化技术栈 (Tech Stack)
*   **架构**：MVVM (Model-View-ViewModel) + Clean Architecture
*   **异步处理**：Kotlin Coroutines + Flow
*   **网络层**：Retrofit 2 + OkHttp (带日志拦截器) + Gson
*   **本地存储**：Room Database (支持离线缓存) + DataStore (Token 管理)
*   **依赖注入**：手动依赖注入 (Manual DI)

## 📥 下载与体验 (Download)

🚀 **[点击这里下载最新 Android APK](https://github.com/lvcean/Timeless/releases)**

或者访问我们的 **[Web 官网与在线演示](https://lvcean.github.io/Timeless/)**

## 🛠️ 项目结构

```
Timeless/
├── app/                  # Android 客户端代码
│   ├── src/main/java     # Kotlin 源码 (MVVM 架构)
│   │   ├── data/         # Repository, API, Room, Supabase
│   │   ├── ui/           # Jetpack Compose Screens (Login, Home...)
│   │   └── viewmodel/    # StateFlow & Business Logic
│   └── src/main/res      # 资源文件
├── docs/                 # Web Landing Page (GitHub Pages)
│   ├── index.html        # 官网入口
│   ├── style.css         # 动效与样式
│   └── script.js         # Web 端 Supabase 交互逻辑
└── README.md             # 项目说明文档
```

## 📸 预览 (Preview)

*(此处可以上传你的截图，或者等会我教你用 Gif 录屏)*

---

### ❤️ 关于开发者

Designed & Developed by **Lvcean YANAOPENG**.
致力于创造美观且实用的软件体验。

Copyright © 2025 Timeless. All rights reserved.
