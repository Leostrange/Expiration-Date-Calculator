# Калькулятор срока годности

*Automatically synced with your [v0.app](https://v0.app) deployments*

[![Deployed on Vercel](https://img.shields.io/badge/Deployed%20on-Vercel-black?style=for-the-badge&logo=vercel)](https://vercel.com/chestergodalive-2654s-projects/v0-product-expiration-calculator)
[![Built with v0](https://img.shields.io/badge/Built%20with-v0.app-black?style=for-the-badge)](https://v0.app/chat/projects/CnKuMM7Iekh)

## Обзор

В репозитории теперь 2 проекта:

- Веб (Next.js) — исходная версия в директориях `app`, `components`, `lib`, `styles`.
- Android (Kotlin + Jetpack Compose) — новая мобильная версия в директории `app/` (модуль Android) и корневых Gradle-файлах.

## Сборка Android

Требования:

- JDK 17
- Android SDK (compileSdk 34)
- ADB и эмулятор/устройство

Сборка и установка:

```bash
cd /workspace
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Функциональность Android

- Ввод даты производства (ДД.ММ.ГГГГ) с автоформатированием
- Ввод срока (число) и выбор единиц (дни/недели/месяцы/годы)
- Карточка результата: дата истечения, сколько осталось/просрочено, статус
- Переключение светлой/тёмной темы, сохранение в DataStore

## Примечание

Веб-часть (Next.js) сохранена без изменений и может продолжать деплоиться на Vercel, Android-версия — отдельный модуль.
