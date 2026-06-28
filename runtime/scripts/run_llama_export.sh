#!/usr/bin/env bash
# Run Llama 1B QNN export (weights on native Linux disk).
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RUNTIME_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
REPO_DIR="$(cd "$RUNTIME_DIR/.." && pwd)"

source "$SCRIPT_DIR/envsetup.sh"
source ~/lodestar-venv/bin/activate

export LODESTAR_LLAMA_DIR="${LODESTAR_LLAMA_DIR:-$HOME/lodestar-models/llama3_2_1b}"
export LODESTAR_LLAMA_DECODER="${LODESTAR_LLAMA_DECODER:-llama3_2-1b_instruct}"

cd "$REPO_DIR"
bash "$SCRIPT_DIR/export_llama.sh" 2>&1 | tee ~/lodestar-llama-export.log
