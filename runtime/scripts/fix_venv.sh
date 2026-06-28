#!/usr/bin/env bash
# Recovery: fix failed pip install -e executorch (OneDrive path + spaces break cmake).
# NO sudo required. Run after setup_wsl.sh apt/NDK/clone steps already succeeded.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RUNTIME_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
EXECUTORCH_ROOT="${EXECUTORCH_ROOT:-$HOME/src/executorch}"
VENV_DIR="${LODESTAR_VENV:-$HOME/lodestar-venv}"

echo "==> Lodestar venv fix (native Linux disk, no OneDrive)"
echo "    Venv: $VENV_DIR"
echo "    ExecuTorch: $EXECUTORCH_ROOT"

if [[ ! -d "$EXECUTORCH_ROOT" ]]; then
  echo "ERROR: ExecuTorch not found at $EXECUTORCH_ROOT. Clone first or set EXECUTORCH_ROOT." >&2
  exit 1
fi

# Remove broken OneDrive venv (optional — saves space)
if [[ -d "$RUNTIME_DIR/.venv" ]]; then
  echo "==> Removing broken OneDrive venv at runtime/.venv ..."
  rm -rf "$RUNTIME_DIR/.venv"
fi

python3 -m venv "$VENV_DIR"
# shellcheck disable=SC1091
source "$VENV_DIR/bin/activate"
pip install --upgrade pip wheel setuptools

echo "==> Installing CPU-only PyTorch (no CUDA — export runs on CPU, inference on phone NPU)..."
pip install torch torchvision torchaudio --index-url https://download.pytorch.org/whl/cpu

echo "==> Installing ExecuTorch example deps (NO editable cmake build)..."
if [[ -f "$EXECUTORCH_ROOT/requirements-examples.txt" ]]; then
  pip install -r "$EXECUTORCH_ROOT/requirements-examples.txt" || true
fi
# Common export script deps
pip install hydra-core omegaconf pyyaml pandas tabulate flatbuffers pytorch_tokenizers 'torchao==0.15.0'

echo "==> Updating runtime/.env ..."
ENV_FILE="$RUNTIME_DIR/.env"
if [[ ! -f "$ENV_FILE" ]]; then
  cp "$RUNTIME_DIR/env.template" "$ENV_FILE"
fi
# Ensure paths without re-breaking QNN path
grep -q '^LODESTAR_VENV=' "$ENV_FILE" 2>/dev/null || echo "LODESTAR_VENV=$VENV_DIR" >> "$ENV_FILE"
sed -i "s|^LODESTAR_VENV=.*|LODESTAR_VENV=$VENV_DIR|" "$ENV_FILE" 2>/dev/null || echo "LODESTAR_VENV=$VENV_DIR" >> "$ENV_FILE"

echo ""
echo "==> Done. Test with:"
echo "  source $RUNTIME_DIR/scripts/envsetup.sh"
echo "  bash $RUNTIME_DIR/scripts/verify_env.sh"
echo ""
echo "Export uses PYTHONPATH=\$EXECUTORCH_ROOT — no pip install -e executorch needed."
