#!/usr/bin/env bash
# One-time WSL host setup for Lodestar ExecuTorch + QNN builds.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RUNTIME_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "==> Lodestar runtime setup (WSL)"
echo "    (sudo will ask for your Linux password — type it and press Enter)"

# --- apt packages ---
sudo apt-get update
sudo apt-get install -y \
  build-essential cmake ninja-build git curl unzip zip \
  python3 python3-pip python3-venv \
  openjdk-17-jdk \
  adb fastboot \
  libgl1

# g++ 13+ required by ExecuTorch Qualcomm backend
GPP_VER=$(g++ -dumpversion | cut -d. -f1)
if [[ "$GPP_VER" -lt 13 ]]; then
  echo "WARNING: g++ $GPP_VER found; ExecuTorch recommends g++ 13+. Upgrade if build fails."
fi

# --- Android SDK + NDK 26c ---
ANDROID_SDK_ROOT="${ANDROID_SDK_ROOT:-$HOME/Android/Sdk}"
NDK_VERSION="26.2.11394342"
export ANDROID_SDK_ROOT
mkdir -p "$ANDROID_SDK_ROOT/cmdline-tools"

if [[ ! -d "$ANDROID_SDK_ROOT/cmdline-tools/latest" ]]; then
  echo "==> Installing Android command-line tools..."
  TMP_ZIP="/tmp/cmdline-tools.zip"
  curl -fsSL -o "$TMP_ZIP" \
    "https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip"
  unzip -qo "$TMP_ZIP" -d /tmp/cmdline-tools-unpack
  rm -rf "$ANDROID_SDK_ROOT/cmdline-tools/latest"
  mv /tmp/cmdline-tools-unpack/cmdline-tools "$ANDROID_SDK_ROOT/cmdline-tools/latest"
  rm -f "$TMP_ZIP"
fi

SDKMANAGER="$ANDROID_SDK_ROOT/cmdline-tools/latest/bin/sdkmanager"
yes | "$SDKMANAGER" --licenses >/dev/null 2>&1 || true
"$SDKMANAGER" "platform-tools" "ndk;${NDK_VERSION}"

echo "ANDROID_SDK_ROOT=$ANDROID_SDK_ROOT" >> "$HOME/.bashrc"
echo "ANDROID_NDK_ROOT=$ANDROID_SDK_ROOT/ndk/$NDK_VERSION" >> "$HOME/.bashrc"
echo "PATH=\$PATH:\$ANDROID_SDK_ROOT/platform-tools" >> "$HOME/.bashrc"

# --- ExecuTorch v1.0.0 ---
EXECUTORCH_ROOT="${EXECUTORCH_ROOT:-$HOME/src/executorch}"
mkdir -p "$(dirname "$EXECUTORCH_ROOT")"

if [[ ! -d "$EXECUTORCH_ROOT/.git" ]]; then
  echo "==> Cloning ExecuTorch v1.0.0..."
  git clone --depth 1 --branch v1.0.0 https://github.com/pytorch/executorch.git "$EXECUTORCH_ROOT"
  cd "$EXECUTORCH_ROOT"
  git submodule update --init --recursive
else
  echo "==> ExecuTorch already cloned at $EXECUTORCH_ROOT"
fi

# Python venv on native Linux disk (NEVER on /mnt/c OneDrive — cmake breaks on spaces + slow FS)
VENV_DIR="${LODESTAR_VENV:-$HOME/lodestar-venv}"
if [[ ! -d "$VENV_DIR" ]]; then
  python3 -m venv "$VENV_DIR"
fi
# shellcheck disable=SC1091
source "$VENV_DIR/bin/activate"
pip install --upgrade pip wheel setuptools
# ExecuTorch v1.0+ requires CMake 3.29+ (Ubuntu 22.04 apt ships 3.28)
pip install 'cmake>=3.29,<4.0'
pip install torch torchvision torchaudio --index-url https://download.pytorch.org/whl/cpu
# Do NOT pip install -e executorch (cmake build fails on OneDrive paths). Use PYTHONPATH instead.
if [[ -f "$EXECUTORCH_ROOT/requirements-examples.txt" ]]; then
  pip install -r "$EXECUTORCH_ROOT/requirements-examples.txt" || true
fi
pip install hydra-core omegaconf pyyaml pandas tabulate flatbuffers || true
# Remove legacy broken venv if present
[[ -d "$RUNTIME_DIR/.venv" ]] && rm -rf "$RUNTIME_DIR/.venv"

# --- env file ---
ENV_FILE="$RUNTIME_DIR/.env"
if [[ ! -f "$ENV_FILE" ]]; then
  cp "$RUNTIME_DIR/env.template" "$ENV_FILE"
  sed -i "s|QNN_SDK_ROOT=.*|QNN_SDK_ROOT=\$HOME/qnn/2.37.0|" "$ENV_FILE"
  sed -i "s|ANDROID_NDK_ROOT=.*|ANDROID_NDK_ROOT=$ANDROID_SDK_ROOT/ndk/$NDK_VERSION|" "$ENV_FILE"
  sed -i "s|EXECUTORCH_ROOT=.*|EXECUTORCH_ROOT=$EXECUTORCH_ROOT|" "$ENV_FILE"
  echo "==> Created $ENV_FILE — set QNN_SDK_ROOT after downloading QNN SDK."
fi

echo ""
echo "=============================================="
echo "Manual step remaining (P1.0a):"
echo "  1. Download QNN SDK from Qualcomm AI Hub / Package Manager"
echo "     Recommend: QNN 2.37.0"
echo "  2. Unzip to ~/qnn/2.37.0 (or update runtime/.env)"
echo "  3. source runtime/scripts/envsetup.sh"
echo "  4. bash runtime/scripts/verify_env.sh"
echo "=============================================="
