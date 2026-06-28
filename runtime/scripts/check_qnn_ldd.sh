#!/usr/bin/env bash
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck disable=SC1091
source "$SCRIPT_DIR/envsetup.sh"
NDK_LLVM="${ANDROID_NDK_ROOT}/toolchains/llvm/prebuilt/linux-x86_64/lib"
export LD_LIBRARY_PATH="${NDK_LLVM}:${QNN_SDK_ROOT}/lib/x86_64-linux-clang:${LD_LIBRARY_PATH:-}"
ldd "${QNN_SDK_ROOT}/lib/x86_64-linux-clang/libQnnHtp.so" | head -12
