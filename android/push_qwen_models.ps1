param(
    [ValidateSet("0.6b", "1.7b")]
    [string]$Model = "0.6b"
)
$ErrorActionPreference = "Stop"

$Wsl06 = "\\wsl.localhost\Ubuntu\home\ranji\lodestar-models\qwen3-0_6b_sm8750_export"
$Repo06 = Join-Path $PSScriptRoot "..\runtime\models\qwen3-0_6b_sm8750"
$Repo17 = Join-Path $PSScriptRoot "..\runtime\models\qwen3-1_7b_ctxt-4096_SM8750"
$Z17 = "Z:\LOca\qwen3-1_7b_ctxt-4096_SM8750"

if ($Model -eq "0.6b") {
    $Subdir = "qwen3-0_6b"
    $Src = if (Test-Path $Wsl06) { $Wsl06 } elseif (Test-Path $Repo06) { $Repo06 } else { $null }
    if (-not $Src) {
        Write-Error "Qwen 0.6B bundle not found. Run runtime/scripts/export_qwen06_sm8750.sh in WSL first."
    }
} else {
    $Subdir = "qwen3-1_7b"
    $Src = if (Test-Path $Z17) { $Z17 } elseif (Test-Path $Repo17) { $Repo17 } else { $null }
    if (-not $Src) {
        Write-Error "Qwen 1.7B bundle not found."
    }
}

$Adb = "C:\Users\ranji\OneDrive\Desktop\QCOM Hackathon\platform-tools\adb.exe"
$Tmp = "/data/local/tmp/lodestar_qwen"
& $Adb shell "mkdir -p $Tmp"
Write-Host "Pushing $Model model from $Src ..."
& $Adb push "$Src\hybrid_llama_qnn.pte" "$Tmp/hybrid_llama_qnn.pte"
& $Adb push "$Src\tokenizer.json" "$Tmp/tokenizer.json"
if (Test-Path "$Src\chat_template.jinja") {
    & $Adb push "$Src\chat_template.jinja" "$Tmp/chat_template.jinja"
}

Write-Host "Installing into app sandbox (run-as com.medic.app) ..."
$AppDir = "files/models/$Subdir"
& $Adb shell "run-as com.medic.app mkdir -p $AppDir"
& $Adb shell "run-as com.medic.app cp $Tmp/hybrid_llama_qnn.pte $AppDir/hybrid_llama_qnn.pte"
& $Adb shell "run-as com.medic.app cp $Tmp/tokenizer.json $AppDir/tokenizer.json"
if (Test-Path "$Src\chat_template.jinja") {
    & $Adb shell "run-as com.medic.app cp $Tmp/chat_template.jinja $AppDir/chat_template.jinja"
}
& $Adb shell "run-as com.medic.app ls -la $AppDir/"
Write-Host "Done. $Model model installed at app internal storage/$Subdir"
