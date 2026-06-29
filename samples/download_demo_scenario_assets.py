#!/usr/bin/env python3
"""Download demo scenario images and compute verified hospital bearings."""

from __future__ import annotations

import json
import math
import time
import urllib.parse
import urllib.request
from pathlib import Path

OUT = Path(__file__).resolve().parent
HOSPITALS = (
    Path(__file__).resolve().parent.parent
    / "android/app/src/main/assets/sf_hospitals.json"
)
USER_AGENT = "LodestarHackathon/1.0 (demo scenario assets)"
WIKI_API = "https://commons.wikimedia.org/w/api.php"

ASSETS = [
    {
        "filename": "demo_wounded_hand.jpg",
        "wiki_title": "File:Wound_on_palm_of_hand_-_day_2.jpg",
        "thumb_width": 1600,
        "license": "Public domain (Arria Belli)",
        "source_page": "https://commons.wikimedia.org/wiki/File:Wound_on_palm_of_hand_-_day_2.jpg",
        "caption": "Palm laceration — day 2 after fall (reference injury photo, not a real patient on stage).",
    },
    {
        "filename": "demo_sf_powell_street.jpg",
        "wiki_title": "File:Powell_Street_from_Sutter_Street_(San_Francisco)_July_2022.JPG",
        "thumb_width": 2048,
        "license": "CC0 1.0",
        "source_page": "https://commons.wikimedia.org/wiki/File:Powell_Street_from_Sutter_Street_(San_Francisco)_July_2022.JPG",
        "caption": "Powell Street from Sutter Street, San Francisco — geotagged capture location.",
        "lat": 37.789261,
        "lon": -122.408653,
        "camera_heading_deg": 337.02,
        "location_label": "Powell Street at Sutter Street, Union Square, San Francisco, CA",
    },
]

EARTH_R_KM = 6371.0


def wiki_thumb_url(title: str, width: int) -> str:
    params = urllib.parse.urlencode(
        {
            "action": "query",
            "titles": title,
            "prop": "imageinfo",
            "iiprop": "url",
            "iiurlwidth": str(width),
            "format": "json",
        }
    )
    req = urllib.request.Request(f"{WIKI_API}?{params}", headers={"User-Agent": USER_AGENT})
    with urllib.request.urlopen(req, timeout=60) as resp:
        data = json.loads(resp.read().decode())
    page = next(iter(data["query"]["pages"].values()))
    info = page["imageinfo"][0]
    return info.get("thumburl") or info["url"]


def download(url: str, dest: Path) -> None:
    req = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
    with urllib.request.urlopen(req, timeout=120) as resp:
        dest.write_bytes(resp.read())


def distance_km(lat1: float, lon1: float, lat2: float, lon2: float) -> float:
    d_lat = math.radians(lat2 - lat1)
    d_lon = math.radians(lon2 - lon1)
    a = (
        math.sin(d_lat / 2) ** 2
        + math.cos(math.radians(lat1))
        * math.cos(math.radians(lat2))
        * math.sin(d_lon / 2) ** 2
    )
    return EARTH_R_KM * 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))


def bearing_deg(lat1: float, lon1: float, lat2: float, lon2: float) -> float:
    lat1r, lat2r = math.radians(lat1), math.radians(lat2)
    d_lon = math.radians(lon2 - lon1)
    y = math.sin(d_lon) * math.cos(lat2r)
    x = math.cos(lat1r) * math.sin(lat2r) - math.sin(lat1r) * math.cos(lat2r) * math.cos(d_lon)
    return (math.degrees(math.atan2(y, x)) + 360.0) % 360.0


def cardinal(deg: float) -> str:
    dirs = ["N", "NE", "E", "SE", "S", "SW", "W", "NW"]
    idx = int((deg + 22.5) / 45.0) % 8
    return dirs[idx]


def nearest_hospitals(lat: float, lon: float, n: int = 3) -> list[dict]:
    hospitals = json.loads(HOSPITALS.read_text(encoding="utf-8"))["hospitals"]
    ranked = []
    for h in hospitals:
        d = distance_km(lat, lon, h["latitude"], h["longitude"])
        b = bearing_deg(lat, lon, h["latitude"], h["longitude"])
        ranked.append(
            {
                "name": h["name"],
                "latitude": h["latitude"],
                "longitude": h["longitude"],
                "distance_km": round(d, 2),
                "bearing_deg": round(b, 1),
                "cardinal": cardinal(b),
            }
        )
    ranked.sort(key=lambda x: x["distance_km"])
    return ranked[:n]


def build_scenario(nearest: list[dict], field: dict) -> dict:
    primary = nearest[0]
    return {
        "scenario_title": "Powell Street palm laceration — offline triage + hospital heading",
        "field_position": field,
        "nearest_hospitals": nearest,
        "user_prompt_text": (
            "I fell on the sidewalk and cut my palm — there's a lot of blood and it hasn't stopped. "
            "I'm near Powell Street in San Francisco and GPS is unreliable."
        ),
        "user_prompt_with_image": (
            "Show demo_wounded_hand.jpg to the judge, then type the prompt above in TREAT."
        ),
        "expected_safety_tree": "CRITICAL",
        "expected_orient_line": (
            f"From Powell & Sutter ({field['lat']}, {field['lon']}): "
            f"nearest help is {primary['name']} — {primary['distance_km']} km, "
            f"head {primary['cardinal']} (~{primary['bearing_deg']}° true)."
        ),
        "narrator_close": (
            f"Move {primary['cardinal']} (~{int(primary['bearing_deg'])}°) toward "
            f"{primary['name']} — about {primary['distance_km']} km by great-circle, "
            "offline from our baked hospital list."
        ),
    }


def write_demo_markdown(scenario: dict) -> None:
    field = scenario["field_position"]
    nearest = scenario["nearest_hospitals"]
    p = nearest[0]

    md = f"""# Demo scenario — image + prompt + expected answer

> **Use on stage:** show the injury photo, type the prompt, then ORIENT with mock/cached position at Powell & Sutter.  
> Coordinates verified against Wikimedia Commons EXIF and `HospitalFinder` / `GeoMath` in the app.

---

## 1. Injury reference (show to judges)

![Wounded palm — demo reference](demo_wounded_hand.jpg)

*Public domain reference — [`demo_wounded_hand.jpg`](demo_wounded_hand.jpg)*

---

## 2. Field location (show on phone / slide)

![Powell Street, San Francisco — demo field photo](demo_sf_powell_street.jpg)

| Field | Value |
|-------|-------|
| **Place** | {field['location_label']} |
| **Latitude** | `{field['lat']}` |
| **Longitude** | `{field['lon']}` |
| **Camera heading** | `{field['camera_heading_deg']}°` (NNW — direction camera faced) |
| **Source** | [Wikimedia Commons CC0]({field['source_page']}) |

**Mock GPS for demo** (Developer options → mock location app):

```
{field['lat']}, {field['lon']}
```

---

## 3. USER PROMPT — copy into TREAT tab

```
{scenario['user_prompt_text']}
```

*(Optional: hold up `demo_wounded_hand.jpg` while saying it.)*

---

## 4. EXPECTED APP ANSWER (prompt-style)

### TREAT — SafetyTree (authoritative)

```
SEVERITY: CRITICAL
RULE: Active bleeding — "hasn't stopped"
DISCLAIMER: Reference / triage only — not a diagnosis.
```

**Say:** *"The CRITICAL label is deterministic — not from the LLM."*

### TREAT — Assistant text (stub or real LLM)

```
Apply firm direct pressure with a clean cloth to the palm wound.
Keep pressure continuous; elevate the hand if possible.
If bleeding does not stop, seek emergency care immediately.
[FA-0002] [FA-0004]

Disclaimer: This app does not replace professional medical care.
```

*(If stub: you'll see `[STUB RESPONSE]` — severity still real.)*

### ORIENT — Nearest hospitals from `{field['lat']}, {field['lon']}`

```
POSITION: {field['lat']}°N, {field['lon']}°W  (Powell & Sutter, SF)
HEADING TO NEAREST ER:

  1. {nearest[0]['name']}
     {nearest[0]['distance_km']} km  ·  {nearest[0]['bearing_deg']}° ({nearest[0]['cardinal']})

  2. {nearest[1]['name']}
     {nearest[1]['distance_km']} km  ·  {nearest[1]['bearing_deg']}° ({nearest[1]['cardinal']})

  3. {nearest[2]['name']}
     {nearest[2]['distance_km']} km  ·  {nearest[2]['bearing_deg']}° ({nearest[2]['cardinal']})
```

### ORIENT — One line for judges

> **"{scenario['narrator_close']}"**

---

## 5. Live demo sequence (90 seconds)

1. Airplane mode ON · open Lodestar  
2. Show **wounded hand** image → type **USER PROMPT** in TREAT → point at **CRITICAL**  
3. Mock GPS to `{field['lat']}, {field['lon']}` (or narrate if mock not ready)  
4. ORIENT → hospitals → confirm **#{nearest[0]['name']}** appears first  
5. Show **Powell Street** photo: *"This is where we are; move {p['cardinal']} toward the nearest ER — offline."*

---

## 6. Recovery

```powershell
cd QCOM
.\\scripts\\demo_fix.ps1 -Action stage
```

Push scenario images:

```powershell
$ADB = "..\\platform-tools\\adb.exe"
$ S = "samples"
& $ADB push "$S\\demo_wounded_hand.jpg" /sdcard/Download/
& $ADB push "$S\\demo_sf_powell_street.jpg" /sdcard/Download/
```
"""
    # fix typo $ S -> $S in md - I'll fix in the string
    md = md.replace("$ S", "$S")
    (OUT / "DEMO_SCENARIO.md").write_text(md, encoding="utf-8")


def main() -> None:
    OUT.mkdir(parents=True, exist_ok=True)
    field_meta = None

    for i, asset in enumerate(ASSETS):
        dest = OUT / asset["filename"]
        if not dest.exists() or dest.stat().st_size < 5000:
            if i:
                time.sleep(8)
            print(f"Downloading {asset['filename']} …")
            url = wiki_thumb_url(asset["wiki_title"], asset["thumb_width"])
            download(url, dest)
        else:
            print(f"Skip existing {asset['filename']}")
        if "lat" in asset:
            field_meta = asset

    assert field_meta is not None
    nearest = nearest_hospitals(field_meta["lat"], field_meta["lon"])
    field = {
        "location_label": field_meta["location_label"],
        "lat": field_meta["lat"],
        "lon": field_meta["lon"],
        "camera_heading_deg": field_meta["camera_heading_deg"],
        "source_page": field_meta["source_page"],
    }
    scenario = build_scenario(nearest, field)
    (OUT / "demo_scenario.json").write_text(json.dumps(scenario, indent=2) + "\n", encoding="utf-8")
    write_demo_markdown(scenario)
    print(json.dumps(nearest, indent=2))
    print(f"\nWrote {OUT / 'DEMO_SCENARIO.md'} and demo_scenario.json")


if __name__ == "__main__":
    main()
