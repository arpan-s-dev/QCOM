# DECISIONS.md — Locked Decisions & Gotchas

> Why things are the way they are, so no fresh agent session re-litigates them. Append new gotchas with a date.

## Locked Decisions
| Decision | Choice | Notes |
|---|---|---|
| LLM | **Llama 3.2 3B Instruct, Q4 (SpinQuant/QAT)** | 1B fallback if <10 tok/s / throttling |
| STT | **Whisper-Base or Tiny** (Qualcomm AI Hub) | NPU, w8a16 |
| TTS | **Android native `TextToSpeech`** | Offline; don't waste NPU on it |
| Embedder | **BGE-small** | For medical RAG |
| Runtime | **ExecuTorch 1.0 + Qualcomm QNN backend** | |
| App | **Native Android / Kotlin / Jetpack Compose** | NOT a website |
| Nav (shipping) | **Solar-math heading + GPS spoof detection + dead-reckoning** | |
| Night star plate-solve | **v2 ROADMAP — NOT built** | Pitch as future; don't risk the demo on it |
| Hospital routing / wound vision classifier / QR | **CUT** | Liability / risk / scope |
| Runtime network calls | **NONE** | Must work in airplane mode |
| App package | **`com.medic.app`** | Keep for now; display name **Lodestar** |
| Repo layout | **`android/` + `runtime/` + `corpus/`** | Replaced zip-on-main upload |

## AiService interface (canonical — 2026-06-28)
Person 2 expanded the original two-method sketch. Person 1 implements all methods in `RealAiService` (file TBD under `android/.../ai/`).

| Method | Owner implements | Notes |
|---|---|---|
| `embed(text)` | P1 | BGE-small; must match corpus embedding dim (384) |
| `generate(prompt)` | P1 | Llama 3.2 3B Q4 on NPU |
| `transcribe(audioPcm16)` | P1 | Whisper on NPU; 16 kHz mono PCM |
| `translate(...)` | P1 | Optional; can stub-return until demo-ready |
| `isReady` | P1 | True after models loaded |

## Pinned Versions (Person 1 fills in)
- **QNN SDK version:** `__________` ← FILL IN after download (recommend **2.37.0** per ExecuTorch 1.0 docs; pin exact build string from `QNN_ReleaseNotes.txt`)
- ExecuTorch commit/release: `v1.0.0` (target; record exact tag after clone in `runtime/.env`)
- Android NDK: `26.2.11394342` (r26c)
- FlatBuffers: `__________` (filled by ExecuTorch build)
- **Target SoC:** `SM8750` (Snapdragon 8 Elite — Galaxy S25 Ultra)

## Gotchas (append as discovered)
- `2026-06-28` | QNN version mismatch (Error 5000 "Qnn API version mismatched") is the top failure mode — pin versions across build + device.
- `2026-06-28` | repo | Person 2 uploaded `track2manjeet.zip` to main instead of source tree — extracted to `android/` on `integrate/lodestar-v1`.
- `2026-06-28` | WSL | `runtime/` only exists on integration branch, not old `main` — `git pull` + checkout correct branch before running setup scripts.
- `2026-06-28` | android | `./gradlew.bat testDebugUnitTest` currently fails during configuration because Kotlin 2.0 + Compose now requires the Compose compiler Gradle plugin in the existing build scripts.
