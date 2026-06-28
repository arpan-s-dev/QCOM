#!/usr/bin/env bash
# Run pre-built Qwen3 1.7B hybrid .pte on S25 (SM8750) — no export needed.
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RUNTIME_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
MODEL_DIR="${LODESTAR_QWEN_DIR:-$HOME/lodestar-models/qwen3-1_7b}"
# Fallback to repo copy if native dir missing
if [[ ! -f "$MODEL_DIR/hybrid_llama_qnn.pte" ]] && [[ -f "$RUNTIME_DIR/models/qwen3-1_7b_ctxt-4096_SM8750/hybrid_llama_qnn.pte" ]]; then
  MODEL_DIR="$RUNTIME_DIR/models/qwen3-1_7b_ctxt-4096_SM8750"
fi
PTE="$MODEL_DIR/hybrid_llama_qnn.pte"
PROMPT="${LODESTAR_QWEN_PROMPT:-Hello}"
MAX_SEQ="${LODESTAR_QWEN_MAX_SEQ:-4096}"
PREFILL_AR="${LODESTAR_QWEN_PREFILL_AR:-32}"

# shellcheck disable=SC1091
source "$SCRIPT_DIR/envsetup.sh"
bash "$SCRIPT_DIR/verify_env.sh"

for f in hybrid_llama_qnn.pte tokenizer.json; do
  if [[ ! -f "$MODEL_DIR/$f" ]]; then
    echo "ERROR: missing $MODEL_DIR/$f" >&2
    echo "Copy the bundle from Z:\\LOca\\qwen3-1_7b_ctxt-4096_SM8750 into runtime/models/" >&2
    exit 1
  fi
done

echo "==> Qwen3 1.7B NPU inference (pre-built PTE, hybrid, ctx=$MAX_SEQ)"
echo "    Model dir: $MODEL_DIR"
echo "    PTE size:  $(du -h "$PTE" | cut -f1)"

cd "$EXECUTORCH_ROOT"
python examples/qualcomm/oss_scripts/llama/llama.py \
  -b build-android \
  -m "$QCOM_CHIPSET" \
  -s "${ADB_SERIAL:-}" \
  --decoder_model qwen3-1_7b \
  --model_mode hybrid \
  --max_seq_len "$MAX_SEQ" \
  --prefill_ar_len "$PREFILL_AR" \
  --pre_gen_pte "$MODEL_DIR" \
  -a "$MODEL_DIR" \
  --prompt "$PROMPT" \
  --temperature 0.3 \
  2>&1 | tee "$HOME/lodestar-qwen-device.log"

echo "==> Done. Log: ~/lodestar-qwen-device.log"
