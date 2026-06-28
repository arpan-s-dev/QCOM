#!/usr/bin/env bash
# One-time: symlink libunwind.so.8 -> libunwind.so.1 for QNN host libs (no sudo).
set -euo pipefail
LIB_DIR="${LODESTAR_LIBS:-$HOME/lodestar-libs}"
mkdir -p "$LIB_DIR"
if [[ ! -e /lib/x86_64-linux-gnu/libunwind.so.8 ]]; then
  echo "libunwind.so.8 not found — run: sudo apt install libunwind8" >&2
  exit 1
fi
ln -sf /lib/x86_64-linux-gnu/libunwind.so.8 "$LIB_DIR/libunwind.so.1"
echo "==> $LIB_DIR/libunwind.so.1 -> libunwind.so.8"
