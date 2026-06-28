#!/usr/bin/env bash
# P1.0c — Export stock DeepLabV3 .pte via QnnPartitioner (INT8 gate model).
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RUNTIME_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
# shellcheck disable=SC1091
source "$SCRIPT_DIR/envsetup.sh"

bash "$SCRIPT_DIR/verify_env.sh"

cd "$EXECUTORCH_ROOT"
echo "==> Exporting DeepLabV3 for $QCOM_CHIPSET..."
python -m examples.qualcomm.scripts.deeplab_v3 \
  -b build-android \
  -m "$QCOM_CHIPSET" \
  --compile_only \
  --download

mkdir -p "$RUNTIME_DIR/models"
cp -f "$EXECUTORCH_ROOT/deeplab_v3/dlv3_qnn.pte" "$RUNTIME_DIR/models/dlv3_qnn.pte"
echo "==> Model copied to $RUNTIME_DIR/models/dlv3_qnn.pte"
