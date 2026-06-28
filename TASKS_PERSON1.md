# TASKS — PERSON 1 (Runtime + NPU) — Ranji + Cursor

> See **HANDOFF.md** for full context. Mark: `[ ]` TODO · `[~]` IN_PROGRESS · `[x]` DONE

## Phase 0 — De-risk GATE
- [x] P1.0a QNN + NDK + ExecuTorch + venv (`~/lodestar-venv`, `fix_venv.sh`)
- [x] P1.0b envsetup + verify_env.sh passed
- [~] P1.0c build_executorch.sh ← **IN_PROGRESS / NEXT**
- [ ] P1.0d export_deeplab + run_deeplab_device (phone connected R3CXC07ZZWL)
- [ ] P1.0e DE-RISK GATE YES

## Phase 1 — Core inference
- [ ] P1.1 Llama 3.2 3B Q4 → `.pte` → tok/s on device
- [ ] P1.2 1B fallback decision if <10 tok/s
- [ ] P1.3 Whisper on NPU
- [ ] P1.4 RealAiService NPU backend (shell exists; wire after models)

## Phase 2 — If time
- [ ] P1.5 BGE embedder + corpus vectors in APK
- [ ] P1.6 Airplane-mode harness + NPU metrics

**Agent:** Cursor (NPU). **Do not** edit ui/nav unless coordinating with Claude APP.
