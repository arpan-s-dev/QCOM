#!/usr/bin/env bash
# Export Qwen3 0.6B hybrid PTE for SM8750 with the pinned ExecuTorch + QNN toolchain.
# Produces a .pte that matches android/app/libs/executorch.aar (v1.0.0 + QNN 2.37.0.250724).
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ARTIFACT="${LODESTAR_QWEN06_EXPORT:-$HOME/lodestar-models/qwen3-0_6b_sm8750_export}"
MAX_SEQ="${LODESTAR_QWEN_MAX_SEQ:-1024}"
PREFILL_AR="${LODESTAR_QWEN_PREFILL_AR:-128}"

# shellcheck disable=SC1091
source "$SCRIPT_DIR/envsetup.sh"
bash "$SCRIPT_DIR/verify_env.sh"

mkdir -p "$ARTIFACT"
echo "==> Exporting Qwen3-0.6B hybrid PTE for $QCOM_CHIPSET → $ARTIFACT"
echo "    (calibration can take 30–90 min on first run)"

cd "$EXECUTORCH_ROOT"
export PYTHONUNBUFFERED=1
python examples/qualcomm/oss_scripts/llama/llama.py \
  -b build-android \
  -m SM8750 \
  --compile_only \
  --decoder_model qwen3-0_6b \
  --model_mode hybrid \
  --max_seq_len "$MAX_SEQ" \
  --prefill_ar_len "$PREFILL_AR" \
  --prompt "Hello" \
  --temperature 0 \
  --dtype-override fp32 \
  --range_setting minmax \
  --artifact "$ARTIFACT" \
  2>&1 | tee "$HOME/lodestar-qwen06-export.log"

echo "==> Export done. PTE: $ARTIFACT/hybrid_llama_qnn.pte"
ls -lh "$ARTIFACT/hybrid_llama_qnn.pte" "$ARTIFACT/tokenizer.json"
