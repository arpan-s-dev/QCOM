# STATUS.md — Live Dashboard

> Updated 2026-06-28 (SHIP agent). **Read HANDOFF.md for full context.**

## DE-RISK GATE
**Stock `.pte` on NPU + QNN pinned?  [x] YES** (2026-06-28 — `dl3_qnn_q8.pte` ran on R3CXC07ZZWL, QNN backend, ~4.8ms inference)

---

## MASTER CHECKLIST

### Environment (Person 1)
- [x] QNN 2.37.0.250724 installed
- [x] NDK 26c + ExecuTorch v1.0 cloned
- [x] venv `~/lodestar-venv` (use `runtime/scripts/fix_venv.sh` if broken)
- [x] `verify_env.sh` all passed (re-verified 2026-06-28 in WSL)
- [x] S25 Ultra adb `device` (R3CXC07ZZWL) — verified from the successful on-device gate run
- [x] `build_executorch.sh`
- [x] `export_deeplab.sh` + `run_deeplab_device.sh`
- [x] DE-RISK GATE YES

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
- [x] README team names filled
- [x] DEMO.md 5-min script written
- [ ] Airplane-mode end-to-end on S25

---

## Components

| Component | Status | Notes |
|-----------|--------|-------|
| QNN env + NPU gate | DONE | DeepLab gate passed on device; QNN backend confirmed |
| Llama / Whisper / RealAiService | TODO | After gate |
| Android app features | DONE (code) | star-nav, hospitals, field kit, triage, and demo GPS-spoof fallback control |
| Android build + corpus APK | DONE on `p2/integrate-lodestar-v1` | needs merge to demo/final |
| UI polish / animations | IN_PROGRESS | APP-UI: field-green theme, tab transitions, mic pulse, ORIENT spoof demo UX |
| NPU metrics harness | TODO | If time |

---

## Active agents

| Agent | Tool | Task | Status |
|-------|------|------|--------|
| NPU | Cursor | post-gate models + RealAiService | IN_PROGRESS |
| APP-BUILD | Copilot | merge → demo/final + install | TODO |
| APP-UI | Copilot | Lodestar animations + theme | IN_PROGRESS |
| SHIP | Cursor | README + DEMO + pitch + PR_BODY | DONE |
| GIT | gh (Ranji) | PR merge | TODO |

---

## Blockers

| Blocker | Fix |
|---------|-----|
| Branches split | Claude APP: merge to `demo/final` |
| Gradle Java 25 | Set JAVA_HOME to JDK 17 |
