# STATUS.md — Live Dashboard

> Single source of truth for build state. Status values: `TODO` · `IN_PROGRESS` · `BLOCKED` · `DONE`.

## DE-RISK GATE
**Stock `.pte` running on NPU + QNN version pinned?  [ ] NO**
> Nothing custom ships until this is YES. (Person 1 flips it after the first model runs on the NPU and the version is recorded in DECISIONS.md.)

## Components
| Component | Owner | Status | Notes | Last updated |
|---|---|---|---|---|
| QNN env + stock `.pte` on NPU | P1 | IN_PROGRESS | `runtime/` scripts on `integrate/lodestar-v1`; WSL setup pending | 2026-06-28 |
| Llama 3.2 3B (Q4) on NPU | P1 | TODO | 1B fallback if <10 tok/s or throttling | — |
| Whisper-Base/Tiny on NPU | P1 | TODO | From AI Hub | — |
| `AiService` real impl | P1 | TODO | Replaces StubAiService; see expanded interface in DECISIONS.md | — |
| Airplane-mode harness + NPU metrics | P1 | TODO | The 40% evidence | — |
| BGE-small embedder (for RAG) | P1 | TODO | NPU or CPU | — |
| Compose shell + status strip | P2 | DONE | `android/app/.../ui` | 2026-06-28 |
| `StubAiService` (canned) | P2 | DONE | `android/app/.../ai/AiService.kt` | 2026-06-28 |
| Medical first-aid corpus (TCCC/MARCH) | P2 | DONE | 93 chunks in `corpus/` | 2026-06-28 |
| RAG retrieval + grounded prompt | P2 | DONE | Interfaces ready; needs P1 embedder for real vectors | 2026-06-28 |
| Deterministic safety tree | P2 | DONE | 16/16 Python tests pass | 2026-06-28 |
| Voice loop (mic→STT→RAG→LLM→TTS) | P2 | DONE | Wired; stub ASR until P1 | 2026-06-28 |
| Solar compass (heading) | P2 | DONE | 4/4 Python solar sanity checks pass | 2026-06-28 |
| GPS spoof detection + 3-tier fallback | P2 | DONE | Unit tests + demo steps in DEMO.md | 2026-06-28 |
| Translation | P2 | DONE | Bonus screen wired | 2026-06-28 |
| SOS card | P2 | DONE | Bonus screen wired | 2026-06-28 |
| README + MIT license + diagram | P2 | DONE | Root README.md + LICENSE | 2026-06-28 |

## Current Blockers
| Blocker | Owner | What's needed |
|---|---|---|
| WSL env not set up | P1 (human) | Run `bash runtime/scripts/setup_wsl.sh` in interactive WSL |
| QNN SDK not downloaded | P1 (human) | Qualcomm account → QNN 2.37.0 → `~/qnn/2.37.0/` |
| Android app not built on hardware | Both | Open `android/` in Android Studio, run on S25 Ultra |
| Gradle never run locally | P2 verify | `./gradlew test` in `android/` after Android Studio sync |
