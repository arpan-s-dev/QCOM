#!/usr/bin/env bash
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck disable=SC1091
source "$SCRIPT_DIR/envsetup.sh"

cd "$EXECUTORCH_ROOT/extension/android"
./gradlew --stop 2>/dev/null || true
rm -rf "$HOME/.gradle/caches/"*/transforms
export ANDROID_HOME="${ANDROID_SDK:-$HOME/Android/Sdk}"
./gradlew :executorch_android:assembleRelease --no-daemon -x test -x lint

BUILD_AAR_DIR="$EXECUTORCH_ROOT/aar-out"
mkdir -p "$BUILD_AAR_DIR"
for cand in \
  "$EXECUTORCH_ROOT/extension/android/executorch_android/build/outputs/aar/executorch_android-release.aar" \
  "$EXECUTORCH_ROOT/extension/android/executorch_android/build/outputs/aar/executorch_android-debug.aar"; do
  if [[ -f "$cand" ]]; then
    cp -f "$cand" "$BUILD_AAR_DIR/executorch.aar"
    break
  fi
done

WIN_LIBS="/mnt/c/Users/ranji/OneDrive/Desktop/QCOM Hackathon/QCOM/android/app/libs"
mkdir -p "$WIN_LIBS"
cp -f "$BUILD_AAR_DIR/executorch.aar" "$WIN_LIBS/executorch.aar"
ls -lh "$WIN_LIBS/executorch.aar"
