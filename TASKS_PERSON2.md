# TASKS — PERSON 2 (App + Medical + Navigation + Pitch)

> You own everything users touch. Build against `StubAiService` until Person 1's real `AiService` lands — never blocked.
> Mark each: `[ ]` TODO · `[~]` IN_PROGRESS · `[x]` DONE. Update STATUS.md + CHANGELOG.md on finish.

## Phase 0 — App skeleton
- [x] **P2.0a** Scaffold Compose app: chat surface + section switcher (TREAT / ORIENT / COMMUNICATE)
- [x] **P2.0b** Persistent status strip: position source + pulsing indicator + AIRPLANE MODE badge
- [x] **P2.0c** `StubAiService` with canned strings + `AiService` interface

## Phase 1 — Medical
- [x] **P2.1** First-aid corpus (TCCC/MARCH) — 93 chunks in `corpus/`
- [x] **P2.2** Retrieval pipeline + grounded prompt (awaiting P1 BGE embedder for real vectors)
- [x] **P2.3** Deterministic safety tree + negation tests (16/16 Python pass)
- [x] **P2.4** Voice loop wiring (stub ASR until P1)

## Phase 2 — Navigation
- [x] **P2.5** Solar math verified in Python (4/4 sanity checks pass)
- [x] **P2.6** `SolarCompass.kt` + AR overlay composable
- [x] **P2.7** GPS spoof detection + mock-location demo steps in DEMO.md
- [x] **P2.8** `PositionSource` state machine wired to status strip

## Phase 3 — Bonus + Pitch
- [x] **P2.9** Translation screen
- [x] **P2.10** SOS card
- [x] **P2.11** Pitch outline + DEMO.md
- [x] **P2.12** README + MIT LICENSE + architecture diagram

## Integration note (2026-06-28)
Person 2's zip (`track2manjeet.zip`) was extracted into `android/`, `corpus/`, `scripts/`, `docs/` on branch `integrate/lodestar-v1`. See `SETUP.md` for build instructions.

## Integration hardening (2026-06-28)
- [x] **P2.13** Make `android/` build cleanly with `./gradlew.bat test` on Windows (Compose plugin, theme/resources, icon assets, dependency fixes).
- [x] **P2.14** Bundle `corpus/first_aid_corpus.json` into `android/app/src/main/assets/` and load it through `android/app/.../data/CorpusLoader.kt` at app startup.
- [x] **P2.15** Replace README placeholder identities with real team names/emails.
