#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

echo "[android-run] Project root: $ROOT_DIR"

has_cmd() { command -v "$1" >/dev/null 2>&1; }

if ! has_cmd adb; then
  echo "[android-run] ERROR: adb not found. Install Android Platform Tools and ensure 'adb' is in PATH."
  exit 1
fi

# Pick gradle wrapper or system gradle
GRADLE_CMD=""
if [ -x "$ROOT_DIR/gradlew" ]; then
  GRADLE_CMD="$ROOT_DIR/gradlew"
elif has_cmd gradle; then
  GRADLE_CMD="gradle"
else
  echo "[android-run] ERROR: Gradle wrapper (./gradlew) or system gradle not found."
  exit 1
fi

# Check for connected device
DEVICE_ID="$(adb devices | awk 'NR>1 && $2=="device" {print $1}' | head -n1 || true)"

if [ -z "${DEVICE_ID}" ]; then
  echo "[android-run] No connected devices. Trying to start an emulator..."

  EMULATOR_BIN="$(command -v emulator || true)"
  if [ -z "$EMULATOR_BIN" ] && [ -n "${ANDROID_HOME:-}" ] && [ -x "$ANDROID_HOME/emulator/emulator" ]; then
    EMULATOR_BIN="$ANDROID_HOME/emulator/emulator"
  fi
  if [ -z "$EMULATOR_BIN" ] && [ -n "${ANDROID_SDK_ROOT:-}" ] && [ -x "$ANDROID_SDK_ROOT/emulator/emulator" ]; then
    EMULATOR_BIN="$ANDROID_SDK_ROOT/emulator/emulator"
  fi
  if [ -z "$EMULATOR_BIN" ]; then
    echo "[android-run] ERROR: Android emulator binary not found. Ensure SDK tools are installed and 'emulator' is in PATH."
    exit 1
  fi

  # Choose AVD: first arg or first available
  AVD_NAME="${1:-$($EMULATOR_BIN -list-avds | head -n1)}"
  if [ -z "$AVD_NAME" ]; then
    echo "[android-run] ERROR: No AVDs found. Create one via 'avdmanager' or Android Studio."
    exit 1
  fi

  echo "[android-run] Starting emulator AVD '$AVD_NAME'..."
  "$EMULATOR_BIN" -avd "$AVD_NAME" -no-snapshot -netdelay none -netspeed full >/dev/null 2>&1 &

  echo "[android-run] Waiting for emulator to be ready (this can take ~30-60s)..."
  adb wait-for-device

  # Wait until sys.boot_completed == 1
  BOOTED="0"
  for _ in $(seq 1 120); do
    BOOTED="$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')"
    if [ "$BOOTED" = "1" ]; then
      break
    fi
    sleep 2
  done
  if [ "$BOOTED" != "1" ]; then
    echo "[android-run] ERROR: Emulator boot timed out."
    exit 1
  fi
fi

echo "[android-run] Building debug APK..."
"$GRADLE_CMD" :androidApp:assembleDebug

APK_PATH="$(ls -t "$ROOT_DIR"/androidApp/build/outputs/apk/debug/*.apk 2>/dev/null | head -n1 || true)"
if [ -z "$APK_PATH" ] || [ ! -f "$APK_PATH" ]; then
  echo "[android-run] ERROR: APK not found in androidApp/build/outputs/apk/debug."
  exit 1
fi
echo "[android-run] Installing APK: $APK_PATH"
adb install -r "$APK_PATH" || true

PACKAGE="com.example.expiration"
ACTIVITY="com.example.expiration.MainActivity"
echo "[android-run] Launching $PACKAGE/$ACTIVITY"
adb shell am start -n "$PACKAGE/$ACTIVITY" >/dev/null 2>&1 || true

echo "[android-run] Done."

