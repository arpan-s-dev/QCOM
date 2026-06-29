#!/usr/bin/env bash
# Run Qwen3 0.6B hybrid PTE on device (proof NPU pipeline before 1.7B re-export).
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MODEL_DIR="${LODESTAR_QWEN06_DIR:-$HOME/lodestar-models/qwen3-0_6b_sm8550}"
PROMPT="${LODESTAR_QWEN_PROMPT:-Hello, who are you?}"
MAX_SEQ="${LODESTAR_QWEN_MAX_SEQ:-1024}"
PREFILL_AR="${LODESTAR_QWEN_PREFILL_AR:-128}"

# shellcheck disable=SC1091
source "$SCRIPT_DIR/envsetup.sh"
bash "$SCRIPT_DIR/verify_env.sh"
bash "$SCRIPT_DIR/download_qwen06b_pte.sh"

echo "==> Qwen3 0.6B NPU inference (SM8550 PTE on $QCOM_CHIPSET device)"
cd "$EXECUTORCH_ROOT"
python examples/qualcomm/oss_scripts/llama/llama.py \
  -b build-android \
  -m SM8750 \
  -s "${ADB_SERIAL:-}" \
  --decoder_model qwen3-0_6b \
  --model_mode hybrid \
  --max_seq_len "$MAX_SEQ" \
  --prefill_ar_len "$PREFILL_AR" \
  --pre_gen_pte "$MODEL_DIR" \
  -a "$MODEL_DIR" \
  --prompt "$PROMPT" \
  --temperature 0.3 \
  2>&1 | tee "$HOME/lodestar-qwen06-device.log"
