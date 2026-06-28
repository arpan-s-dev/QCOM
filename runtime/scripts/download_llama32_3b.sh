#!/usr/bin/env bash
# Download Meta Llama 3.2 3B Instruct weights for Qualcomm export.
# Requires: huggingface-cli login (accept license at huggingface.co/meta-llama/Llama-3.2-3B-Instruct)
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RUNTIME_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
MODEL_DIR="${LODESTAR_LLAMA_DIR:-$RUNTIME_DIR/models/llama3_2_3b}"
mkdir -p "$MODEL_DIR"
export MODEL_DIR

# shellcheck disable=SC1091
source "$SCRIPT_DIR/envsetup.sh"

if [[ -f "$MODEL_DIR/consolidated.00.pth" && -f "$MODEL_DIR/tokenizer.model" && -f "$MODEL_DIR/params.json" ]]; then
  echo "==> Llama weights already present in $MODEL_DIR"
  exit 0
fi

echo "==> Downloading meta-llama/Llama-3.2-3B-Instruct to $MODEL_DIR"
echo "    Prereqs:"
echo "      1. Accept license: https://huggingface.co/meta-llama/Llama-3.2-3B-Instruct"
echo "      2. Create a READ token: https://huggingface.co/settings/tokens"
echo "      3. Auth ONE of:"
echo "           export HF_TOKEN=hf_....   (recommended in WSL)"
echo "           hf auth login"
echo "         OR manual files from https://www.llama.com/ into $MODEL_DIR"
pip install -q huggingface_hub

if [[ -z "${HF_TOKEN:-}" ]] && [[ ! -f "$HOME/.cache/huggingface/token" ]]; then
  echo "ERROR: No HF token. Run: export HF_TOKEN=hf_your_read_token" >&2
  exit 1
fi

python <<'PY'
import os, shutil
from pathlib import Path
from huggingface_hub import hf_hub_download

model_dir = Path(os.environ["MODEL_DIR"])
repo = "meta-llama/Llama-3.2-3B-Instruct"
token = os.environ.get("HF_TOKEN")
files = ["original/consolidated.00.pth", "original/params.json", "original/tokenizer.model"]
for rel in files:
    print(f"  fetching {rel}...")
    local = hf_hub_download(
        repo_id=repo,
        filename=rel,
        local_dir=str(model_dir / "_hf"),
        token=token,
    )
    dest = model_dir / Path(rel).name
    shutil.copy2(local, dest)
    print(f"  -> {dest}")
print("OK")
PY

ls -lh "$MODEL_DIR"/consolidated.00.pth "$MODEL_DIR"/params.json "$MODEL_DIR"/tokenizer.model
