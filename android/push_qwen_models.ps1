$ErrorActionPreference = "Stop"
$Src = "Z:\LOca\qwen3-1_7b_ctxt-4096_SM8750"
if (-not (Test-Path $Src)) {
    $Src = Join-Path $PSScriptRoot "..\runtime\models\qwen3-1_7b_ctxt-4096_SM8750"
}
if (-not (Test-Path $Src)) {
    Write-Error "Qwen bundle not found."
}

$Adb = "C:\Users\ranji\OneDrive\Desktop\QCOM Hackathon\platform-tools\adb.exe"
$Remote = "/sdcard/Android/data/com.medic.app/files/models/qwen3-1_7b"
$Tmp = "/data/local/tmp/lodestar_qwen"
& $Adb shell "mkdir -p $Tmp"
Write-Host "Pushing hybrid_llama_qnn.pte (~1.7GB) to /data/local/tmp..."
& $Adb push "$Src\hybrid_llama_qnn.pte" "$Tmp/hybrid_llama_qnn.pte"
& $Adb push "$Src\tokenizer.json" "$Tmp/tokenizer.json"
if (Test-Path "$Src\chat_template.jinja") {
    & $Adb push "$Src\chat_template.jinja" "$Tmp/chat_template.jinja"
}

# App process cannot read adb-pushed files in external app storage (shell uid).
# Copy into internal app-owned storage via run-as.
Write-Host "Installing into app sandbox (app-owned uid, ~1.7GB copy on device)..."
$AppDir = "files/models/qwen3-1_7b"
& $Adb shell "run-as com.medic.app mkdir -p $AppDir"
& $Adb shell "run-as com.medic.app cp $Tmp/hybrid_llama_qnn.pte $AppDir/hybrid_llama_qnn.pte"
& $Adb shell "run-as com.medic.app cp $Tmp/tokenizer.json $AppDir/tokenizer.json"
if (Test-Path "$Src\chat_template.jinja") {
    & $Adb shell "run-as com.medic.app cp $Tmp/chat_template.jinja $AppDir/chat_template.jinja"
}
& $Adb shell "run-as com.medic.app ls -la $AppDir/"
Write-Host "Done. Model installed under app internal storage."
