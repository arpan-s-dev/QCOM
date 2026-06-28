# Lodestar — Setup Guide (Person 1 + Person 2)

**GitHub:** https://github.com/arpan-s-dev/QCOM  
**Working branch:** `integrate/lodestar-v1` (has both `android/` and `runtime/`)

---

## Why your WSL command failed

You were on **`main`**, which only had `track2manjeet.zip` — no `runtime/` folder.

The P1 scripts live on **`integrate/lodestar-v1`**. Pull that branch first:

```bash
cd "/mnt/c/Users/ranji/OneDrive/Desktop/QCOM Hackathon/QCOM"
git fetch origin
git checkout integrate/lodestar-v1
git pull origin integrate/lodestar-v1
ls runtime/scripts    # should list setup_wsl.sh etc.
```

---

## Part A — Android app (Person 2, no WSL needed)

### Option 1: Android Studio (recommended)

1. Install [Android Studio](https://developer.android.com/studio) on Windows.
2. **File → Open** → select the `android/` folder inside the repo.
3. Wait for Gradle sync (downloads SDK automatically).
4. Connect S25 Ultra via USB → enable **USB debugging** in Developer Options.
5. Click **Run** (green play button) or in terminal:
   ```powershell
   cd android
   .\gradlew.bat test
   .\gradlew.bat installDebug
   ```

### Option 2: Verify logic without Android (Python only)

```powershell
cd "C:\Users\ranji\OneDrive\Desktop\QCOM Hackathon\QCOM"
python scripts\verify_safety_tree.py   # expect 16/16 PASS
python scripts\verify_solar_math.py    # expect ALL PASS
```

---

## Part B — NPU / ExecuTorch (Person 1, needs WSL)

### Step 1: Open WSL properly

In **PowerShell** or **Windows Terminal**:
```
wsl
```
You should see `ranji@Vision:~$` — stay in this window (interactive).

### Step 2: Get the right branch

```bash
cd "/mnt/c/Users/ranji/OneDrive/Desktop/QCOM Hackathon/QCOM"
git fetch origin
git checkout integrate/lodestar-v1
git pull origin integrate/lodestar-v1
```

### Step 3: Run setup (needs your password once)

```bash
bash runtime/scripts/setup_wsl.sh
```
This installs: build tools, Android NDK 26c, clones ExecuTorch v1.0.0, creates `runtime/.env`.

**If it says sudo needed:** you're not in an interactive terminal — open Ubuntu from Start Menu instead.

### Step 4: Download QNN SDK (manual — Qualcomm account)

1. Go to https://developer.qualcomm.com/software/qualcomm-ai-engine-direct-sdk
2. Download **QNN 2.37.0** (recommended by ExecuTorch docs)
3. In WSL:
   ```bash
   mkdir -p ~/qnn/2.37.0
   # copy zip from Windows Downloads, e.g.:
   cp /mnt/c/Users/ranji/Downloads/qnn-v2.37.0*.zip ~/
   unzip ~/qnn-v2.37.0*.zip -d ~/qnn/2.37.0
   ```
4. Confirm `~/qnn/2.37.0/QNN_README.txt` exists.
5. Edit env if needed:
   ```bash
   nano runtime/.env   # set QNN_SDK_ROOT=$HOME/qnn/2.37.0
   ```

### Step 5: Connect phone + verify

On Windows: install [USB drivers](https://developer.samsung.com/android-usb-driver) if `adb devices` shows nothing.

In WSL (adb from apt works if phone shows in `adb devices`):
```bash
adb devices          # should list your S25 Ultra
source runtime/scripts/envsetup.sh
bash runtime/scripts/verify_env.sh
```

### Step 6: DE-RISK GATE (long builds)

```bash
source runtime/scripts/envsetup.sh
bash runtime/scripts/build_executorch.sh    # 30-60 min
bash runtime/scripts/export_deeplab.sh
bash runtime/scripts/run_deeplab_device.sh
```

Confirm NPU in logcat:
```bash
adb logcat -d | grep -iE 'qnn|htp|hexagon'
```

Pin QNN version in `DECISIONS.md` → flip DE-RISK GATE in `STATUS.md`.

---

## Part C — Git workflow

```bash
# Always start a session with:
git pull origin main   # or your feature branch

# Person 1 branches: p1/<task>
# Person 2 branches: p2/<task>
# Integration branch: integrate/lodestar-v1

git checkout -b p1/qnn-gate
# ... work ...
git add .
git commit -m "[P1] runtime: <what changed>"
git push -u origin p1/qnn-gate
# Open PR to main on GitHub
```

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| `runtime/scripts: No such file` | Wrong branch — `git checkout integrate/lodestar-v1` |
| `sudo` hangs | Use interactive Ubuntu terminal, not background agent |
| `adb devices` empty | USB debugging on; try Windows `adb` from Android Studio platform-tools |
| Gradle sync fails | Open `android/` in Android Studio; accept SDK licenses |
| QNN Error 5000 | Version mismatch — pin same QNN on build host and device |

---

## What agents cannot do (you must)

- Enter WSL sudo password
- Download QNN SDK (Qualcomm login)
- Plug in phone / airplane mode test
- Merge PRs on GitHub (unless you ask agent to use `gh`)
