#!/usr/bin/env bash
# Build host x86 PyQnn wrappers only (for .pte export). Android build must exist first.
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck disable=SC1091
source "$SCRIPT_DIR/envsetup.sh"
cd "$EXECUTORCH_ROOT"
./backends/qualcomm/scripts/build.sh --release --skip_aarch64 2>&1 | tee -a "$HOME/lodestar-build-x86.log"
