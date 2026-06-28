# Pitch Outline — Lodestar

10 slides. Each bullet is what goes ON the slide; speaker notes are in italics below each.

---

## 1. Title
**Lodestar** — first aid, navigation, and communication that work with zero signal.
*Built for the Qualcomm Snapdragon 8 Elite / ExecuTorch hackathon. Names and contacts on the last slide.*

---

## 2. The problem isn't hypothetical anymore
- GPS/GNSS jamming and spoofing is now a **daily, ongoing reality**, not a wartime edge case
- Poland alone logged **2,732 jamming incidents in January 2025 alone** — up from 1,908 in late 2023
- Lithuania recorded **1,185 cases** in the same month — more than double March 2024's figure
- An EU document covering 13 member states calls this **"a systemic, deliberate action,"** not random interference

*Source: EU document cited by NBC News, reporting on a June 2025 alarm raised by 13 EU member states over jamming/spoofing threats to air and sea travel.*

---

## 3. It's not just statistics — it reaches the top
- **September 2025:** European Commission President Ursula von der Leyen's plane lost GPS approaching Plovdiv, Bulgaria — pilots landed using paper maps. Bulgarian and EU officials suspect Russian interference.
- **2024:** A plane carrying the British Defence Secretary had its satellite signal jammed near Russian territory
- **2024:** A Finnish airline suspended flights to Tartu, Estonia for a month due to jamming
- Latvia recorded **820 interference incidents in 2024**, up from just **26 in 2022**

*Source: AP/PBS, NBC News, CBC News reporting on the September 2025 von der Leyen incident and surrounding context.*

---

## 4. At sea, the scale is enormous
- GPS jamming affected **nearly 2,000 ships in a single 30-day window** in mid-2025 across four key regions (Windward AI data)
- The Persian Gulf/Strait of Hormuz alone saw **over 3,000 disrupted vessels in under two weeks** in June 2025
- April 2024: **117 ships simultaneously spoofed** to appear at Beirut Airport; later **227 ships spoofed** across the Eastern Mediterranean
- Real consequences, not near-misses: GPS jamming caused the container ship **MSC Antonia to run aground** near Jeddah, Saudi Arabia (May 2025)

*Source: Windward AI / GPSPATRON cumulative maritime GNSS interference analysis 2025; Lloyd's List.*

---

## 5. This isn't only "over there"
- A US-based judge or attendee with maritime, aviation, or military experience has very likely
  either witnessed this directly or worked alongside people who have
- (**Speaker note, judge-specific:** if a judge has lived experience with this — e.g. an Israeli
  background, given GPS jamming/spoofing around conflict zones in Israel's region — land that
  moment directly and let them speak to it rather than over-explaining. Don't assume the
  specifics; ask if they'd like to share.)
- The pattern is consistent everywhere it happens: **the moment GPS goes, so does everything
  built only on top of GPS** — including, often, easy access to medical guidance and coordination

---

## 6. What we built: an app that assumes GPS is gone
- **TREAT** — voice or text first-aid triage, fully offline, grounded in a real TCCC/MARCH
  first-aid corpus with citations, gated by a deterministic safety tree (not just "ask the LLM")
- **ORIENT** — when GPS is gone, fall back to dead reckoning, then to a **solar compass** (day)
  or **night-sky star plate-solve** (import photo → CV detection + catalog geometry) — both derive
  true north with zero satellites and zero network
- **COMMUNICATE** — medic↔casualty translation and a structured SOS summary, both offline

*[Live demo happens here — see DEMO.md]*

---

## 7. The signature element: you can SEE the trust level
- A persistent status strip, visible on every screen, shows exactly which position source is
  active: `GPS_TRUSTED` → `DEAD_RECKONING` → `SOLAR_FIX` / `STAR_FIX`
- A pulsing position-source indicator makes trust level visible at a glance — not buried in a
  settings menu, and no fake airplane badge needed
- When we simulate a GPS spoof live, the strip **flips in real time** and freezes to the last
  trusted position instead of silently trusting a forged signal

---

## 8. Why the medical side is trustworthy, not just clever
- Severity is decided by a **deterministic rule engine**, not the language model — the LLM can
  explain and cite sources, but it cannot talk its way into downgrading a critical situation
- We specifically tested negation handling (*"the bleeding hasn't stopped"* vs. *"has stopped
  now"*) because getting this backwards is the actual failure mode that matters in the field
- We found and fixed a real bug during testing (resumed bleeding after a false "stopped") —
  happy to show the test suite, not just claim it works

---

## 9. Live today vs. roadmap next
**Live today**
- SafetyTree triage with tested negation handling
- Solar heading and night-sky photo heading, both fully offline
- Offline SF hospitals from bundled JSON
- Spoof-detection logic and trust-state UI
- Full app shell, RAG plumbing, and `AiService` abstraction
- Field-instrument Compose UI (field-green / bone / signal-orange) with animated tab and status transitions *(landing on `demo/final`)*

**Roadmap / only claim if landed before demo**
- Snapdragon NPU-backed `RealAiService` for generation, embeddings, and ASR
- More medical corpus depth beyond the current 93 curated chunks
- Stronger spoof filtering beyond the current speed-gate heuristic
- Camera-composited AR heading overlay

*Speaker note: be explicit that the deterministic safety layer is real today even if the LLM layer remains stubbed.*

---

## 10. Team / contact
- Arpanjeet Singh — 106011010+arpan-s-dev@users.noreply.github.com — AI models, embeddings, on-device inference
- Manjeet Singh — 62642705+manjeetsingh-satveer@users.noreply.github.com — app, medical corpus + safety tree, navigation, pitch
- Code: https://github.com/arpan-s-dev/QCOM · MIT licensed
