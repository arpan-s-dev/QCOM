# DEMO.md — Run-of-Show & Honesty Sheet

> Rehearse this. The demo wins on equal code. Never overclaim — the "what's real vs roadmap" list below is what keeps a technical judge from sinking you.

## Run-of-Show (airplane mode, ~5 min)
1. **Open the app → flip the phone to AIRPLANE MODE on stage.** Radios off. This is the headline — say it out loud.
2. Speak: *"I have a deep bleeding wound on my leg, what do I do?"*
   → Whisper transcribes on the NPU → RAG + LLM returns MARCH-grounded bleeding-control steps → spoken back via TTS.
3. Say: *"GPS is jammed — which way is north?"* → trigger the **mock GPS spoof** → status strip flips `GPS_TRUSTED → SPOOF_DETECTED → DEAD_RECKONING` live.
4. Point the phone at the sun (or enter rough location) → **solar heading** appears, contrasted with the (deliberately wrong / magnet-disturbed) compass.
5. Quick **translation** exchange + show the **SOS card**. Close: *"Every bit of that ran on the NPU, offline, with the radios off."*

## Maps to all 5 judging criteria
Technical 40% (NPU, latency, energy) · Innovation 25% (GPS-denial) · Privacy 15% (on-device) · Deployment 10% · Presentation 10%.

## Anticipated Judge Questions
- **"What if it's cloudy / indoors / nighttime?"** → Solar needs sun; we fall back to dead-reckoning from last trusted fix. Night star plate-solve is our v2 roadmap; the manual/solar versions work today.
- **"Can you recover the real GPS location after a spoof?"** → No — you can't out-math a spoofed signal. We *detect* it via IMU cross-check, discard it, and fall back to sources the attacker isn't faking (dead-reckoning, sun). That's the honest, defensible claim.
- **"Is this giving medical diagnoses?"** → No. First-aid REFERENCE / TRIAGE only, grounded in TCCC/MARCH, with a deterministic safety tree making the high-stakes calls and disclaimers throughout.
- **"How accurate is the location?"** → Heading is sub-degree (better than the phone compass). We deliberately do NOT claim GPS-grade lat/long — that needs hardware a phone doesn't have.
- **"Is anything calling the cloud?"** → No. Airplane mode, on-device `.pte` / AI Hub assets only. (Offer to show it running with radios off.)

## What's REAL vs ROADMAP (do not blur these on stage)
**Real / demoed:**
- On-device LLM + Whisper on the NPU, fully offline
- RAG-grounded triage + deterministic safety tree
- Solar-math true-north heading
- GPS spoof detection with live fallback
- Airplane-mode operation

**Roadmap (say "v2", don't claim as working):**
- Night-time star plate-solve (`tetra3` + Yale Bright Star Catalog + star-point detector on NPU)
- Full lat/long position fixing
- Offline map routing

## The GPS-Jamming Story (for the pitch)
GNSS jamming/spoofing is endemic near conflict zones: >10,000 vessels affected in Q2 2025; 1,600+ aircraft over two days in March 2024 (Baltic); the EU Commission President's plane GPS-jammed in Sept 2025; Ukrainian anti-drone spoofing spilling into civilian phones. Jammers can be Wi-Fi-power devices; civilian GPS is unencrypted hence spoofable. The magnetometer is *also* unreliable/spoofable — which is exactly why a sun/star heading source matters. (One of the judges lived this in Israel — land that.)
