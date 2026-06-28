# Copilot Agent Prompts — copy one block per Copilot CLI session

> Repo: https://github.com/arpan-s-dev/QCOM  
> Read **HANDOFF.md** + **STATUS.md** before any work.  
> Integration target branch: **`demo/final`**  
> Current feature branch: **`feature/star-navigation`**  
> Build fix branch: **`p2/integrate-lodestar-v1`**

---

## Before every Copilot session

```bash
cd "/mnt/c/Users/ranji/OneDrive/Desktop/QCOM Hackathon/QCOM"
git fetch origin
git pull origin feature/star-navigation   # or demo/final when it exists
```

**Ranji (human):** merge PRs, USB phone, airplane-mode test — not Copilot.

**Cursor (separate IDE):** NPU / runtime only — do not duplicate in Copilot.

---

## Copilot Agent 1 — APP-BUILD (merge + Gradle + device)

```
You are Copilot Agent APP-BUILD for Lodestar (Qualcomm ExecuTorch hackathon).

READ FIRST: HANDOFF.md, STATUS.md, AGENTS.md, DECISIONS.md

REPO: github.com/arpan-s-dev/QCOM
YOUR BRANCH: p1/app-build (create from feature/star-navigation)

YOUR JOB ONLY:
1. Merge origin/p2/integrate-lodestar-v1 into feature/star-navigation → create demo/final
2. Fix Android build: Java 17 (NOT Java 25), Compose compiler plugin for Kotlin 2.0
3. Ensure android/app/src/main/assets/first_aid_corpus.json exists + CorpusLoader wired
4. Run: cd android && ./gradlew.bat test && ./gradlew.bat installDebug
5. Phone target: Samsung S25 Ultra (already adb authorized)

YOU OWN: android/build.gradle*, android/app/build.gradle*, settings.gradle.kts,
         android/gradle/, res/, assets/, android/.../data/CorpusLoader*
DO NOT TOUCH: runtime/, android/.../ai/RealAiService NPU backend, ui/theme/ (Agent 2)

CONSTRAINTS: No network APIs at runtime. No model training. Native Android only.

ON FINISH:
- Commit: [P1] android: <what>
- Update STATUS.md + CHANGELOG.md
- Push p1/app-build, tell Ranji to gh pr create → demo/final
```

---

## Copilot Agent 2 — APP-UI (theme + motion)

```
You are Copilot Agent APP-UI for Lodestar.

READ FIRST: HANDOFF.md, STATUS.md, AGENTS.md

REPO: github.com/arpan-s-dev/QCOM
YOUR BRANCH: p1/ui-rebuild (from demo/final when APP-BUILD merge lands, else feature/star-navigation)

YOUR JOB ONLY:
Rebuild Lodestar field-instrument UI with fluid animations.

DESIGN:
- Background: field-green #1A2E1F
- Text: bone #E8E4D9
- Alerts / CRITICAL / mic recording: signal-orange #FF6B2B
- Monospace status labels, smooth tab transitions

IMPLEMENT:
- AnimatedContent on TREAT / ORIENT / COMMUNICATE tab switch
- Chat message enter animations + animateItemPlacement on LazyColumn
- StatusStrip: smooth color transition on position source change
- Mic button: orange pulse when recording
- OrientScreen: spring-animated compass needle
- Replace default Material buttons with themed FieldButton

YOU OWN: android/app/src/main/java/com/medic/app/ui/** only
         android/app/src/main/java/com/medic/app/ui/theme/**
DO NOT TOUCH: runtime/, ai/RealAiService, data/CorpusLoader, build.gradle, nav/ (Agent 3)

ON FINISH: [P1] ui: <what> + STATUS.md + CHANGELOG.md
```

---

## Copilot Agent 3 — APP-NAV (GPS + spoof + status strip)

```
You are Copilot Agent APP-NAV for Lodestar.

READ FIRST: HANDOFF.md, STATUS.md, DECISIONS.md

REPO: github.com/arpan-s-dev/QCOM
YOUR BRANCH: p1/nav-wire (from demo/final or feature/star-navigation)

YOUR JOB ONLY:
Wire real navigation state to the UI.

TASKS:
1. LocationManager → MainViewModel.updateRoughLocation() + updatePositionState()
2. SpoofDetector on GPS jumps → status strip shows SPOOF + fast pulse
3. PositionStateMachine: GPS_TRUSTED → DEAD_RECKONING → SOLAR_FIX on OrientScreen
4. SF hospitals panel: HospitalFinder uses cached approximate GPS from step 1
5. Keep star-nav + solar math working (nav/star/, SolarCompass.kt)

YOU OWN: android/.../nav/**, minimal hooks in MainViewModel for GPS only
DO NOT TOUCH: ui/theme/ (Agent 2), runtime/, ai/RealAiService, build.gradle (Agent 1)

MainViewModel: only add GPS/location methods — do NOT change aiService = StubAiService() line.

ON FINISH: [P1] nav: <what> + STATUS.md. Run ./gradlew test.
```

---

## Copilot Agent 4 — SHIP (docs + demo script)

```
You are Copilot Agent SHIP for Lodestar hackathon submission.

READ FIRST: HANDOFF.md, DEMO.md, STATUS.md, docs/PITCH_OUTLINE.md

REPO: github.com/arpan-s-dev/QCOM
YOUR BRANCH: p1/demo-docs

YOUR JOB ONLY — DOCS, NO APP CODE:
1. README.md: fill [Person 1 Name] / [Person 2 Name] / emails, MIT license note
2. DEMO.md: 5-minute judge walkthrough:
   - Airplane mode ON
   - TREAT: "bleeding hasn't stopped" → CRITICAL + triage answer
   - ORIENT: solar heading OR star nav demo
   - SF hospitals nearest-3
   - Optional: GPS spoof flip on status strip
3. docs/PITCH_OUTLINE.md: honest "live vs roadmap" (NPU Llama if not ready)
4. Keep STATUS.md checklist current

YOU OWN: README.md, DEMO.md, docs/, STATUS.md, CHANGELOG.md (docs lines only)
DO NOT TOUCH: android/, runtime/

Be honest: stub LLM vs real NPU — judges prefer honesty.
ON FINISH: [P1] docs: <what>
```

---

## Cursor Agent — NPU (NOT Copilot — separate IDE chat)

```
Read HANDOFF.md, STATUS.md, DECISIONS.md.
Person 1 / Ranji. Cursor = NPU agent ONLY.

Branch: feature/star-navigation (or demo/final)
Phone: R3CXC07ZZWL adb device. verify_env passed.

Own: runtime/ + android/.../ai/ RealAiService NPU backend ONLY.

NEXT:
1. bash runtime/scripts/build_executorch.sh  (WSL, 30-60 min)
2. export_deeplab.sh + run_deeplab_device.sh → DE-RISK GATE
3. Llama 3.2 3B Q4 .pte on NPU
4. Whisper + wire RealAiService

NO training. NO ui/nav/data edits. Update STATUS.md + CHANGELOG.md.
```

---

## Ranji — GitHub CLI (not AI)

```powershell
cd "C:\Users\ranji\OneDrive\Desktop\QCOM Hackathon\QCOM"
gh auth login
git fetch origin
git checkout demo/final
git pull origin demo/final

# After each Copilot agent pushes:
gh pr create --base demo/final --head p1/app-build --title "[P1] android: build merge"
gh pr list
gh pr merge <number> --squash

# Final submission:
gh pr create --base main --head demo/final --title "Lodestar hackathon submission"
```

---

## Run order (recommended)

| Order | Agent | Can parallel? |
|-------|-------|----------------|
| 1 | **Cursor NPU** | start build_executorch now |
| 2 | **Copilot APP-BUILD** | yes, parallel with Cursor |
| 3 | **Copilot APP-NAV** | after APP-BUILD merges |
| 4 | **Copilot APP-UI** | after APP-BUILD merges |
| 5 | **Copilot SHIP** | anytime |
| 6 | **gh (Ranji)** | merge each PR when green |

---

## Folder lock (prevent collisions)

| Path | Agent |
|------|--------|
| `runtime/` | Cursor NPU only |
| `android/.../ai/` backend | Cursor NPU only |
| `android/` gradle, assets, data | Copilot APP-BUILD |
| `android/.../ui/` | Copilot APP-UI |
| `android/.../nav/` | Copilot APP-NAV |
| `README`, `DEMO`, `docs/` | Copilot SHIP |
