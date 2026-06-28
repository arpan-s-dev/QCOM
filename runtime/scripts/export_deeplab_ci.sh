#!/usr/bin/env bash
# P1.0c fast path — random input (--ci), skips corrupt VOC download. Valid for DE-RISK gate.
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RUNTIME_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
# shellcheck disable=SC1091
source "$SCRIPT_DIR/envsetup.sh"
bash "$SCRIPT_DIR/verify_env.sh"
cd "$EXECUTORCH_ROOT"
echo "==> Exporting DeepLabV3 for $QCOM_CHIPSET (CI mode — gate only)..."
python -m examples.qualcomm.scripts.deeplab_v3 \
  -b build-android \
  -m "$QCOM_CHIPSET" \
  --compile_only \
  --ci
SRC_PTE="$EXECUTORCH_ROOT/deeplab_v3/dl3_qnn_q8.pte"
if [[ ! -f "$SRC_PTE" ]]; then
  SRC_PTE="$EXECUTORCH_ROOT/deeplab_v3/dlv3_qnn.pte"
fi
mkdir -p "$RUNTIME_DIR/models"
cp -f "$SRC_PTE" "$RUNTIME_DIR/models/dlv3_qnn.pte"
echo "==> Model copied to $RUNTIME_DIR/models/dlv3_qnn.pte (from $(basename "$SRC_PTE"))"
