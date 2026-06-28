#!/usr/bin/env python3
"""Generate synthetic night-sky PNGs for Lodestar star-navigation testing.

CvStarDetector needs bright blobs on a dark field. PrecomputedStarSolver
matches filename substrings (demo_night, night, stars, sky) and requires
>= 8 detected stars for the generic fallback entry.
"""

from __future__ import annotations

import random
from pathlib import Path

try:
    from PIL import Image, ImageDraw
except ImportError:
    raise SystemExit("Install Pillow: pip install pillow")

OUT = Path(__file__).resolve().parent
RNG = random.Random(42)


def star_field(
    width: int,
    height: int,
    star_count: int,
    *,
    min_sep: float = 28.0,
) -> list[tuple[int, int, int]]:
    """Return (x, y, brightness) star centers."""
    stars: list[tuple[int, int, int]] = []
    margin = 24
    attempts = 0
    while len(stars) < star_count and attempts < star_count * 200:
        attempts += 1
        x = RNG.randint(margin, width - margin)
        y = RNG.randint(margin, height - margin)
        if all((x - sx) ** 2 + (y - sy) ** 2 >= min_sep**2 for sx, sy, _ in stars):
            brightness = RNG.randint(230, 255)
            stars.append((x, y, brightness))
    return stars


def render(path: Path, stars: list[tuple[int, int, int]], width: int, height: int) -> None:
    img = Image.new("RGB", (width, height), (8, 10, 18))
    draw = ImageDraw.Draw(img)
    for x, y, b in stars:
        # 3x3 core + faint halo so flood-fill finds >= minBlobPixels
        for dy in range(-2, 3):
            for dx in range(-2, 3):
                px, py = x + dx, y + dy
                if 0 <= px < width and 0 <= py < height:
                    dist = abs(dx) + abs(dy)
                    val = max(0, b - dist * 18)
                    if val > 40:
                        img.putpixel((px, py), (val, val, min(255, val + 8)))
        draw.ellipse((x - 1, y - 1, x + 1, y + 1), fill=(b, b, 255))
    img.save(path, format="PNG", optimize=True)
    print(f"Wrote {path.name} ({width}x{height}, {len(stars)} stars)")


def main() -> None:
    OUT.mkdir(parents=True, exist_ok=True)

    # Primary demo asset — matches demo_night / lodestar_night / night_sky_demo
    render(
        OUT / "demo_night_lodestar.png",
        star_field(1280, 960, 14),
        1280,
        960,
    )

    # Generic fallback — filename contains "night" + needs >= 8 stars
    render(
        OUT / "night_stars_sky_test.png",
        star_field(1024, 768, 12),
        1024,
        768,
    )

    # Sparse frame — geometry solver may fail; good negative test
    render(
        OUT / "night_sky_sparse_fail.png",
        star_field(640, 480, 3, min_sep=80.0),
        640,
        480,
    )

    print("\nCopy to phone:")
    print('  adb push samples/demo_night_lodestar.png /sdcard/Download/')


if __name__ == "__main__":
    main()
