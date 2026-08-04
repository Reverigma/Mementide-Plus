# HabitTrack（占位名，后续可改）

轻量、纯本地的习惯打卡 App。**数据完全不出本机**（不申请联网权限），无账号、无社交、无云端。

> 这是 M0 首版脚手架：Gradle + AGP + Jetpack Compose + Hilt + Room 已跑通，首页可展示习惯列表与打卡勾选。
> 功能取舍原则见 `原生习惯打卡App_实现方案.md`（位于念汐仓库）。

## 技术栈
- Kotlin + Jetpack Compose (Material 3)
- MVVM + Hilt（依赖注入）
- Room（本地数据库，M0 暂用示例数据，后续接入持久化）
- 构建：Gradle 8.5 / AGP 8.5.0 / Kotlin 1.9.22

## 构建
```bash
./gradlew assembleDebug      # 产出 app/build/outputs/apk/debug/app-debug.apk
```
（首次构建会联网拉取 Gradle 与依赖。）

## 当前状态
- 首页「今日习惯」展示示例习惯 + 打卡勾选（本地状态，未持久化）
- 应用锁、本机备份、统计可视化等为后续里程碑

## 仓库
- GitHub：https://github.com/Reverigma/habittrack-app
