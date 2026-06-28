#!/usr/bin/env bash
# P1.0d — Run stock DeepLabV3 .pte on device via qnn_executor_runner (NPU gate test).
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RUNTIME_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
# shellcheck disable=SC1091
source "$SCRIPT_DIR/envsetup.sh"

MODEL="${1:-$RUNTIME_DIR/models/dlv3_qnn.pte}"
RUNNER="$EXECUTORCH_ROOT/build-android/examples/qualcomm/executor_runner/qnn_executor_runner"
BACKEND="$EXECUTORCH_ROOT/build-android/lib/libqnn_executorch_backend.so"

if [[ ! -f "$MODEL" ]]; then
  echo "Model not found: $MODEL — run export_deeplab.sh first." >&2
  exit 1
fi

bash "$SCRIPT_DIR/push_qnn_libs.sh"

echo "==> Pushing runner + model..."
$ADB push "$MODEL" "${DEVICE_DIR}/dlv3_qnn.pte"
$ADB push "$RUNNER" "${DEVICE_DIR}/qnn_executor_runner"
$ADB push "$BACKEND" "${DEVICE_DIR}/libqnn_executorch_backend.so"
$ADB shell "chmod +x ${DEVICE_DIR}/qnn_executor_runner"

echo "==> Running inference on NPU (watch logcat for QNN HTP, not CPU fallback)..."
$ADB shell "cd ${DEVICE_DIR} \
  && export LD_LIBRARY_PATH=${DEVICE_DIR} \
  && export ADSP_LIBRARY_PATH=${DEVICE_DIR} \
  && ./qnn_executor_runner --model_path ./dlv3_qnn.pte"

echo ""
echo "Confirm in logcat: Hexagon/HTP execution. If CPU fallback, check QNN version pin."
echo "  $ADB logcat -d | grep -iE 'qnn|htp|hexagon'"
