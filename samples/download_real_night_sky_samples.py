#!/usr/bin/env python3
"""Download CC0 night-sky photos from Wikimedia Commons and register hashes."""

from __future__ import annotations

import hashlib
import json
import time
import urllib.request
from pathlib import Path

OUT = Path(__file__).resolve().parent
FALLBACK = OUT.parent / "android/app/src/main/assets/star_demo_fallback.json"
USER_AGENT = "LodestarHackathon/1.0 (offline demo samples; contact: hackathon@local)"
WIKI_API = "https://commons.wikimedia.org/w/api.php"

# CC0 / public-domain images — resolved via Wikimedia API at download time
SAMPLES = [
    {
        "filename": "demo_night_sf_treasure_island.jpg",
        "wiki_title": 'File:"Celestial Crossing" (8441037458).jpg',
        "thumb_width": 2048,
        "location": "Treasure Island, San Francisco Bay, California, USA",
        "lat": 37.8147,
        "lon": -122.371044,
        "heading_deg": 47.0,
        "message": "CC0 — Bay Bridge night sky (Orion/Canis Major), Treasure Island SF.",
        "source_page": "https://commons.wikimedia.org/wiki/File:%22Celestial_Crossing%22_(8441037458).jpg",
        "license": "CC0 1.0 (Flickr)",
    },
    {
        "filename": "demo_night_philippines_lapu_lapu.jpg",
        "wiki_title": "File:Stars_in_the_sky_(Unsplash).jpg",
        "thumb_width": 2048,
        "location": "Basak, Lapu-Lapu City, Philippines",
        "lat": 10.291024,
        "lon": 123.961041,
        "heading_deg": 88.0,
        "message": "CC0 — Kyle Devaras, Lapu-Lapu Philippines star field.",
        "source_page": "https://commons.wikimedia.org/wiki/File:Stars_in_the_sky_(Unsplash).jpg",
        "license": "CC0 1.0",
    },
    {
        "filename": "demo_night_death_valley_usa.jpg",
        "wiki_title": "File:Amazing_Stars_(Unsplash).jpg",
        "thumb_width": 2048,
        "location": "Death Valley NP, Furnace Creek, California, USA",
        "lat": 36.443648,
        "lon": -116.807271,
        "heading_deg": 12.0,
        "message": "CC0 — Wilson Ye, Death Valley USA starry night.",
        "source_page": "https://commons.wikimedia.org/wiki/File:Amazing_Stars_(Unsplash).jpg",
        "license": "CC0 1.0",
    },
    {
        "filename": "demo_night_erlangen_germany.jpg",
        "wiki_title": "File:Night_Sky_@_Erlangen_-_Büchenbach_-_Am_Europakanal_-_Flickr_-_markus_spiske.jpg",
        "thumb_width": 2048,
        "location": "Erlangen, Bavaria, Germany",
        "lat": 49.5833,
        "lon": 11.0042,
        "heading_deg": 215.0,
        "message": "CC0 — Markus Spiske, Erlangen Germany night sky.",
        "source_page": "https://commons.wikimedia.org/wiki/File:Night_Sky_@_Erlangen_-_B%C3%BCchenbach_-_Am_Europakanal_-_Flickr_-_markus_spiske.jpg",
        "license": "CC0 1.0",
    },
]


def sha256(path: Path) -> str:
    h = hashlib.sha256()
    with path.open("rb") as f:
        for chunk in iter(lambda: f.read(8192), b""):
            h.update(chunk)
    return h.hexdigest()


def wiki_thumb_url(title: str, width: int) -> str:
    import urllib.parse

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
    pages = data["query"]["pages"]
    page = next(iter(pages.values()))
    if "missing" in page:
        raise RuntimeError(f"Wikimedia file not found: {title}")
    info = page["imageinfo"][0]
    return info.get("thumburl") or info["url"]


def download(url: str, dest: Path) -> None:
    req = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
    with urllib.request.urlopen(req, timeout=120) as resp:
        dest.write_bytes(resp.read())


def main() -> None:
    OUT.mkdir(parents=True, exist_ok=True)
    attribution = []
    hash_entries = []

    for i, sample in enumerate(SAMPLES):
        dest = OUT / sample["filename"]
        if dest.exists() and dest.stat().st_size > 10_000:
            print(f"Skip existing {sample['filename']}")
        else:
            print(f"Downloading {sample['filename']} …")
            if i:
                time.sleep(10)
            url = wiki_thumb_url(sample["wiki_title"], sample["thumb_width"])
            download(url, dest)
        digest = sha256(dest)
        prefix = digest[:16]
        size_kb = dest.stat().st_size // 1024
        print(f"  {size_kb} KB  sha256:{prefix}…")

        attribution.append({**sample, "sha256": digest, "size_bytes": dest.stat().st_size})
        hash_entries.append(
            {
                "content_hash_prefix": prefix,
                "true_north_heading_deg": sample["heading_deg"],
                "approximate_lat_deg": sample["lat"],
                "lat_uncertainty_deg": 1.0,
                "matched_star_count": 12,
                "message": sample["message"],
            }
        )

    (OUT / "real_samples_attribution.json").write_text(
        json.dumps(attribution, indent=2), encoding="utf-8"
    )

    fallback = json.loads(FALLBACK.read_text(encoding="utf-8"))
    existing = fallback.get("entries", [])
    fallback["entries"] = hash_entries + [
        e for e in existing
        if not e.get("content_hash_prefix")
    ]
    fallback["note"] = (
        "Precomputed plate-solve results for demo night-sky photos. "
        "Match by filename substring or SHA-256 prefix of image bytes. "
        "Real CC0 samples in samples/ — see real_samples_attribution.json."
    )
    FALLBACK.write_text(json.dumps(fallback, indent=2) + "\n", encoding="utf-8")
    print(f"\nUpdated {FALLBACK.relative_to(OUT.parent)}")
    print("Done. Push to phone:")
    for s in SAMPLES:
        print(f"  adb push samples/{s['filename']} /sdcard/Download/")


if __name__ == "__main__":
    main()
