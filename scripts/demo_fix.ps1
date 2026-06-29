<#
.SYNOPSIS
  Lodestar live-demo recovery — run from QCOM repo root.

.EXAMPLE
  .\scripts\demo_fix.ps1 -Action check
  .\scripts\demo_fix.ps1 -Action stage
  .\scripts\demo_fix.ps1 -Action relaunch
  .\scripts\demo_fix.ps1 -Action reinstall
  .\scripts\demo_fix.ps1 -Action install-stub
  .\scripts\demo_fix.ps1 -Action logs
#>
param(
    [ValidateSet("check", "stage", "relaunch", "reinstall", "install-stub", "logs")]
    [string]$Action = "check"
)

$ErrorActionPreference = "Stop"
$Root = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path

$Adb    = "C:\Users\ranji\OneDrive\Desktop\QCOM Hackathon\platform-tools\adb.exe"
$Java17 = "C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot"
$Android = Join-Path $Root "android"
$Samples = Join-Path $Root "samples"
$Pkg     = "com.medic.app"
$Activity = "$Pkg/.MainActivity"

function Invoke-Adb {
    param([Parameter(ValueFromRemainingArguments = $true)][string[]]$AdbArgs)
    & $Adb @AdbArgs
    if ($LASTEXITCODE -ne 0) { throw "adb failed: $AdbArgs" }
}

function Test-Device {
    $out = & $Adb devices 2>&1 | Out-String
    if ($out -notmatch "device\s*$") {
        Write-Host "FAIL: No adb device. Plug in S25 and allow USB debugging." -ForegroundColor Red
        return $false
    }
    Write-Host "OK: device connected" -ForegroundColor Green
    return $true
}

function Stage-Samples {
    $files = @(
        "demo_night_sf_treasure_island.jpg",
        "demo_wounded_hand.jpg",
        "demo_sf_powell_street.jpg"
    )
    foreach ($name in $files) {
        $local = Join-Path $Samples $name
        if (Test-Path $local) {
            Invoke-Adb push $local "/sdcard/Download/"
            Write-Host "OK: pushed $name" -ForegroundColor Green
        } else {
            Write-Host "WARN: missing $name - run: python samples\download_demo_scenario_assets.py"
        }
    }
    Get-ChildItem (Join-Path $Samples "demo_night_*.jpg") -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -notin $files } |
        ForEach-Object {
            Invoke-Adb push $_.FullName "/sdcard/Download/"
            Write-Host "OK: pushed $($_.Name)" -ForegroundColor Green
        }
}

function Install-Apk([bool]$StubOnly) {
    if (-not (Test-Path $Java17)) {
        throw "Java 17 not found at $Java17 - Gradle needs JDK 17, not 25."
    }
    $env:JAVA_HOME = $Java17
    $env:PATH = "$Java17\bin;$env:PATH"
    Push-Location $Android
    try {
        if ($StubOnly) {
            Write-Host "Building stub-only APK (NPU chat disabled)..." -ForegroundColor Yellow
            & .\gradlew.bat installDebug "-PenableQnnBackend=false"
        } else {
            Write-Host "Building and installing debug APK..." -ForegroundColor Yellow
            & .\gradlew.bat installDebug
        }
        if ($LASTEXITCODE -ne 0) { throw "gradlew installDebug failed" }
        Write-Host "OK: app installed" -ForegroundColor Green
    } finally {
        Pop-Location
    }
}

function Relaunch-App {
    Invoke-Adb shell am force-stop $Pkg
    Start-Sleep -Seconds 1
    Invoke-Adb shell am start -n $Activity
    Write-Host "OK: app relaunched" -ForegroundColor Green
}

function Show-Check {
    Test-Device | Out-Null
    Write-Host "`n--- Package ---"
    & $Adb shell pm path $Pkg 2>$null
    Write-Host "`n--- Night samples on phone ---"
    & $Adb shell ls -la /sdcard/Download/demo_night* 2>$null
    Write-Host "`n--- Qwen model (optional) ---"
    & $Adb shell "run-as $Pkg ls files/models/qwen3-1_7b/ 2>/dev/null || echo no_model_ok"
    Write-Host "`n--- Safe demo reminder ---"
    Write-Host "  TREAT: TYPE bleeding prompts (no mic)"
    Write-Host "  ORIENT: Night sky -> demo_night_sf_treasure_island.jpg"
    Write-Host "  See docs/DEMO_SAFE_RUNBOOK.md"
}

switch ($Action) {
    "check" {
        Show-Check
    }
    "stage" {
        if (-not (Test-Device)) { exit 1 }
        Stage-Samples
        Relaunch-App
        Show-Check
    }
    "relaunch" {
        if (-not (Test-Device)) { exit 1 }
        Relaunch-App
    }
    "reinstall" {
        if (-not (Test-Device)) { exit 1 }
        Install-Apk -StubOnly $false
        Stage-Samples
        Relaunch-App
    }
    "install-stub" {
        if (-not (Test-Device)) { exit 1 }
        Install-Apk -StubOnly $true
        Relaunch-App
        Write-Host "OK: NPU chat disabled - use typed TREAT + night-sky demo" -ForegroundColor Green
    }
    "logs" {
        if (-not (Test-Device)) { exit 1 }
        Write-Host "Logcat (Ctrl+C to stop) - filter: AiService, SafetyTree, StarNav, AndroidRuntime"
        & $Adb logcat -c
        & $Adb logcat AiServiceFactory:I ExecutorchQwenBackend:W StarNavigationPipeline:D AndroidRuntime:E *:S
    }
}
