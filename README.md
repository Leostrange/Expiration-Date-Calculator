# Калькулятор срока годности

*Automatically synced with your [v0.app](https://v0.app) deployments*

[![Deployed on Vercel](https://img.shields.io/badge/Deployed%20on-Vercel-black?style=for-the-badge&logo=vercel)](https://vercel.com/chestergodalive-2654s-projects/v0-product-expiration-calculator)
[![Built with v0](https://img.shields.io/badge/Built%20with-v0.app-black?style=for-the-badge)](https://v0.app/chat/projects/CnKuMM7Iekh)

## Обзор

В репозитории два проекта:

- **Веб (Next.js)** — директории `app`, `components`, `lib`, `styles`. Деплой на Vercel.
- **Android (Kotlin + Jetpack Compose)** — модуль в директории `androidApp/` и корневые Gradle-файлы.

## Сборка Android

Требования:

- JDK 17
- Android SDK (compileSdk 34)
- ADB и эмулятор/устройство

Сборка и установка:

### Быстрый способ
```bash
cd /workspace
./scripts/android-run.sh   # соберёт и установит на подключённое устройство или эмулятор
