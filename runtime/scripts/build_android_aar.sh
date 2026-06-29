#!/usr/bin/env bash
# Build ExecuTorch Android AAR with the pinned QNN SDK (matches .pte export toolchain).
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck disable=SC1091
source "$SCRIPT_DIR/envsetup.sh"

cd "$EXECUTORCH_ROOT"
export ANDROID_NDK="${ANDROID_NDK:-$ANDROID_NDK_ROOT}"
export ANDROID_SDK="${ANDROID_SDK:-$HOME/Android/Sdk}"
export ANDROID_ABIS="${ANDROID_ABIS:-arm64-v8a}"
export BUILD_AAR_DIR="$EXECUTORCH_ROOT/aar-out"
mkdir -p "$BUILD_AAR_DIR"

if [[ ! -f cmake-out-android-so/arm64-v8a/libexecutorch.so ]]; then
  echo "==> Stage 1: native libs (NDK=$ANDROID_NDK, QNN=$QNN_SDK_ROOT)"
  bash scripts/build_android_library.sh 2>&1 | tee "$HOME/lodestar-aar-build.log" || {
    echo "WARN: build_android_library.sh exited non-zero; checking for staged .so files..."
    test -f cmake-out-android-so/arm64-v8a/libexecutorch.so || exit 1
  }
else
  echo "==> Native libs already built — skipping cmake"
fi

bash "$SCRIPT_DIR/assemble_android_aar.sh"
