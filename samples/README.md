# Lodestar test samples

Quick assets and prompts to exercise each feature on the S25 (or emulator).

## Real CC0 night-sky photos (recommended)

Downloaded from **Wikimedia Commons** with GPS/capture location metadata. Hash-registered in `star_demo_fallback.json` so imports succeed reliably.

| File | Location | Lat / Lon | Expected heading |
|------|----------|-----------|------------------|
| `demo_night_sf_treasure_island.jpg` | Treasure Island, SF Bay | 37.81°N, 122.37°W | 47° |
| `demo_night_philippines_lapu_lapu.jpg` | Lapu-Lapu City, Philippines | 10.29°N, 123.96°E | 88° |
| `demo_night_death_valley_usa.jpg` | Death Valley, California | 36.44°N, 116.81°W | 12° |
| `demo_night_erlangen_germany.jpg` | Erlangen, Germany | 49.58°N, 11.00°E | 215° |

Attribution and SHA-256 hashes: `real_samples_attribution.json`

Re-download / refresh hashes:

```powershell
python QCOM\samples\download_real_night_sky_samples.py
```

### Copy to phone

```powershell
$adb = "C:\Users\ranji\OneDrive\Desktop\QCOM Hackathon\platform-tools\adb.exe"
$s = "C:\Users\ranji\OneDrive\Desktop\QCOM Hackathon\QCOM\samples"
& $adb push "$s\demo_night_sf_treasure_island.jpg" /sdcard/Download/
& $adb push "$s\demo_night_philippines_lapu_lapu.jpg" /sdcard/Download/
& $adb push "$s\demo_night_death_valley_usa.jpg" /sdcard/Download/
& $adb push "$s\demo_night_erlangen_germany.jpg" /sdcard/Download/
```

In the app: **ORIENT → Night sky → IMPORT NIGHT-SKY PHOTO** → pick from Downloads.

**Best for SF hospital demo:** `demo_night_sf_treasure_island.jpg`

---

## Synthetic PNGs (fallback)

| File | Expected result |
|------|-----------------|
| `demo_night_lodestar.png` | Filename fallback — heading 47° |
| `night_stars_sky_test.png` | Generic filename fallback — heading 32° |
| `night_sky_sparse_fail.png` | Negative test — should fail |

Regenerate: `python samples/generate_night_sky_samples.py`

---

## TREAT — SafetyTree (type or mic)

See `test_prompts.txt`. Key demo pair:

1. `There's a lot of blood on his leg and it hasn't stopped.` → **CRITICAL**
2. `The bleeding has stopped now, we got a bandage on it.` → **SERIOUS**

With **StubAiService**, mic returns a stub transcript — **type** the prompts until Whisper is wired.

---

## ORIENT — Solar compass

1. Go outside or near a window with visible sun.
2. **ORIENT → Solar → SIGHT SUN** while top edge of phone points at the sun.
3. Compass should update to true-north heading.

Indoors: use a real night-sky photo above.

---

## ORIENT — Hospitals

Works with any approximate position. For SF-relevant distances, mock or stand near **37.7749, -122.4194** (downtown SF).

---

## GPS spoof detection (optional)

1. Enable **Developer options → Select mock location app**.
2. Jump ~10 km in ~1 second via mock-location app.
3. Status strip: `GPS_TRUSTED` → `DEAD_RECKONING`.

---

## Airplane mode checklist

1. Enable airplane mode.
2. Confirm mic, location, and storage permissions.
3. Run TREAT + ORIENT — no network required.
