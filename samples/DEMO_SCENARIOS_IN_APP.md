# In-app Demo scenarios

Tap **Demo** (next to **Offline ready**) on any screen → pick a scenario.

All scenarios work **offline** — no mic, no NPU required. Images are bundled in `assets/demo/`.

---

| # | Scenario | What it does |
|---|----------|----------------|
| 1 | **Full field demo** | Powell St GPS + palm CRITICAL prompt → open **Nearby hospital** for Saint Francis ~0.7 km **west** |
| 2 | **Palm laceration (Powell St)** | Position `37.789261, -122.408653` + bleeding prompt → **CRITICAL** |
| 3 | **Hospital heading (Powell St)** | Same GPS → nearest: **Saint Francis Memorial** 0.7 km · **270° W** |
| 4 | **Negation — CRITICAL** | `"hasn't stopped"` → **CRITICAL** (SafetyTree) |
| 5 | **Negation — SERIOUS** | `"has stopped now"` → **SERIOUS** |
| 6 | **Night sky STAR_FIX** | Bundled Treasure Island photo → heading **~47°** |
| 7 | **Wound photo checklist** | Bundled palm image → guided infection signs |
| 8 | **Solar heading demo** | Demo heading **47°** (use **SIGHT SUN** outdoors for live solar) |
| 9 | **GPS spoof downgrade** | Simulates spoof → **DEAD_RECKONING** |
| 10 | **Translate (offline stub)** | Fills medic phrase → stub EN→ES |
| 11 | **Burn triage (stub LLM)** | Burn prompt → stub cooling guidance |

---

## Recommended judge flow

1. **Full field demo** (Assistant → then Hospital tab)
2. **Negation — CRITICAL** then **Negation — SERIOUS**
3. **Night sky STAR_FIX** (Location tab)

Tap the yellow **DEMO** banner × to exit demo mode.

---

## Powell St coordinates (verified)

- **37.789261, -122.408653** — Powell & Sutter, Union Square SF
- Nearest ER: **Saint Francis Memorial Hospital** — **0.7 km**, bearing **270° (west)**

See also [`DEMO_SCENARIO.md`](DEMO_SCENARIO.md) for narrator script.
