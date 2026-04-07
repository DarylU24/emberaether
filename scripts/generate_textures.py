#!/usr/bin/env python3
"""
generate_textures.py
Generates 128×128 placeholder PNG textures for the EmberAether mod.

Each UV region is filled with a distinct colour so artists can immediately
see which pixel area belongs to which body part.

Usage:
    python3 scripts/generate_textures.py

Output:
    src/main/resources/assets/emberaether/textures/entity/infernal_drake.png
    src/main/resources/assets/emberaether/textures/entity/frost_wyvern.png
"""

import struct
import zlib
import os
from pathlib import Path

# ─── Minimal PNG writer (no external deps) ─────────────────────────────────

def _pack_chunk(chunk_type: bytes, data: bytes) -> bytes:
    crc = zlib.crc32(chunk_type + data) & 0xFFFFFFFF
    return struct.pack(">I", len(data)) + chunk_type + data + struct.pack(">I", crc)

def write_png(path: str, pixels: list[list[tuple[int,int,int,int]]]):
    """Write a 128×128 RGBA PNG to *path*."""
    width = height = 128
    sig = b"\x89PNG\r\n\x1a\n"
    ihdr_data = struct.pack(">IIBBBBB", width, height, 8, 2, 0, 0, 0)
    # colour type 2 = RGB (no alpha) — use 6 = RGBA
    ihdr_data = struct.pack(">IIBBBB B", width, height, 8, 6, 0, 0, 0)
    ihdr = _pack_chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0))

    raw_rows = []
    for row in pixels:
        row_bytes = b"\x00"  # filter type None
        for r, g, b, a in row:
            row_bytes += struct.pack("BBBB", r, g, b, a)
        raw_rows.append(row_bytes)

    compressed = zlib.compress(b"".join(raw_rows), 9)
    idat = _pack_chunk(b"IDAT", compressed)
    iend = _pack_chunk(b"IEND", b"")

    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "wb") as f:
        f.write(sig + ihdr + idat + iend)

def make_pixels() -> list[list[tuple[int,int,int,int]]]:
    return [[(0, 0, 0, 255)] * 128 for _ in range(128)]

def fill_rect(pixels, u, v, w, h, colour):
    """Fill rectangle [u, u+w) × [v, v+h) with colour (R,G,B,A)."""
    for row in range(v, min(v + h, 128)):
        for col in range(u, min(u + w, 128)):
            pixels[row][col] = colour

def add_border(pixels, u, v, w, h, border_colour=(0, 0, 0, 255), thickness=1):
    """Draw a 1-px dark border around a rect to make regions visible."""
    for t in range(thickness):
        for col in range(u, min(u + w, 128)):
            if v + t < 128:
                pixels[v + t][col] = border_colour
            if v + h - 1 - t >= 0:
                pixels[v + h - 1 - t][col] = border_colour
        for row in range(v, min(v + h, 128)):
            if u + t < 128:
                pixels[row][u + t] = border_colour
            if u + w - 1 - t >= 0:
                pixels[row][u + w - 1 - t] = border_colour

def draw_region(pixels, u, v, w, h, fill, label=None):
    fill_rect(pixels, u, v, w, h, fill)
    add_border(pixels, u, v, w, h)

# ─── Infernal Drake UV regions ─────────────────────────────────────────────
# All box-UV sizes: width = 2*D + 2*W,  height = D + H
# Cube notation: (W, H, D)

DRAKE_REGIONS = [
    # (u, v, box_w, box_h, colour_RGB, label)
    # Q1 TL — Body & Neck
    (0,  0,  56, 28, (150, 80,  60),  "Body"),         # (10,10,18)
    (0,  28, 24, 12, (160, 90,  70),  "Neck-Base"),    # (6,6,6)
    (0,  40, 22, 11, (170, 100, 75),  "Neck-Mid"),     # (5,5,6)
    (22, 40, 20, 10, (180, 110, 80),  "Neck-Upper"),   # (4,4,6)
    # Q2 TR — Head, Jaw, Horns
    (64, 0,  36, 18, (200, 60,  40),  "Head/Skull"),   # (8,8,10)
    (64, 18, 32, 11, (210, 70,  50),  "Jaw"),          # (8,3,8)
    (64, 29, 8,  8,  (220, 80,  60),  "Horn-L"),       # (2,6,2)
    (72, 29, 8,  8,  (220, 80,  60),  "Horn-R"),       # (2,6,2)
    # Q3 BL — Legs & Tail
    (0,  64, 12, 9,  (80,  120, 60),  "FrontLeg-Up"),  # (3,6,3)
    (12, 64, 12, 8,  (90,  130, 65),  "FrontLeg-Dn"),  # (3,5,3)
    (24, 64, 16, 12, (60,  100, 50),  "BackLeg-Up"),   # (4,8,4)
    (40, 64, 16, 10, (70,  110, 55),  "BackLeg-Dn"),   # (4,6,4)
    (0,  76, 20, 10, (40,  90,  40),  "Tail-1"),       # (4,4,6)
    (20, 76, 20, 10, (45,  95,  42),  "Tail-2"),       # (4,4,6)
    (40, 76, 16, 8,  (50, 100,  44),  "Tail-3"),       # (3,3,5)
    (0,  86, 14, 7,  (55, 105,  46),  "Tail-4"),       # (3,3,4)
    (14, 86, 12, 6,  (60, 110,  48),  "Tail-5"),       # (2,2,4)
    # Q4 BR — Wings
    (64, 64, 12, 13, (60,  60,  180), "WingBone-L"),   # (3,10,3)
    (76, 64, 12, 13, (65,  65,  185), "WingBone-R"),   # (3,10,3)  mirror
    # Wing membrane per-face UV
    (64, 77, 14, 18, (80,  80,  220), "WingMem-Up"),   # top face
    (78, 77, 14, 18, (85,  85,  225), "WingMem-Dn"),   # down face
    (64, 95, 14,  1, (90,  90,  230), "WingMem-N"),    # north
    (78, 95, 14,  1, (92,  92,  232), "WingMem-S"),    # south
    (92, 77,  1, 18, (95,  95,  235), "WingMem-E"),    # east
    (93, 77,  1, 18, (97,  97,  237), "WingMem-W"),    # west
]

# ─── Frost Wyvern UV regions ───────────────────────────────────────────────

WYVERN_REGIONS = [
    # Q1 TL — Body & Neck
    (0,  0,  56, 28, (60, 120, 180),  "Body"),         # (8,8,20)
    (0,  28, 20, 10, (70, 130, 190),  "Neck-1"),       # (4,4,6)
    (20, 28, 20, 10, (75, 135, 195),  "Neck-2"),       # (4,4,6)
    (40, 28, 20, 10, (80, 140, 200),  "Neck-3"),       # (4,4,6)
    (0,  38, 20, 10, (85, 145, 205),  "Neck-4"),       # (4,4,6)
    (0,  48, 28, 14, (90, 150, 210),  "Head"),         # (6,6,8)
    (28, 48, 16,  7, (95, 155, 215),  "Snout"),        # (3,2,5)
    # Q3 BL — Legs & Tail
    (0,  64, 16, 14, (50, 160, 120),  "BackLeg-Up"),   # (4,10,4)
    (16, 64, 12,  9, (55, 165, 125),  "BackLeg-Dn"),   # (3,6,3)
    (0,  78, 18,  9, (30, 140, 100),  "Tail-1"),       # (3,3,6)
    (18, 78, 18,  9, (32, 142, 102),  "Tail-2"),       # (3,3,6)
    (36, 78, 16,  8, (34, 144, 104),  "Tail-3"),       # (3,3,5)
    (0,  87, 14,  7, (36, 146, 106),  "Tail-4"),       # (2,2,5)
    (14, 87, 12,  6, (38, 148, 108),  "Tail-5"),       # (2,2,4)
    (26, 87, 12,  6, (40, 150, 110),  "Tail-6"),       # (2,2,4)
    # Q2 TR — Wings (primary limbs)
    (64,  0, 16, 16, (30,  80, 200),  "WingArm-Up"),   # (4,12,4)
    (80,  0, 12, 13, (35,  85, 205),  "WingForearm"),  # (3,10,3)
    # Wing membrane per-face UV
    (64, 16, 18, 20, (40,  90, 220),  "WingMem-Up"),   # top
    (82, 16, 18, 20, (45,  95, 225),  "WingMem-Dn"),   # down
    (64, 36, 18,  1, (50, 100, 230),  "WingMem-N"),    # north
    (82, 36, 18,  1, (52, 102, 232),  "WingMem-S"),    # south
    (100,16,  1, 20, (55, 105, 235),  "WingMem-E"),    # east
    (101,16,  1, 20, (57, 107, 237),  "WingMem-W"),    # west
]


def generate(output_path: str, regions, base_bg=(30, 30, 30)):
    pixels = [[(base_bg[0], base_bg[1], base_bg[2], 255)] * 128
              for _ in range(128)]
    for u, v, w, h, colour, label in regions:
        r, g, b = colour
        draw_region(pixels, u, v, w, h, (r, g, b, 255), label)
    write_png(output_path, pixels)
    print(f"  Written: {output_path}")


if __name__ == "__main__":
    base = Path(__file__).parent.parent / "src/main/resources/assets/emberaether/textures/entity"
    base.mkdir(parents=True, exist_ok=True)

    print("Generating placeholder textures…")
    generate(str(base / "infernal_drake.png"), DRAKE_REGIONS, base_bg=(20, 10, 5))
    generate(str(base / "frost_wyvern.png"),   WYVERN_REGIONS, base_bg=(5, 10, 20))
    print("Done. Replace these with your final artwork!")
