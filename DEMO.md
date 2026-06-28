# DEMO.md — Run of Show

**Total target time: ~4-5 minutes live demo, inside a ~10 minute slot with the pitch.**

> Honesty checkpoint before you demo: this app has been written but not compiled/run on a
> real device by the person who wrote this file (sandboxed build environment, no Android SDK).
> Build it in Android Studio and run through this whole script at least once, well before your
> slot, so you're not debugging live. If something doesn't compile, the most likely first
> issues are Gradle/Compose BOM version mismatches — bump versions via Android Studio's
> suggested fixes rather than hand-editing further.

---

## Pre-demo setup (do this before you're on stage)

1. Phone in airplane mode, mic and location permissions already granted (grant them in a
   previous run so you don't eat time on a permission dialog mid-demo).
2. Make sure Developer Options → "Allow mock locations" is enabled, and the app you'll use to
   inject a mock location (e.g. a simple mock-location app, or `adb` from a laptop) is set as
   the mock location provider.
3. Have a real GPS fix established BEFORE going into airplane mode if your flow depends on a
   last-known-trusted location for the spoof-freeze demo to look right.
4. Confirm Solar sighting works: check that wherever you are demoing has a visible sun (or skip
   straight to a pre-recorded screen capture of the ORIENT screen if you're indoors — say so
   explicitly rather than faking it live).

---

## Beat 1 — "Offline, no network." (~30s)

- Open the app. Point at the status strip immediately.
- Toggle airplane mode on the phone if it isn't already — narrate: "No SIM, no WiFi, no
  internet permission in the manifest — all data and models are on-device."
- The position pill should read `GPS_TRUSTED` with a slow pulse (real GPS still works without
  cellular/Wi‑Fi). No decorative airplane badge — the app just works offline.

## Beat 2 — "Speak a wound." (~60-90s)

- Switch to TREAT. Press the mic button.
- Say something with a clear negation case, since that's the thing you tested hardest, e.g.:
  *"There's a lot of blood on his leg and it hasn't stopped."*
- Wait for: transcript appears as a user message → CRITICAL tag appears → spoken answer plays
  via TTS → on-screen disclaimer is visible under the answer.
- Optionally do a second, calmer example to show the SERIOUS/MODERATE distinction, e.g.
  *"The bleeding has stopped now, we got a bandage on it."* — should NOT come back CRITICAL.
- Narration line: "The severity label never comes from the language model — it comes from a
  deterministic rule engine underneath. The model can only explain, not downgrade."

## Beat 3 — "Now we kill GPS trust." (~60-90s)

This is the centerpiece. Two ways to do it depending on what you have time/tools for:

**Option A (real spoof demo, requires mock-location setup above):**
1. While the app is in the foreground, use your mock-location tool to feed it a location that's
   physically impossible given elapsed time (e.g. teleport ~50km in a couple of seconds).
2. The status strip should flip from `GPS_TRUSTED` to `DEAD_RECKONING`, the pulse should speed
   up, and "SPOOF_DETECTED — frozen to last trusted fix" should appear under the label.
3. Narration: "It didn't just accept that. It compared the jump against what's physically
   possible and froze to the last position it actually trusted."

**Option B (no mock-location tooling available / time-constrained):**
1. Show the `SpoofDetectorTest.kt` test file and/or run the equivalent verification script
   live (`scripts/verify_safety_tree.py` and a quick spoof check) to prove the logic, then
   show the STATIC `StatusStrip` states by toggling a debug switch in the app (if you wire one
   in) or by describing what Option A would show.
2. Be straightforward that this is the fallback path: "We'll show you the test proving this
   logic is correct; the live trigger needs mock-location tooling we don't have rigged right
   now."

## Beat 4 — "When even GPS estimation runs out, here's the sun." (~60s)

- Switch to ORIENT.
- If indoors or the sun isn't visible: narrate the screen instead of performing the sighting
  gesture, and say so plainly ("we'll show you the math, not fake the sighting").
- If outdoors with sun visible: hold the phone with the top edge pointed at the sun, tap "SIGHT
  SUN," and show the compass ring update with the corrected heading needle plus the yellow sun
  marker at its computed azimuth.
- Narration: "This isn't guessing. NOAA/Meeus solar position math, the same kind used in
  real navigation software — we verified it against known sunrise/sunset/solar-noon directions
  for this location before trusting it."

## Beat 5 — Close (~30s)

- Quick cut to COMMUNICATE: show the SOS card populated from the conversation so far, and/or
  the two-field translation box.
- Land the close line: "Everything you just saw ran with zero network connectivity. The only
  thing we haven't wired in yet is swapping the placeholder language model for the real
  on-device one — and that's a one-line change, because we built the whole app against a single
  interface from day one."

---

## If something breaks live

- **Mic/TTS doesn't work:** fall back to typing the query into the text field — the chat surface
  and safety tree work identically either way; only the audio I/O layer is skipped.
- **Mock-location demo doesn't trigger:** use Option B above. Don't improvise a fake UI state —
  show the passing test instead, and say that's what you're doing.
- **Solar sighting unavailable (no sun/indoors):** narrate over a description or a pre-recorded
  clip; say plainly that you're doing so.
- **App crashes:** have a 60-second screen recording of a successful run on a backup device or
  laptop as an absolute last resort, and say so before playing it.
