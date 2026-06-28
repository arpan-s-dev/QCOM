#!/usr/bin/env bash
# Preflight: fail fast before a long ExecuTorch build.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck disable=SC1091
source "$SCRIPT_DIR/envsetup.sh"

ERR=0
check() {
  local label="$1" path="$2"
  if [[ -e "$path" ]]; then
    echo "  OK  $label → $path"
  else
    echo "  FAIL $label → $path (missing)" >&2
    ERR=1
  fi
}

echo "==> Verifying Lodestar runtime environment"

check "QNN_README" "$QNN_SDK_ROOT/QNN_README.txt"
check "QNN HTP lib" "$QNN_SDK_ROOT/lib/aarch64-android/libQnnHtp.so"
check "QNN HTP V79 Stub" "$QNN_SDK_ROOT/lib/aarch64-android/libQnnHtpV79Stub.so"
check "Android NDK" "$ANDROID_NDK_ROOT/ndk-build"
check "ExecuTorch root" "$EXECUTORCH_ROOT/backends/qualcomm/scripts/build.sh"
check "adb" "$(command -v adb)"

GPP_VER=$(g++ -dumpversion | cut -d. -f1)
if [[ "$GPP_VER" -ge 13 ]]; then
  echo "  OK  g++ version $GPP_VER"
else
  echo "  WARN g++ $GPP_VER < 13 (may fail ExecuTorch build)" >&2
fi

DEVICE_COUNT=$($ADB devices 2>/dev/null | grep -c 'device$' || true)
if [[ "$DEVICE_COUNT" -ge 1 ]]; then
  echo "  OK  adb device(s) connected: $DEVICE_COUNT"
  $ADB devices -l
else
  echo "  WARN no adb device connected (needed for P1.0d)" >&2
fi

if [[ -f "$QNN_SDK_ROOT/QNN_ReleaseNotes.txt" ]]; then
  echo ""
  echo "QNN SDK release notes (pin this version in DECISIONS.md):"
  head -5 "$QNN_SDK_ROOT/QNN_ReleaseNotes.txt"
fi

if [[ "$ERR" -ne 0 ]]; then
  echo ""
  echo "Environment incomplete. Fix failures above before building." >&2
  exit 1
fi

echo ""
echo "All checks passed."
