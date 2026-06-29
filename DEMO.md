# DEMO.md — 5-minute judge script

**Target:** 4-5 minutes live, inside a ~10 minute pitch slot.

## What is honest to claim today

- **Live now:** deterministic SafetyTree triage, solar heading, night-sky photo flow, offline SF hospitals, spoof-detection logic, Compose UI, airplane-mode operation.
- **Only claim NPU language generation / ASR if it is actually landed on the device.**
- If `RealAiService` is **not** wired by demo time, say this plainly: **"The safety decision is live and deterministic; the fluent assistant response is still coming from our stubbed AI service today."**

---

## Pre-stage checklist

1. Put the phone in **airplane mode** and confirm mic, speaker, and location permissions are already granted.
2. Open the app once before walking on stage so the first screen is warm.
3. For the **solar** path, confirm the sun is visible; otherwise plan to use the **night-sky photo** path instead.
4. For the **star** path, pre-stage an outdoor night-sky image on the phone. Keep `night` or `demo_night` in the filename so the fallback mapping still works.
5. If you want the **optional spoof** moment, enable mock locations ahead of time and have the tool ready.

---

## Judge script

### 0:00-0:30 — Open with offline truth

- Open Lodestar and point at the status strip first.
- Optional one-liner if UI animations are visible: **"The whole shell is built for field use — high-contrast, offline-first, with trust state always on screen."**
- Say: **"This phone is in airplane mode. No cloud calls, no map API, no Street View, and no internet requirement at runtime."**
- If the position pill already shows `GPS_TRUSTED`, add: **"Even in airplane mode, we can still use device sensors and GPS when we trust it."**

### 0:30-1:45 — TREAT: negation is the real safety demo

- Go to **TREAT** and use the mic or type if audio is flaky.
- Say: **"There's a lot of blood on his leg and it hasn't stopped."**
- Wait for the transcript, severity label, answer, and disclaimer.
- Narrate: **"The important part is not the wording flair. The CRITICAL label comes from a deterministic safety tree, not the language model."**
- Immediately contrast with: **"The bleeding has stopped now, we got a bandage on it."**
- Call out the negation difference: **"We specifically tested 'hasn't stopped' versus 'has stopped now' because that's the failure mode that matters."**
- If the NPU backend is still pending, say: **"Today the explanation text is stubbed, but the safety classification and offline workflow are real."**

### 1:45-3:00 — ORIENT: use the sun or the stars

Choose **one** live path based on the room. Do not fake both.

**Option A — Solar (daytime, recommended when the sun is visible)**

- Switch to **ORIENT → Solar**.
- Hold the phone toward the sun and tap **SIGHT SUN**.
- Say: **"When GPS is degraded, we can still recover true heading from solar position math, fully offline."**
- Point out the compass update and corrected heading.

**Option B — Night sky photo (recommended indoors or when lighting is bad)**

- Switch to **ORIENT → Night sky**.
- Tap **IMPORT NIGHT-SKY PHOTO** and select the staged image.
- Show the detected stars, the heading result, and the `STAR_FIX` state.
- Say: **"This is classical computer vision plus a bundled bright-star catalog. No network, no model training, and we ship a demo-safe fallback for the staged image."**

### 3:00-3:45 — Hospitals: nearest help still works offline

- Stay in **ORIENT** and open the hospitals panel/list.
- Show the nearest 3 San Francisco hospitals with distance and bearing.
- Say: **"These are baked from offline JSON. If connectivity is down, we still have a practical direction-of-travel aid for known emergency sites."**
- Keep the wording honest: this is **heading guidance**, not turn-by-turn navigation.

### 3:45-4:30 — Optional GPS spoof moment

Only do this if the tooling is ready. Otherwise skip it cleanly.

**Live spoof path**

1. Feed a physically impossible jump with mock location tooling.
2. Show the status strip changing from `GPS_TRUSTED` to `DEAD_RECKONING`.
3. Say: **"We do not silently trust impossible jumps. We freeze to the last trusted position and surface the trust downgrade."**

**If you skip it**

- Say: **"We tested spoof detection, but we're skipping the live trigger to stay inside time."**

### 4:30-5:00 — Close

- Land on **COMMUNICATE** or back on the status strip.
- Say: **"What you saw was offline triage, offline heading recovery, and offline hospital reference in airplane mode. If the Snapdragon NPU language stack lands in time, the same app swaps from stub AI to real on-device generation through one interface."**

---

## Failure-safe fallbacks

> **Full runbook + fix commands:** [`docs/DEMO_SAFE_RUNBOOK.md`](docs/DEMO_SAFE_RUNBOOK.md)  
> **One-command recovery:** `.\scripts\demo_fix.ps1 -Action stage` (from `QCOM/`)

- **Mic or TTS fails:** type the prompt and keep the negation demo.
- **Sun not visible:** use the night-sky photo path and say why.
- **Night-sky import misbehaves:** pick `demo_night_sf_treasure_island.jpg` from Downloads, or run `demo_fix.ps1 -Action stage`; narrate expected `STAR_FIX` ~47° if UI still fails.
- **Model / NPU crash:** `.\scripts\demo_fix.ps1 -Action install-stub` then type triage only.
- **Spoof tooling is not ready:** skip it and keep the rest of the flow.
- **NPU backend does not land:** say "stubbed AI service today" once, confidently, then move on.
