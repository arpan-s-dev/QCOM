#!/usr/bin/env bash
# Wait for SM8750 PTE export, push to phone, run WSL device smoke test.
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ARTIFACT="${LODESTAR_QWEN06_EXPORT:-$HOME/lodestar-models/qwen3-0_6b_sm8750_export}"
PTE="$ARTIFACT/hybrid_llama_qnn.pte"

echo "==> Waiting for $PTE ..."
for _ in $(seq 1 180); do
  if [[ -f "$PTE" ]]; then
    ls -lh "$PTE"
    break
  fi
  sleep 30
done
[[ -f "$PTE" ]] || { echo "ERROR: export timed out" >&2; exit 1; }

# shellcheck disable=SC1091
source "$SCRIPT_DIR/envsetup.sh"
cd "$EXECUTORCH_ROOT"
python examples/qualcomm/oss_scripts/llama/llama.py \
  -b build-android -m SM8750 -s "${ADB_SERIAL:-}" \
  --decoder_model qwen3-0_6b --model_mode hybrid \
  --max_seq_len 1024 --prefill_ar_len 128 \
  --pre_gen_pte "$ARTIFACT" -a "$ARTIFACT" \
  --prompt "Hello, who are you?" --temperature 0.3 \
  2>&1 | tee "$HOME/lodestar-qwen06-device.log"

echo "==> Device inference OK. Push to Android app with android/push_qwen_models.ps1 -Model 0.6b"
