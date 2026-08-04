# Mementide Plus（念汐 Plus）

轻量、纯本地的习惯打卡 + 纪念日 App。**数据完全不出本机**（不申请联网权限），无账号、无社交、无云端。

> 这是首个有实际功能的版本（v0.2.0）：打卡与纪念日均已落地本地持久化（Room）。
> 功能取舍原则见念汐仓库的 `原生习惯打卡App_实现方案.md`。

## 已实现功能
- **今日打卡**：习惯列表、一键打卡/取消、补录任意历史日期、连续打卡天数（streak）。
- **纪念日**：每年 / 每月 / 不重复三种循环，自动倒计时（还有 N 天 / 就是今天 / 已过 N 天）。
- 习惯与纪念日均支持新增、删除；数据保存在本机 Room 数据库。
- 底部导航在「今日」与「纪念日」之间切换，右下角 + 快速添加。

## 技术栈
- Kotlin + Jetpack Compose (Material 3)
- MVVM + Hilt（依赖注入）
- Room（本地数据库，已真实持久化）
- 构建：Gradle 8.7 / AGP 8.5.0 / Kotlin 1.9.22

## 构建
```bash
./gradlew assembleRelease      # 产出 app/build/outputs/apk/release/app-release.apk
```
（首次构建会联网拉取 Gradle 与依赖；发布签名凭据来自 local.properties，不入库。）

## 后续里程碑
- 应用锁、本机备份 / 导出导入、统计可视化（图表）
- 习惯分组、提醒、备注

## 仓库
- GitHub：https://github.com/Reverigma/habittrack-app
