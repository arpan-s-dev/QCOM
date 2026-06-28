#!/usr/bin/env bash
# Push QNN HTP libraries to device (SM8750 / V79).
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck disable=SC1091
source "$SCRIPT_DIR/envsetup.sh"

bash "$SCRIPT_DIR/verify_env.sh"

echo "==> Pushing QNN libs to $DEVICE_DIR on device..."
$ADB shell "mkdir -p ${DEVICE_DIR}"

LIBS=(
  "lib/aarch64-android/libQnnHtp.so"
  "lib/aarch64-android/libQnnSystem.so"
  "lib/aarch64-android/libQnnHtpV69Stub.so"
  "lib/aarch64-android/libQnnHtpV73Stub.so"
  "lib/aarch64-android/libQnnHtpV75Stub.so"
  "lib/aarch64-android/libQnnHtpV79Stub.so"
  "lib/hexagon-v79/unsigned/libQnnHtpV79Skel.so"
)

for rel in "${LIBS[@]}"; do
  src="$QNN_SDK_ROOT/$rel"
  if [[ -f "$src" ]]; then
    $ADB push "$src" "${DEVICE_DIR}/"
  else
    echo "WARN: missing $src (may be OK for other chipsets)" >&2
  fi
done

echo "==> QNN libs pushed."
