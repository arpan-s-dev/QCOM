# STATUS.md — Live Dashboard

> Updated 2026-06-28 (Copilot APP agent). **Read HANDOFF.md for full context.**

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
- [x] Java 17 for Gradle (not Java 25)
- [x] `gradlew.bat installDebug` on S25
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
| Component | Owner | Status | Notes | Last updated |
|---|---|---|---|---|
| QNN env + stock `.pte` on NPU | P1 | IN_PROGRESS | `runtime/` scripts on `integrate/lodestar-v1`; WSL setup pending | 2026-06-28 |
| Llama 3.2 3B (Q4) on NPU | P1 | TODO | 1B fallback if <10 tok/s or throttling | — |
| Whisper-Base/Tiny on NPU | P1 | TODO | From AI Hub | — |
| `AiService` real impl | P1 | TODO | Replaces StubAiService; see expanded interface in DECISIONS.md | — |
| Airplane-mode harness + NPU metrics | P1 | TODO | The 40% evidence | — |
| BGE-small embedder (for RAG) | P1 | TODO | NPU or CPU | — |
| Android gradle test | P2 | DONE | PASS — `gradlew.bat test` succeeds with JDK 17 on `p1/app-crunch` | 2026-06-28 |
| Android installDebug on S25 Ultra | P2 | DONE | PASS — installed to `R3CXC07ZZWL` with `gradlew.bat installDebug` | 2026-06-28 |
| Bundled corpus asset loader | P2 | DONE | `first_aid_corpus.json` bundled in app assets; `CorpusLoader` populates `MainViewModel` on init | 2026-06-28 |
| Compose shell + status strip | P2 | DONE | `android/app/.../ui` | 2026-06-28 |
| `StubAiService` (canned) | P2 | DONE | `android/app/.../ai/AiService.kt` | 2026-06-28 |
| Medical first-aid corpus (TCCC/MARCH) | P2 | DONE | 93 chunks in `corpus/` | 2026-06-28 |
| RAG retrieval + grounded prompt | P2 | DONE | Interfaces ready; needs P1 embedder for real vectors | 2026-06-28 |
| Deterministic safety tree | P2 | DONE | 16/16 Python tests pass | 2026-06-28 |
| Voice loop (mic→STT→RAG→LLM→TTS) | P2 | DONE | Wired; stub ASR until P1 | 2026-06-28 |
| Solar compass (heading) | P2 | DONE | 4/4 Python solar sanity checks pass | 2026-06-28 |
| GPS spoof detection + 3-tier fallback | P2 | DONE | `LocationManager` now feeds `updateRoughLocation()` and `SpoofDetector` updates the status strip | 2026-06-28 |
| Translation | P2 | DONE | Bonus screen wired | 2026-06-28 |
| SOS card | P2 | DONE | Bonus screen wired | 2026-06-28 |
| README + MIT license + diagram | P2 | DONE | Root README.md + LICENSE | 2026-06-28 |

## Current Blockers
| Blocker | Owner | What's needed |
|---|---|---|
| WSL env not set up | P1 (human) | Run `bash runtime/scripts/setup_wsl.sh` in interactive WSL |
| QNN SDK not downloaded | P1 (human) | Qualcomm account → QNN 2.37.0 → `~/qnn/2.37.0/` |
