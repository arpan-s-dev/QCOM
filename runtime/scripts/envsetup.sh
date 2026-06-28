#!/usr/bin/env bash
# Source this in every build shell: source runtime/scripts/envsetup.sh
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RUNTIME_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
ENV_FILE="$RUNTIME_DIR/.env"

if [[ ! -f "$ENV_FILE" ]]; then
  echo "ERROR: $ENV_FILE not found. Run setup_wsl.sh and copy env.template first." >&2
  return 1 2>/dev/null || exit 1
fi

# shellcheck disable=SC1090
set -a
source "$ENV_FILE"
set +a

export ANDROID_NDK_HOME="${ANDROID_NDK_HOME:-$ANDROID_NDK_ROOT}"
export ANDROID_NDK_ROOT
export EXECUTORCH_ROOT
export QCOM_CHIPSET="${QCOM_CHIPSET:-SM8750}"
export DEVICE_DIR="${DEVICE_DIR:-/data/local/tmp/lodestar}"

# QNN SDK env (official) — avoid unbound PYTHONPATH under set -u
export PYTHONPATH="${PYTHONPATH:-}"
if [[ -f "$QNN_SDK_ROOT/bin/envsetup.sh" ]]; then
  set +u
  # shellcheck disable=SC1091
  source "$QNN_SDK_ROOT/bin/envsetup.sh"
  set -u
else
  echo "WARNING: QNN envsetup not found at $QNN_SDK_ROOT/bin/envsetup.sh" >&2
  export LD_LIBRARY_PATH="${QNN_SDK_ROOT}/lib/x86_64-linux-clang/:${LD_LIBRARY_PATH:-}"
fi

# QNN host export (x86 libQnnHtp.so) needs NDK LLVM libc++ / libc++abi
NDK_LLVM_LIB="${ANDROID_NDK_ROOT}/toolchains/llvm/prebuilt/linux-x86_64/lib"
LODESTAR_LIBS="${LODESTAR_LIBS:-$HOME/lodestar-libs}"
if [[ -d "$NDK_LLVM_LIB" ]]; then
  export LD_LIBRARY_PATH="${NDK_LLVM_LIB}:${LD_LIBRARY_PATH:-}"
fi
if [[ -f "$LODESTAR_LIBS/libunwind.so.1" ]]; then
  export LD_LIBRARY_PATH="${LODESTAR_LIBS}:${LD_LIBRARY_PATH:-}"
fi

export PYTHONPATH="${EXECUTORCH_ROOT}:${EXECUTORCH_ROOT}/..:${PYTHONPATH}"

# flatc (FlatBuffers compiler) — required for .pte export; built with x86 ExecuTorch
FLATC_BIN="${EXECUTORCH_ROOT}/build-x86/third-party/flatc_ep/bin"
if [[ -x "$FLATC_BIN/flatc" ]]; then
  export PATH="$FLATC_BIN:$PATH"
fi

# Python venv — prefer native Linux path (see fix_venv.sh)
VENV_DIR="${LODESTAR_VENV:-$HOME/lodestar-venv}"
if [[ -f "$VENV_DIR/bin/activate" ]]; then
  # shellcheck disable=SC1091
  source "$VENV_DIR/bin/activate"
elif [[ -f "$RUNTIME_DIR/.venv/bin/activate" ]]; then
  # shellcheck disable=SC1091
  source "$RUNTIME_DIR/.venv/bin/activate"
fi

# adb serial shortcut
if [[ -n "${ADB_SERIAL:-}" ]]; then
  export ADB="adb -s $ADB_SERIAL"
else
  export ADB="adb"
fi

# WSL2: use Windows adb server so USB devices work (WSL cannot see USB natively)
if grep -qi microsoft /proc/version 2>/dev/null; then
  WIN_HOST="$(ip route show default 2>/dev/null | awk '{print $3}')"
  if [[ -z "$WIN_HOST" ]]; then
    WIN_HOST="$(grep nameserver /etc/resolv.conf 2>/dev/null | awk '{print $2}')"
  fi
  if [[ -n "$WIN_HOST" ]]; then
    export ADB_SERVER_SOCKET="tcp:${WIN_HOST}:5037"
  fi
fi

echo "Lodestar env ready:"
echo "  QNN_SDK_ROOT=$QNN_SDK_ROOT"
echo "  ANDROID_NDK_ROOT=$ANDROID_NDK_ROOT"
echo "  EXECUTORCH_ROOT=$EXECUTORCH_ROOT"
echo "  QCOM_CHIPSET=$QCOM_CHIPSET"
