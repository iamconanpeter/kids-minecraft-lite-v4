#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$PROJECT_DIR"

APP_ID="com.iamconanpeter.kidsminecraftlite"
MAIN_ACTIVITY=".MainActivity"
AVD_NAME="KidsPixel_API_34"

# Resolve Android SDK from env or local.properties
if [[ -z "${ANDROID_SDK_ROOT:-}" ]]; then
  if [[ -f local.properties ]]; then
    SDK_DIR_LINE="$(grep -E '^sdk\.dir=' local.properties || true)"
    if [[ -n "$SDK_DIR_LINE" ]]; then
      ANDROID_SDK_ROOT="${SDK_DIR_LINE#sdk.dir=}"
      export ANDROID_SDK_ROOT
    fi
  fi
fi

if [[ -z "${ANDROID_SDK_ROOT:-}" ]]; then
  echo "[ERROR] ANDROID_SDK_ROOT not set and sdk.dir not found." >&2
  exit 1
fi

export ANDROID_HOME="$ANDROID_SDK_ROOT"
export PATH="$ANDROID_SDK_ROOT/platform-tools:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:${PATH}"

# Resolve Java 17
if [[ -z "${JAVA_HOME:-}" ]]; then
  for j in \
    "$PROJECT_DIR/.jdks/jdk-17.0.18+8" \
    "$PROJECT_DIR/../.jdks/jdk-17.0.18+8" \
    "/home/openclaw/.openclaw/workspace/.jdks/jdk-17.0.18+8" \
    "/home/openclaw/.jdks/jdk-17"; do
    if [[ -x "$j/bin/java" ]]; then
      export JAVA_HOME="$j"
      break
    fi
  done
fi

if [[ -z "${JAVA_HOME:-}" || ! -x "$JAVA_HOME/bin/java" ]]; then
  echo "[ERROR] JAVA_HOME (JDK17+) not found." >&2
  exit 1
fi
export PATH="$JAVA_HOME/bin:$PATH"

provision_emulator() {
  echo "[INFO] Provisioning Android emulator components (first-time only)..."
  yes | sdkmanager --licenses >/dev/null || true
  sdkmanager "platform-tools" "emulator" "platforms;android-34" "system-images;android-34;google_apis;x86_64"

  if ! avdmanager list avd | grep -q "Name: ${AVD_NAME}"; then
    echo "[INFO] Creating AVD: ${AVD_NAME}"
    echo "no" | avdmanager create avd -n "$AVD_NAME" -k "system-images;android-34;google_apis;x86_64" --device "pixel"
  fi
}

start_emulator_if_needed() {
  local connected
  connected="$(adb devices | awk 'NR>1 && $2=="device" {print $1}' | wc -l | tr -d ' ')"
  if [[ "$connected" -gt 0 ]]; then
    echo "[INFO] Found connected device(s), skip emulator start."
    return 0
  fi

  if [[ ! -x "$ANDROID_SDK_ROOT/emulator/emulator" ]]; then
    echo "[WARN] Emulator binary not found at $ANDROID_SDK_ROOT/emulator/emulator"
    echo "[HINT] Run: $0 --provision-emulator"
    return 0
  fi

  if ! avdmanager list avd | grep -q "Name: ${AVD_NAME}"; then
    echo "[WARN] AVD '${AVD_NAME}' not found."
    echo "[HINT] Run: $0 --provision-emulator"
    return 0
  fi

  echo "[INFO] Starting emulator ${AVD_NAME}..."
  nohup "$ANDROID_SDK_ROOT/emulator/emulator" -avd "$AVD_NAME" -no-snapshot-save -no-boot-anim >/tmp/${AVD_NAME}.log 2>&1 &
  local emu_pid=$!

  echo "[INFO] Waiting for emulator boot..."
  local booted=""
  for _ in {1..90}; do
    if ! kill -0 "$emu_pid" 2>/dev/null; then
      echo "[WARN] Emulator process exited early."
      tail -n 40 "/tmp/${AVD_NAME}.log" || true
      return 0
    fi

    if adb devices | awk 'NR>1 && $2=="device" {print $1}' | grep -q .; then
      booted="$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')"
      [[ "$booted" == "1" ]] && break
    fi

    sleep 2
  done

  if [[ "$booted" == "1" ]]; then
    echo "[INFO] Emulator boot completed."
  else
    echo "[WARN] Emulator did not fully boot in time."
    tail -n 40 "/tmp/${AVD_NAME}.log" || true
  fi
}

build_app() {
  echo "[INFO] Building app..."
  ./gradlew test assembleDebug
}

deploy_to_connected() {
  local apk
  apk="app/build/outputs/apk/debug/app-debug.apk"
  if [[ ! -f "$apk" ]]; then
    echo "[ERROR] APK not found: $apk" >&2
    exit 1
  fi

  mapfile -t devices < <(adb devices | awk 'NR>1 && $2=="device" {print $1}')
  if [[ ${#devices[@]} -eq 0 ]]; then
    echo "[WARN] No connected devices/emulators found. Build succeeded but deployment skipped."
    return 0
  fi

  echo "[INFO] Deploying to ${#devices[@]} device(s): ${devices[*]}"
  for d in "${devices[@]}"; do
    echo "[INFO] Installing on $d"
    adb -s "$d" install -r "$apk"
    adb -s "$d" shell am start -n "${APP_ID}/${APP_ID}${MAIN_ACTIVITY}" >/dev/null || true
  done
}

main() {
  adb start-server >/dev/null

  if [[ "${1:-}" == "--provision-emulator" ]]; then
    provision_emulator
  fi

  start_emulator_if_needed
  build_app
  deploy_to_connected

  echo "[DONE] Build + deploy flow completed."
}

main "$@"
