#!/usr/bin/env bash
# Build ExecuTorch with Qualcomm QNN backend for Android.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck disable=SC1091
source "$SCRIPT_DIR/envsetup.sh"

bash "$SCRIPT_DIR/verify_env.sh"

cd "$EXECUTORCH_ROOT"
echo "==> Building ExecuTorch QNN backend (this takes a while)..."
# Pass 1: Android aarch64 (device inference)
./backends/qualcomm/scripts/build.sh --release --skip_x86_64
# Pass 2: Host x86 Python wrappers (needed for .pte export scripts)
./backends/qualcomm/scripts/build.sh --release --skip_aarch64

echo "==> Build complete. Artifacts under $EXECUTORCH_ROOT/build-android/"
