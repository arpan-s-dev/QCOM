# STATUS.md — Live Dashboard

> Updated 2026-06-28 (Cursor NPU agent). **Read HANDOFF.md for full context.**

## DE-RISK GATE
**Stock `.pte` on NPU + QNN pinned?  [ ] NO**

---

## MASTER CHECKLIST

### Environment (Person 1)
- [x] QNN 2.37.0.250724 installed
- [x] NDK 26c + ExecuTorch v1.0 cloned
- [x] venv `~/lodestar-venv` (use `runtime/scripts/fix_venv.sh` if broken)
- [x] `verify_env.sh` all passed (re-verified 2026-06-28 in WSL)
- [ ] S25 Ultra adb `device` (R3CXC07ZZWL) — **Windows adb** (WSL sees 0 devices; plug USB + confirm in Windows)
- [ ] **`build_executorch.sh`** ← **IN PROGRESS** (WSL PID ~5892; log `~/lodestar-build.log`)
- [ ] `export_deeplab.sh` + `run_deeplab_device.sh`
- [ ] DE-RISK GATE YES

### Integration (Claude APP + gh)
- [ ] Merge `p2/integrate-lodestar-v1` + `feature/star-navigation` → **`demo/final`**
- [ ] Java 17 for Gradle (not Java 25)
- [ ] `gradlew.bat installDebug` on S25
- [ ] PR `demo/final` → `main` via `gh`

### Models (Cursor NPU — after gate)
- [ ] Llama 3.2 3B Q4 `.pte` on NPU
- [ ] Whisper on NPU
- [ ] RealAiService backend wired
- [ ] BGE embedder + corpus vectors (if time)

### Demo (Claude SHIP + Ranji)
- [ ] README team names filled
- [ ] DEMO.md 5-min script rehearsed
- [ ] Airplane-mode end-to-end on S25

---

## Components

| Component | Status | Notes |
|-----------|--------|-------|
| QNN env + NPU gate | IN_PROGRESS | verify_env OK; build_executorch running (CMake configure phase) |
| Llama / Whisper / RealAiService | TODO | After gate |
| Android app features | DONE (code) | star-nav, hospitals, field kit, triage |
| Android build + corpus APK | DONE on `p2/integrate-lodestar-v1` | needs merge to demo/final |
| UI polish / animations | TODO | Claude APP after merge |
| NPU metrics harness | TODO | If time |

---

## Active agents

| Agent | Tool | Task | Status |
|-------|------|------|--------|
| NPU | Cursor | build_executorch + gate | IN_PROGRESS |
| APP | Claude | merge → demo/final + install | TODO |
| SHIP | Claude | README + DEMO | TODO |
| GIT | gh (Ranji) | PR merge | TODO |

---

## Blockers

| Blocker | Fix |
|---------|-----|
| `build_executorch` running (~30–60 min) | Monitor: `wsl tail -f ~/lodestar-build.log` |
| Branches split | Claude APP: merge to `demo/final` |
| Gradle Java 25 | Set JAVA_HOME to JDK 17 |
