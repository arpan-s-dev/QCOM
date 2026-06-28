#!/usr/bin/env bash
# P1.1 — Export Llama 3.2 to QNN .pte (KV mode). Default 1B for hackathon speed.
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RUNTIME_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

# 1B default (hackathon); override: LODESTAR_LLAMA_DECODER=llama3_2-3b_instruct
DECODER="${LODESTAR_LLAMA_DECODER:-llama3_2-1b_instruct}"
if [[ "$DECODER" == *3b* ]]; then
  MODEL_DIR="${LODESTAR_LLAMA_DIR:-$RUNTIME_DIR/models/llama3_2_3b}"
  OUT_PTE="$RUNTIME_DIR/models/llama3_2_3b_kv.pte"
else
  MODEL_DIR="${LODESTAR_LLAMA_DIR:-$RUNTIME_DIR/models/llama3_2_1b}"
  OUT_PTE="$RUNTIME_DIR/models/llama3_2_1b_kv.pte"
fi
ARTIFACT="${LODESTAR_LLAMA_ARTIFACT:-$MODEL_DIR/artifact}"
MAX_SEQ_LEN="${LODESTAR_LLAMA_MAX_SEQ:-256}"

# shellcheck disable=SC1091
source "$SCRIPT_DIR/envsetup.sh"
bash "$SCRIPT_DIR/setup_qnn_host_libs.sh"
bash "$SCRIPT_DIR/verify_env.sh"

for f in consolidated.00.pth params.json tokenizer.model; do
  if [[ ! -f "$MODEL_DIR/$f" ]]; then
    echo "ERROR: missing $MODEL_DIR/$f" >&2
    exit 1
  fi
done

echo "==> Export $DECODER (kv, max_seq_len=$MAX_SEQ_LEN) — needs ~12GB+ WSL RAM for 1B"
cd "$EXECUTORCH_ROOT"
python examples/qualcomm/oss_scripts/llama/llama.py \
  -b build-android \
  -m "$QCOM_CHIPSET" \
  --compile_only \
  --prompt "compile export placeholder" \
  --checkpoint "$MODEL_DIR/consolidated.00.pth" \
  --params "$MODEL_DIR/params.json" \
  --tokenizer_model "$MODEL_DIR/tokenizer.model" \
  --decoder_model "$DECODER" \
  --model_mode kv \
  --max_seq_len "$MAX_SEQ_LEN" \
  -a "$ARTIFACT" \
  2>&1 | tee "$HOME/lodestar-llama-export.log"

PTE_SRC="$ARTIFACT/kv_llama_qnn.pte"
[[ -f "$PTE_SRC" ]] || { echo "ERROR: no $PTE_SRC"; exit 1; }
cp -f "$PTE_SRC" "$OUT_PTE"
cp -f "$MODEL_DIR/tokenizer.model" "$RUNTIME_DIR/models/llama3_2_tokenizer.model"
echo "==> Exported: $OUT_PTE ($(du -h "$OUT_PTE" | cut -f1))"
