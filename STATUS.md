# STATUS.md — Live Dashboard

> Single source of truth. Updated after every session.

## DE-RISK GATE
**Stock `.pte` running on NPU + QNN version pinned?  [ ] NO**

---

## Components
| Component | Status | Notes | Last updated |
|---|---|---|---|
| QNN env + stock `.pte` on NPU | IN_PROGRESS | setup_wsl.sh / pip installing torch | 2026-06-28 |
| Llama / Whisper / RealAiService | TODO | After gate | — |
| **SF hospitals (offline)** | **DONE** | `sf_hospitals.json` + nearest-3 panel on ORIENT | 2026-06-28 |
| **Field-kit reference** | **DONE** | `field_kit_reference.json` + FIELD KIT tab under TREAT | 2026-06-28 |
| **Medical triage (SafetyTree + RAG)** | DONE | SafetyTree authoritative; corpus action-oriented | 2026-06-28 |
| **Status strip** | DONE | Position source pill only — **airplane badge removed** | 2026-06-28 |
| Compose UI + nav | DONE | TREAT / ORIENT / COMMUNICATE | 2026-06-28 |
| NPU metrics / harness | TODO | If time | — |

## Scope notes (2026-06-28)
- Hospitals: **San Francisco only**, baked JSON, approximate cached GPS — label says "near your approximate area"
- Medical: **actions + kit reference** — never drug dosing or prescriptions
- No runtime network calls; no decorative airplane-mode icon

## Current Blockers
| Blocker | Fix |
|---|---|
| setup_wsl / torch install | Wait for pip to finish on WSL |
| Gradle build on Windows | Java 17 + Compose compiler plugin (build agent) |
