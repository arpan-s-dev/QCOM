# DEMO_SAFE_RUNBOOK.md — Live demo if the model fails

> **Keep this open on your laptop during the pitch.**  
> Full judge script: [`DEMO.md`](../DEMO.md) · Sample photos: [`samples/README.md`](../samples/README.md)

---

## Full image + prompt scenario (Powell St + wounded hand)

See **[`samples/DEMO_SCENARIO.md`](../samples/DEMO_SCENARIO.md)** — injury photo, SF street photo, exact GPS, hospital bearing answer.

```powershell
python samples\download_demo_scenario_assets.py
.\scripts\demo_fix.ps1 -Action stage
```

Mock GPS for hospital demo: `37.789261, -122.408653` (Powell & Sutter, SF)

---

## Golden rule

**SafetyTree + ORIENT + hospitals work with zero NPU.**  
If Llama / Qwen / Whisper fails, **type** triage text (do not use mic), show **CRITICAL vs SERIOUS**, then run the **night-sky photo** path. Say once:

> *"The safety decision is deterministic and live; the fluent LLM answer is stubbed until our on-device model path is stable."*

That is honest and still scores the architecture.

---

## Safe demo path (no model required)

| Step | Tab | Action | What judges see |
|------|-----|--------|-----------------|
| 1 | — | Airplane mode **ON** | Offline |
| 2 | TREAT | **Type** (don't mic): `There's a lot of blood on his leg and it hasn't stopped.` | **CRITICAL** |
| 3 | TREAT | **Type**: `The bleeding has stopped now, we got a bandage on it.` | **SERIOUS** (negation contrast) |
| 4 | ORIENT | **Night sky** → import `demo_night_sf_treasure_island.jpg` | **STAR_FIX**, heading ~47° |
| 5 | ORIENT | Open hospitals list | Nearest 3 SF hospitals offline |
| 6 | — | Close on status strip | Offline copilot story |

**Skip:** mic, spoof demo, COMMUNICATE translate (all stub-heavy).

---

## Pre-stage (30 min before)

Run from repo root in PowerShell:

```powershell
cd "C:\Users\ranji\OneDrive\Desktop\QCOM Hackathon\QCOM"
.\scripts\demo_fix.ps1 -Action stage
```

Or manually:

```powershell
$ADB = "C:\Users\ranji\OneDrive\Desktop\QCOM Hackathon\platform-tools\adb.exe"
$S   = "C:\Users\ranji\OneDrive\Desktop\QCOM Hackathon\QCOM\samples"

& $ADB push "$S\demo_night_sf_treasure_island.jpg" /sdcard/Download/
& $ADB shell am force-stop com.medic.app
& $ADB shell am start -n com.medic.app/.MainActivity
```

**Checklist**

- [ ] Phone `R3CXC07ZZWL` shows `device` in `adb devices`
- [ ] Airplane mode ON; Wi‑Fi/cell OFF
- [ ] Mic, location, storage permissions granted
- [ ] `demo_night_sf_treasure_island.jpg` in Downloads
- [ ] App opened once (warm start)

---

## Failure matrix — say this + run this

### Model / LLM / chat hangs or crashes

**Say:** *"We keep the safety tree authoritative; I'll show classification with typed input while the NPU path recovers."*

```powershell
.\scripts\demo_fix.ps1 -Action relaunch
```

Then **type** the bleeding prompts — do **not** tap mic. If it crashes again on send:

```powershell
.\scripts\demo_fix.ps1 -Action install-stub
```

Rebuilds with NPU chat **disabled** (`enableQnnBackend=false`). SafetyTree + star nav still work.

---

### Mic shows `[STUB TRANSCRIPT]` or garbage

**Expected** if Whisper is not wired. **Do not use mic on stage.**

**Say:** *"Voice is our NPU stretch goal; typed triage is the reliable path today."*

**Fix:** Type prompts from [`samples/test_prompts.txt`](../samples/test_prompts.txt).

---

### Stub answer text under CRITICAL label

**Expected** with `StubAiService`. The **severity pill** is the demo — not the paragraph.

**Say:** *"The CRITICAL label comes from our deterministic safety tree, not the language model."*

Optional — trigger a slightly richer stub (still not real LLM):

```
Tell me about severe bleeding and pressure
```

---

### Night-sky import fails / low stars

**Say:** *"We ship hash-matched demo assets for the field test photo."*

```powershell
.\scripts\demo_fix.ps1 -Action stage
```

Pick **`demo_night_sf_treasure_island.jpg`** only (not random gallery photos).

**If UI still fails — narrate without faking taps:**

> *"This staged Treasure Island photo resolves to STAR_FIX, true north about 47 degrees, using our bundled bright-star catalog offline."*

Expected: heading **47°**, lat **37.81°**, message mentions Bay Bridge / Treasure Island.

---

### Sun / solar path unavailable

**Say:** *"Solar needs line of sight; indoors we use the star-field photo path."*

Switch to **ORIENT → Night sky** + staged JPG (above).

---

### App frozen / black screen

```powershell
.\scripts\demo_fix.ps1 -Action relaunch
```

If still bad:

```powershell
.\scripts\demo_fix.ps1 -Action reinstall
```

Uses **Java 17** (Java 25 breaks Gradle).

---

### Wrong APK / old star hashes

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot"
cd "C:\Users\ranji\OneDrive\Desktop\QCOM Hackathon\QCOM\android"
.\gradlew.bat installDebug
```

---

### Model was supposed to work but doesn't

**Check** model files on device:

```powershell
$ADB = "C:\Users\ranji\OneDrive\Desktop\QCOM Hackathon\platform-tools\adb.exe"
& $ADB shell "run-as com.medic.app ls -la files/models/qwen3-1_7b/"
```

**Re-push** (takes several minutes — not for stage, only back room):

```powershell
cd "C:\Users\ranji\OneDrive\Desktop\QCOM Hackathon\QCOM\android"
.\push_qwen_models.ps1
```

Then relaunch app and try **one** chat message. If crash → `install-stub` and stay on safe path.

---

### GPS / hospitals empty

**Say:** *"Hospitals use last-known approximate position; GPS in airplane mode can still read cached fixes."*

Toggle airplane mode OFF for 10 s, wait for fix, airplane ON again — or skip hospitals and keep triage + stars.

---

### Spoof demo not ready

**Say:** *"We validated spoof detection in unit tests; skipping the live trigger to stay in time."*

Do not improvise mock-location on stage.

---

## Copy-paste triage lines (safe)

```
There's a lot of blood on his leg and it hasn't stopped.
The bleeding has stopped now, we got a bandage on it.
Blood is spurting out of his leg in time with his heartbeat
There's a third degree burn covering his forearm
Just a small cut on my finger from the knife
```

---

## Backstage debug (not on stage)

```powershell
.\scripts\demo_fix.ps1 -Action check
.\scripts\demo_fix.ps1 -Action logs
```

---

## One-liner close (always works)

> *"You saw offline triage with deterministic severity, offline heading from a star-field photo, and offline hospital reference — all in airplane mode, with one interface ready to swap in Snapdragon NPU generation when the model path is green."*
