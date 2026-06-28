#!/usr/bin/env bash
# Build ExecuTorch with Qualcomm QNN backend for Android.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck disable=SC1091
source "$SCRIPT_DIR/envsetup.sh"

bash "$SCRIPT_DIR/verify_env.sh"

cd "$EXECUTORCH_ROOT"
echo "==> Building ExecuTorch QNN backend (this takes a while)..."
./backends/qualcomm/scripts/build.sh --release

echo "==> Build complete. Artifacts under $EXECUTORCH_ROOT/build-android/"
