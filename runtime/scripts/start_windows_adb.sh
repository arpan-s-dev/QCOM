#!/usr/bin/env bash
# Start Windows adb server (USB bridge for WSL). Run from WSL when phone is plugged in.
set -euo pipefail
WIN_PT="/mnt/c/Users/ranji/OneDrive/Desktop/QCOM Hackathon/platform-tools/adb.exe"
if [[ ! -f "$WIN_PT" ]]; then
  echo "ERROR: $WIN_PT not found." >&2
  exit 1
fi
powershell.exe -NoProfile -Command "
  \$pt = 'C:\Users\ranji\OneDrive\Desktop\QCOM Hackathon\platform-tools\adb.exe'
  & \$pt kill-server 2>\$null
  Start-Process -FilePath \$pt -ArgumentList '-a','-P','5037','server','nodaemon' -WindowStyle Hidden
  Start-Sleep -Seconds 2
  & \$pt devices
"
echo ""
echo "Now: source runtime/scripts/envsetup.sh && adb devices"
