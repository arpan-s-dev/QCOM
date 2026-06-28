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

## Pinned Versions (Person 1 fills in)
- **QNN SDK version:** `__________` ← FILL IN, this is critical
- ExecuTorch commit/release: `__________`
- Android NDK: `26c`
- FlatBuffers: `__________`

## Gotchas (append as discovered)
- `2026-06-28` | QNN version mismatch (Error 5000 "Qnn API version mismatched") is the top failure mode — pin versions across build + device.
- _(add new gotchas here: date | component | what bit us | the fix)_
