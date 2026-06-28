# STATUS.md — Live Dashboard

> Single source of truth. Updated after every session. **Hackathon clock is running — hours left, not days.**

## DE-RISK GATE
**Stock `.pte` running on NPU + QNN version pinned?  [ ] NO**

---

## PERSON 1 CHECKLIST (Ranji) — update every prompt

### Phase 0 — De-risk (MUST finish first)
- [x] On branch `integrate/lodestar-v1` with `runtime/scripts/`
- [x] QNN SDK downloaded (`v2.37.0.250724.zip`)
- [x] QNN SDK unzipped → `~/qnn/2.37.0/qairt/2.37.0.250724/QNN_README.txt`
- [ ] **`bash runtime/scripts/setup_wsl.sh`** ← **YOU ARE HERE** (NDK + ExecuTorch not installed yet)
- [ ] `source runtime/scripts/envsetup.sh` + `verify_env.sh` all green
- [ ] Phone connected (`adb devices` shows S25 Ultra)
- [ ] `build_executorch.sh` (30–60 min)
- [ ] `export_deeplab.sh` + `run_deeplab_device.sh` (NPU confirmed)
- [ ] DE-RISK GATE flipped to YES

### Phase 1 — Core inference (after gate)
- [ ] Llama 3.2 3B Q4 on NPU + tok/s benchmark
- [ ] Whisper on NPU
- [ ] Real `AiService` wired into android app

### Phase 2 — Harden (if time remains)
- [ ] BGE embedder for RAG
- [ ] Airplane-mode harness + NPU metrics on screen

### Person 2 (already done — do not redo)
- [x] Android app UI + medical + nav + stubs
- [ ] App run once on S25 Ultra in Android Studio (optional but good for demo)

---

## Components
| Component | Owner | Status | Notes | Last updated |
|---|---|---|---|---|
| QNN env + stock `.pte` on NPU | P1 | IN_PROGRESS | QNN unzipped; setup_wsl.sh NOT run yet | 2026-06-28 |
| Llama 3.2 3B (Q4) on NPU | P1 | TODO | After gate | — |
| Whisper-Base/Tiny on NPU | P1 | TODO | After gate | — |
| `AiService` real impl | P1 | IN_PROGRESS | Copilot agent; implementing Android-side real service bridge without touching `MainViewModel.kt` | 2026-06-28 |
| Airplane-mode harness + NPU metrics | P1 | TODO | If time | — |
| BGE-small embedder (for RAG) | P1 | TODO | If time | — |
| Compose shell + status strip | P2 | DONE | | 2026-06-28 |
| **Lodestar UI theme + motion rebuild** | **UI Agent** | **DONE** | `p1/ui-rebuild` — field-green #1A2E1F, bone text, signal-orange alerts, AnimatedContent tabs, spring compass, FieldButton | 2026-06-28 |
| `StubAiService` (canned) | P2 | DONE | | 2026-06-28 |
| Medical corpus + safety tree + nav | P2 | DONE | | 2026-06-28 |
| README + pitch | P2 | DONE | | 2026-06-28 |

## Current Blockers
| Blocker | Fix |
|---|---|
| **setup_wsl.sh not run** | Run now in WSL (needs sudo password) |
| **NDK + ExecuTorch missing** | Fixed by setup_wsl.sh |
| **Phone not verified** | USB debug on → `adb devices` |
