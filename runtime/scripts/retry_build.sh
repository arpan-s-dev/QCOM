#!/usr/bin/env bash
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck disable=SC1091
source "$SCRIPT_DIR/envsetup.sh"
cd "$EXECUTORCH_ROOT"
./backends/qualcomm/scripts/build.sh --release --no_clean 2>&1 | tee -a "$HOME/lodestar-build.log"
