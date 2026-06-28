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
& $Adb shell "mkdir -p $Remote"
Write-Host "Pushing hybrid_llama_qnn.pte (~1.7GB)..."
& $Adb push "$Src\hybrid_llama_qnn.pte" "$Remote/hybrid_llama_qnn.pte"
& $Adb push "$Src\tokenizer.json" "$Remote/tokenizer.json"
if (Test-Path "$Src\chat_template.jinja") {
    & $Adb push "$Src\chat_template.jinja" "$Remote/chat_template.jinja"
}
Write-Host "Done. Model on device."
