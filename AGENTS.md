# AGENTS.md — Rules for Every Agent (Cursor · Claude Code · Copilot)

> **Read this + STATUS.md + your TASKS file before writing any code.** Update them when you finish.

## Project
**Lodestar** — an OFFLINE, on-device AI survival copilot. Qualcomm × Meta ExecuTorch Hackathon. Target: Galaxy S25 Ultra (Snapdragon 8 Elite, Hexagon NPU). Runtime: ExecuTorch 1.0 + Qualcomm QNN backend. App: native Android / Kotlin / Jetpack Compose. Team: 2 people.

## Hard Constraints (never violate)
- **AIRPLANE MODE. ZERO cloud LLM/API calls at runtime.** All inference is local `.pte` / AI Hub assets. AI coding tools are for *building*, never *running*.
- **Pin the QNN SDK version** (recorded in DECISIONS.md) across build + device. Version mismatch is the #1 time-sink.
- **Medical = first-aid REFERENCE / TRIAGE only**, never diagnosis. The deterministic safety tree is authoritative over the LLM.
- **Navigation = true-north HEADING, not GPS-grade lat/long.** Don't over-promise position accuracy.
- **Native Android, not a website.** A browser demo throws away the NPU score.
- **Night star plate-solve = v2 ROADMAP, not built.** Ship solar-math heading + spoof detection.

## Workflow Every Agent Must Follow
1. **Read** STATUS.md + the relevant TASKS file before writing code.
2. **Claim** a task: set it to `IN_PROGRESS` with your name in the TASKS file + STATUS.md.
3. **Work only in the owner's directories** (ownership map below).
4. **On finish:** mark the task DONE in TASKS + STATUS.md, append one line to CHANGELOG.md, add any new gotcha to DECISIONS.md.

## Ownership Map
| Owner | Directories |
|---|---|
| **Person 1** (Runtime/NPU) | `/runtime`, `/android/.../ai`, `/android/.../core` |
| **Person 2** (App/Medical/Nav) | `/android/.../ui`, `/android/.../medical`, `/android/.../nav`, `/corpus`, `README.md` |

## File-Lock Etiquette
If a task is `IN_PROGRESS` by the other person, **don't touch those files** — pick a different task. The TASKS files + STATUS.md are the single source of truth; trust them over memory.

## The Shared Interface
The app talks to models only through `AiService`:
```kotlin
suspend fun transcribe(audioPath: String): String
suspend fun generate(prompt: String): String
```
Person 2 builds against `StubAiService` (canned strings) until Person 1's real implementation lands. Don't change this interface without noting it in DECISIONS.md.
