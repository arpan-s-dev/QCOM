#!/usr/bin/env bash
# Download a known-working Qwen3 0.6B hybrid PTE (SM8550) for NPU pipeline validation.
set -euo pipefail
MODEL_DIR="${LODESTAR_QWEN06_DIR:-$HOME/lodestar-models/qwen3-0_6b_sm8550}"
mkdir -p "$MODEL_DIR"
cd "$MODEL_DIR"
if [[ ! -f hybrid_llama_qnn.pte ]]; then
  huggingface-cli download K9FxNa/Qwen3-0.6B-SM8550-Hybrid \
    hybrid_llama_qnn.pte tokenizer.json --local-dir .
fi
ls -lh hybrid_llama_qnn.pte tokenizer.json
