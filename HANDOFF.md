# HANDOFF.md — Start here (fresh chat / small context)

> **Repo:** https://github.com/arpan-s-dev/QCOM  
> **Working branch today:** `feature/star-navigation` (latest app) — must merge with `p2/integrate-lodestar-v1` (build+corpus fix) → target `demo/final`  
> **Device:** Galaxy S25 Ultra `R3CXC07ZZWL` — adb **device** (use Windows adb; WSL uses `ADB_SERVER_SOCKET` via `envsetup.sh`)  
> **Person 1 (Ranji):** NPU / runtime / models  

---

## 1. THE IDEA (30 seconds)

**Lodestar** is an **offline survival copilot** for the Qualcomm × Meta ExecuTorch hackathon.

**Scenario:** GPS jammed, network down, airplane mode. User **talks** to the phone. It returns:
- **Which way is true north** (solar + star nav + spoof-aware position)
- **First-aid triage** (deterministic severity + RAG-grounded guidance)

**All inference on Snapdragon NPU.** Zero cloud calls at runtime.

**Not a medical device.** Reference / triage only. Disclaimers on screen.

---

## 2. FEATURES (current scope)

| Tab | Feature | Status |
|-----|---------|--------|
| **TREAT** | Chat + mic → SafetyTree severity → RAG → LLM answer → TTS | Code done; **needs real NPU models** |
| **TREAT** | Field Kit reference sub-tab | Done (`field_kit_reference.json`) |
| **ORIENT** | Solar compass → true-north heading | Done (math verified) |
| **ORIENT** | Night star plate-solve (CV + Yale catalog) | Done on `feature/star-navigation` |
| **ORIENT** | SF hospitals nearest-3 (offline JSON) | Done (`sf_hospitals.json`) |
| **ORIENT** | GPS spoof detection → status strip | Code done; **GPS wire incomplete** |
| **COMMUNICATE** | Translate + SOS summary card | Stub until real LLM |
| **Global** | Status strip (position source pill) | Done (no airplane badge — see DECISIONS) |
| **NPU** | Llama 3.2 3B Q4 + Whisper + BGE | **NOT DONE** |
| **Gate** | Stock DeepLab `.pte` on NPU | **NOT DONE** |

---

## 3. IMPLEMENTATION (architecture)

```
User (voice/text)
    → MainViewModel
    → SafetyTree.evaluate()     ← AUTHORITATIVE severity (not LLM)
    → TriageOrchestrator
        → AiService.embed()       ← BGE (P1)
        → Retriever (corpus)      ← first_aid_corpus.json in APK
        → AiService.generate()    ← Llama on NPU (P1)
    → TextToSpeech

Navigation: LocationManager → SpoofDetector → PositionStateMachine → StatusStrip
            SolarCompass / StarSolver → OrientScreen
            HospitalFinder → sf_hospitals.json
```

**Swap point:** `MainViewModel.kt` — `StubAiService()` → `RealAiService()`  
**Interface:** `android/.../ai/AiService.kt` + `RealAiService.kt` (shell exists, no NPU backend)

**Tech stack:** Kotlin · Jetpack Compose · Material 3 · MVVM · Coroutines · ExecuTorch 1.0 · QNN 2.37.0 · WSL export · on-device `.pte`

---

## 4. DATA — where it comes from (NO training)

| Data | Source | Train? |
|------|--------|--------|
| First-aid corpus | TCCC/MARCH curated JSON | **No** — hand-built chunks |
| SF hospitals | Baked JSON (~18 sites) | **No** |
| Field kit | Reference JSON | **No** |
| Star catalog | Yale Bright Star Catalog | **No** |
| Llama / Whisper / BGE | Qualcomm AI Hub / Meta checkpoints | **No** — download, quantize, export to `.pte` |
| Google Street View | **NOT USED** | N/A — network + licensing |

---

## 5. ENV STATE (what's done)

- [x] QNN `2.37.0.250724` at `~/qnn/2.37.0/qairt/2.37.0.250724/`
- [x] Android NDK 26c
- [x] ExecuTorch v1.0.0 at `~/src/executorch`
- [x] Python venv `~/lodestar-venv` (NOT OneDrive — use `fix_venv.sh` if broken)
- [x] `verify_env.sh` **all passed**
- [x] S25 Ultra adb **device**
- [ ] `build_executorch.sh` — **NOT finished** (run manually in WSL foreground)
- [ ] DE-RISK GATE — **NO**

**WSL commands that work:**
```bash
cd "/mnt/c/Users/ranji/OneDrive/Desktop/QCOM Hackathon/QCOM"
source runtime/scripts/envsetup.sh
bash runtime/scripts/verify_env.sh
bash runtime/scripts/build_executorch.sh   # 30-60 min, keep window open
```

---

## 6. AGENT PLAN — Cursor + Claude + GitHub CLI

**You (Ranji)** = coordinator: USB, passwords, merge approval, airplane-mode demo test.

| Tool | Role | Owns |
|------|------|------|
| **Cursor** | Agent **NPU** | `runtime/`, `android/.../ai/` RealAiService backend, `.pte` export, NPU gate |
| **Claude Code** | Agent **APP** | `android/` (ui, nav, data, gradle), merge branches, device build, UI polish |
| **Claude Code** (2nd chat) | Agent **SHIP** | `README.md`, `DEMO.md`, `STATUS.md`, `docs/`, pitch script |
| **GitHub CLI (`gh`)** | Agent **GIT** (you or scripted) | branches, PRs, merge — **no code** |

### Why this split
- **Cursor** stays on WSL/NPU long builds and ExecuTorch (good terminal integration).
- **Claude** handles large Android tree edits + Gradle merges.
- **`gh`** keeps git clean so AI agents don't fight over push/PR.

### Branch strategy
```
main (stale)
  └── integrate/lodestar-v1
  └── p2/integrate-lodestar-v1     ← build fix + corpus in APK
  └── feature/star-navigation    ← latest features (CURRENT)
  └── TARGET: demo/final           ← merge all here, PR to main
```

---

## 7. COPY-PASTE PROMPTS (new chats)

### Cursor — Agent NPU
```
Read HANDOFF.md, STATUS.md, DECISIONS.md, AGENTS.md.
Repo: github.com/arpan-s-dev/QCOM, branch feature/star-navigation.
I am Person 1 (Ranji). Own runtime/ and android/.../ai/ only.
Phone R3CXC07ZZWL connected. verify_env passed.
Next: build_executorch.sh → export_deeplab → run_deeplab_device (DE-RISK GATE).
Then Llama .pte + RealAiService backend. NO training. NO Street View.
Update STATUS.md + CHANGELOG.md when done. Branch p1/<task>.
```

### Claude — Agent APP
```
Read HANDOFF.md, STATUS.md, AGENTS.md.
Repo: arpan-s-dev/QCOM. Merge p2/integrate-lodestar-v1 + feature/star-navigation → demo/final.
Own android/ except ai/RealAiService NPU backend. Java 17 for Gradle (not Java 25).
gradlew.bat test + installDebug on S25. Wire GPS if time. UI: field-green, bone text, orange alerts.
DO NOT touch runtime/. Update STATUS.md. Branch p1/app-<task>.
```

### Claude — Agent SHIP
```
Read HANDOFF.md, DEMO.md. Own README.md, DEMO.md, STATUS.md, docs/ only.
Write 5-min judge demo script. Fill team names in README. Honest live vs roadmap.
No code except docs. Branch p1/demo-docs.
```

### GitHub CLI — you run
```powershell
cd "C:\Users\ranji\OneDrive\Desktop\QCOM Hackathon\QCOM"
gh auth login
git checkout demo/final
git pull origin demo/final
gh pr create --base main --head demo/final --title "[demo] Lodestar hackathon submission" --body-file PR_BODY.md
gh pr merge --squash   # only when Ranji approves
```

---

## 8. CRITICAL PATH (hours left)

1. **Cursor/NPU:** `build_executorch.sh` → gate on phone  
2. **Claude/APP:** merge → `demo/final` → app installs on S25  
3. **Cursor/NPU:** Llama `.pte` + wire RealAiService  
4. **Claude/SHIP:** DEMO.md + README  
5. **You:** airplane-mode rehearsal  

**Minimum demo:** gate proof + app on phone with SafetyTree + stub LLM + orient/hospitals.  
**Strong demo:** real voice → NPU Llama answer offline.

---

## 9. FILES TO READ (in order)

1. `HANDOFF.md` (this file)  
2. `STATUS.md`  
3. `DECISIONS.md`  
4. `AGENTS.md`  
5. `runtime/README.md`  
6. `DEMO.md`  

---

## 10. KNOWN GOTCHAS

- WSL adb USB: use Windows adb or `ADB_SERVER_SOCKET` in `envsetup.sh`  
- Never `pip install -e executorch` on OneDrive path — use `~/lodestar-venv` + `fix_venv.sh`  
- QNN version pin: `2.37.0.250724` everywhere  
- Java 25 breaks Gradle — use Java 17  
- Branches are split — must merge before demo  
