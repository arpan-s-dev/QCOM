#!/usr/bin/env bash
# Download Llama 3.2 via Meta signed URL (from llama.com approval email).
# FAST PATH: default 1B Instruct (~2GB, fits hackathon timeline).
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RUNTIME_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
MODEL_ID="${LODESTAR_LLAMA_MODEL:-Llama3.2-1B-Instruct}"
MODEL_DIR="${LODESTAR_LLAMA_DIR:-$RUNTIME_DIR/models/llama3_2_1b}"

if [[ -z "${META_URL:-}" ]]; then
  echo "ERROR: set META_URL from your Meta approval email (48h signed URL)." >&2
  echo "  export META_URL='https://llama3-2-lightweight.llamameta.net/...'" >&2
  exit 1
fi

# shellcheck disable=SC1091
source "$SCRIPT_DIR/envsetup.sh"
source ~/lodestar-venv/bin/activate

mkdir -p "$MODEL_DIR"
echo "==> Downloading $MODEL_ID via Meta CLI → $MODEL_DIR"

# llama-models 0.3.x has a broken import (llama_models.cli.model). Use llama-stack CLI.
if ! python -c "import llama_stack" 2>/dev/null; then
  echo "==> Installing llama-stack (Meta download CLI)..."
  pip install -q 'llama-stack==0.2.23'
fi

if command -v llama >/dev/null 2>&1; then
  # Correct syntax: "llama model download" (space, not llama-model)
  llama model download \
    --source meta \
    --model-id "$MODEL_ID" \
    --meta-url "$META_URL"
elif command -v llama-model >/dev/null 2>&1; then
  echo "WARN: falling back to llama-model (may fail on llama-models 0.3.x)" >&2
  llama-model download \
    --source meta \
    --model-id "$MODEL_ID" \
    --meta-url "$META_URL"
else
  echo "ERROR: no llama CLI. Run: pip install 'llama-stack==0.2.23'" >&2
  exit 1
fi

# Meta CLI cache — find original-format files (check known paths first)
SEARCH_ROOT="${LLAMA_MODELS_DIR:-$HOME/.llama/checkpoints}"
FOUND=""
for candidate in \
  "$SEARCH_ROOT/$MODEL_ID/consolidated.00.pth" \
  "$SEARCH_ROOT/Llama3.2-1B-Instruct/consolidated.00.pth" \
  "$SEARCH_ROOT/Llama3.2-3B-Instruct/consolidated.00.pth"; do
  if [[ -f "$candidate" ]]; then
    FOUND="$candidate"
    break
  fi
done
if [[ -z "$FOUND" ]]; then
  FOUND="$(find "$SEARCH_ROOT" "$HOME/.cache/llama" "$RUNTIME_DIR" -name 'consolidated.00.pth' 2>/dev/null | head -1 || true)"
fi
if [[ -z "$FOUND" ]]; then
  FOUND="$(find "$HOME" -path '*Llama*3.2*' -name 'consolidated.00.pth' 2>/dev/null | head -1 || true)"
fi

if [[ -n "$FOUND" ]]; then
  SRC_DIR="$(dirname "$FOUND")"
  echo "==> Found weights in $SRC_DIR"
  cp -f "$SRC_DIR/consolidated.00.pth" "$MODEL_DIR/"
  cp -f "$SRC_DIR/params.json" "$MODEL_DIR/"
  cp -f "$SRC_DIR/tokenizer.model" "$MODEL_DIR/"
else
  echo "WARN: consolidated.00.pth not found automatically. Search and copy manually:" >&2
  find "$HOME" -name 'consolidated.00.pth' 2>/dev/null | head -5 >&2
  exit 1
fi

ls -lh "$MODEL_DIR"/consolidated.00.pth "$MODEL_DIR"/params.json "$MODEL_DIR"/tokenizer.model
echo "==> Ready for: LODESTAR_LLAMA_DIR=$MODEL_DIR bash runtime/scripts/export_llama.sh"
