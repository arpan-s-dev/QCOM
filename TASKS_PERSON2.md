# TASKS — PERSON 2 (App + Medical + Navigation + Pitch)

> You own everything users touch. Build against `StubAiService` until Person 1's real `AiService` lands — never blocked.
> Mark each: `[ ]` TODO · `[~]` IN_PROGRESS · `[x]` DONE. Update STATUS.md + CHANGELOG.md on finish.

## Phase 0 — App skeleton
- [ ] **P2.0a** Scaffold Compose app: chat surface (message list + input + mic button) + section switcher (TREAT / ORIENT / COMMUNICATE).
- [ ] **P2.0b** Persistent **status strip** (always visible): position source (`GPS_TRUSTED` / `DEAD_RECKONING` / `SOLAR_FIX`) with a pulsing indicator + an **AIRPLANE MODE** badge. This is the signature element.
- [ ] **P2.0c** `StubAiService` returning canned strings so the app runs before models exist.

## Phase 1 — Medical (biggest score — do first)
- [ ] **P2.1** Build the **first-aid corpus** from TCCC + MARCH (Massive hemorrhage, Airway, Respiration, Circulation, Hypothermia): arterial bleed/tourniquet, wound packing, burns by degree/%, fractures, shock, blast injury, infection signs. ~200–400 short self-contained chunks with source tags. (Use Claude/Copilot to draft — allowed.)
- [ ] **P2.2** Embed corpus once (Person 1's BGE embedder), ship vectors in APK. Query → top-k cosine retrieval (in-memory) → **grounded prompt** forcing the LLM to answer ONLY from retrieved passages + cite + disclaimer.
- [ ] **P2.3** **Deterministic safety tree** (authoritative over LLM), priority order: not-breathing → arterial/spurting bleed not stopped → 3rd-degree/>20% burn → SERIOUS/MODERATE/MINOR. **Parser must handle NEGATION** — write tests proving `"hasn't stopped" → CRITICAL` and `"has stopped now" → SERIOUS`.
- [ ] **P2.4** **Voice loop:** mic → `AiService.transcribe()` → RAG+LLM → Android native `TextToSpeech`. Disclaimers on screen.

## Phase 2 — Navigation (reliable wins only)
- [ ] **P2.5** **Verify solar math in Python FIRST**: NOAA/Meeus sun azimuth+elevation; sanity-check (~37.3N,121.9W late June): solar noon ≈ due south, sunrise ENE, sunset WNW, negative elevation at night.
- [ ] **P2.6** Port to Kotlin `SolarCompass`: time + rough location → sun azimuth → point phone at sun → derive + display **true-north heading** (AR compass overlay).
- [ ] **P2.7** **GPS spoof detection:** IMU dead-reckoning estimate; GPS jump > plausible (elapsed × max speed) → flag `SPOOF_DETECTED`, freeze to last-trusted + DR. Wire into status strip. **Demo via mock-location on the real phone.**
- [ ] **P2.8** `PositionSource` state machine `GPS_TRUSTED → DEAD_RECKONING → SOLAR_FIX` feeding the status strip.

## Phase 3 — Bonus + Pitch
- [ ] **P2.9** Translation: prompt the LLM; minimal two-field medic↔casualty screen. *(Only if 1–2 solid.)*
- [ ] **P2.10** SOS card: LLM drafts structured distress summary (injury, approx position, # people, needs). *(Only if 1–2 solid.)*
- [ ] **P2.11** **Pitch deck + DEMO.md run-of-show.** Bake in GPS-jamming validation (Ukraine/Baltic/Israel; >10,000 vessels Q2 2025; 1,600+ aircraft Mar 2024; von der Leyen Sept 2025). Land the Israel judge's lived experience.
- [ ] **P2.12** **README** (required for eligibility): app description, both names/emails, setup, run/usage, **MIT license**, architecture diagram (mermaid).

## Definition of Done (your lane)
Airplane-mode app: speak a wound → spoken triage answer (RAG-grounded, safety-tree-gated); trigger a spoof → status flips live; solar heading displays vs. compass. README + pitch ready, real-vs-roadmap honest.
