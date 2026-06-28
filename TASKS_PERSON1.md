# TASKS — PERSON 1 (Runtime + On-Device Inference)

> You own the chip. Get the LLM + Whisper on the NPU, expose clean Kotlin functions, own the airplane-mode harness + metrics. **You unblock Person 2 — but you are not blocked by them** (they build on stubs).
> Mark each: `[ ]` TODO · `[~]` IN_PROGRESS · `[x]` DONE. Update STATUS.md + CHANGELOG.md on finish.

## Phase 0 — De-risk FIRST (before any custom code)
- [~] **P1.0a** WSL env + NDK + QNN SDK. QNN **done** (`2.37.0.250724`). **Still need:** `setup_wsl.sh` for NDK + ExecuTorch.
- [ ] **P1.0b** Set env vars: `QNN_SDK_ROOT`, `LD_LIBRARY_PATH`, `ADSP_LIBRARY_PATH`.
- [ ] **P1.0c** Export a stock **MobileNetV2 / DeepLabV3** to `.pte` via `QnnPartitioner` + Qualcomm quantizer (INT8).
- [ ] **P1.0d** Push `.pte` + QNN HTP skel/stub libs (`libQnnHtpV79Skel.so`) via adb; run on device; **confirm NPU execution (not CPU fallback).**
- [ ] **P1.0e** ✅ **Flip the DE-RISK GATE to YES in STATUS.md.**

## Phase 1 — Core inference
- [ ] **P1.1** Export/quantize **Llama 3.2 3B Q4** (SpinQuant/QAT checkpoint, static seq-len, `--model_mode kv`) → `.pte` → run on device → **print prefill + decode tok/s.**
- [ ] **P1.2** Decision: if **<10 tok/s or hard throttling → swap to Llama 3.2 1B.** Record choice in DECISIONS.md.
- [ ] **P1.3** **Whisper-Base/Tiny** from AI Hub on NPU: mic → 30s chunk → mel → encoder (NPU) → decoder → text. Print latency for a 30s clip.
- [~] **P1.4** Implement real `AiService`: `transcribe(audioPath): String`, `generate(prompt): String`. Swap it in for `StubAiService`. *(Copilot agent: Android bridge + tests done; runtime backend + swap still pending)*

## Phase 2 — Support + harden
- [ ] **P1.5** Run **BGE-small embedder** for Person 2's RAG (NPU or CPU). Expose an embed function.
- [ ] **P1.6** Build the **airplane-mode harness**: a test screen that runs `transcribe()` + `generate()` end-to-end with all radios OFF and asserts success.
- [ ] **P1.7** **NPU metrics:** log + display per-inference latency and a battery-delta energy estimate (the 40% Technical evidence).
- [ ] **P1.8** **Fallback ready:** if QNN export fights the LLM → run LLM on XNNPACK CPU, but **keep Whisper on NPU** to protect the NPU score. Note in DECISIONS.md if used.

## Definition of Done (your lane)
App, in airplane mode, takes voice → transcribes on NPU → generates on NPU → returns text, with latency/energy visible. `AiService` real impl merged. GATE flipped. DECISIONS.md has the pinned QNN version + final model choices.
