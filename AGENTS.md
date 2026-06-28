# AGENTS.md — Rules for Cursor · Claude Code · GitHub CLI

> **Fresh session? Read `HANDOFF.md` first.** Then STATUS.md + your task file.

## Project
**Lodestar** — OFFLINE on-device AI survival copilot. Qualcomm × Meta ExecuTorch Hackathon. Galaxy S25 Ultra (SM8750). ExecuTorch 1.0 + QNN. Kotlin / Jetpack Compose.

## Hard Constraints
- **AIRPLANE MODE** — zero cloud/API at runtime. Local `.pte` only.
- **Pin QNN** `2.37.0.250724` (see DECISIONS.md).
- **Medical = triage/reference only.** SafetyTree authoritative over LLM.
- **Nav = heading**, not survey-grade GPS. SF hospitals = offline JSON only.
- **NO model training.** Download + quantize + export only.
- **NO Google Street View** or network map APIs.

## Tool Assignments (2026-06-28)

| Tool | Agent | Scope |
|------|-------|--------|
| **Cursor** | NPU | `runtime/`, `android/.../ai/` (RealAiService + backend) |
| **GitHub Copilot CLI** | APP-BUILD / APP-UI / APP-NAV / SHIP | See **`COPILOT_AGENTS.md`** for prompts |
| **GitHub CLI (`gh`)** | GIT | PRs, merge — **Ranji runs** |

## Workflow (every AI agent)
1. `git pull origin demo/final` (or agreed integration branch)
2. Read HANDOFF.md + STATUS.md
3. Claim ONE task → mark `IN_PROGRESS` in STATUS.md
4. Work only in owned directories
5. Commit: `[P1] <area>: <change>` on branch `p1/<task>`
6. Update STATUS.md + CHANGELOG.md; gotchas → DECISIONS.md
7. **Do not merge to main** — open PR; Ranji merges via `gh`

## Ownership Map

| Path | Owner |
|------|--------|
| `runtime/` | Cursor (NPU) |
| `android/.../ai/` RealAiService backend | Cursor (NPU) |
| `android/.../ui/`, `nav/`, `data/` | Claude (APP) |
| `corpus/`, `scripts/` (verify) | Claude (APP) |
| `README.md`, `DEMO.md`, `docs/` | Claude (SHIP) |
| `HANDOFF.md`, `STATUS.md`, git/PRs | All update; **gh** merges |

## AiService (canonical — see DECISIONS.md)
```kotlin
val isReady: Boolean
suspend fun embed(text: String): FloatArray
suspend fun generate(prompt: String): String
suspend fun transcribe(audioPcm16: ShortArray): String
suspend fun translate(text: String, fromLang: String, toLang: String): String
```
App uses `StubAiService()` until Cursor lands NPU backend in `RealAiService`.

## Integration branch
Target: **`demo/final`** = `feature/star-navigation` + `p2/integrate-lodestar-v1` merged.
