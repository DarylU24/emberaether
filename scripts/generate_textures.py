#!/usr/bin/env python3
"""
generate_textures.py  —  EmberAether premium dragon textures (128×128 RGBA)

Generates 5 variant PNGs per breed (10 total):
  Infernal Drake : common, rare, elite, molten, void
  Frost Wyvern   : common, rare, elite, blizzard, storm

Each PNG uses face-specific painting:
  • Body dorsal vs belly have distinct colours
  • All solid areas show a 4-pixel fish-scale tile pattern (highlight / base / shadow)
  • Wing membranes have radiating diagonal veins
  • Head front (north) face has full face detail:
      – brow-ridge highlight, two separated 2×2 slit eyes (L + R), nostril dots,
        jaw-line shadow, and ivory fang tips on the jaw's front face
  • Horns have a base-to-tip gradient
  • Rare/elite/molten/void variants add crack/vein overlays

UV layout strictly follows the Bedrock box-UV convention so every region
maps cleanly with zero pixel overlap.

Usage:  python3 scripts/generate_textures.py
"""

import struct, zlib, os, random
from pathlib import Path

SIZE = 128

# ─── PNG helpers ──────────────────────────────────────────────────────────────

def _pack_chunk(ct: bytes, data: bytes) -> bytes:
    crc = zlib.crc32(ct + data) & 0xFFFFFFFF
    return struct.pack(">I", len(data)) + ct + data + struct.pack(">I", crc)

def write_png(path: str, pixels):
    sig  = b"\x89PNG\r\n\x1a\n"
    ihdr = _pack_chunk(b"IHDR", struct.pack(">IIBBBBB", SIZE, SIZE, 8, 6, 0, 0, 0))
    raw  = b"".join(b"\x00" + bytes(b for px in row for b in px) for row in pixels)
    idat = _pack_chunk(b"IDAT", zlib.compress(raw, 9))
    iend = _pack_chunk(b"IEND", b"")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "wb") as f:
        f.write(sig + ihdr + idat + iend)
    print(f"  → {path}")

def make_canvas(bg=(0, 0, 0)) -> list:
    return [[(bg[0], bg[1], bg[2], 255)] * SIZE for _ in range(SIZE)]

# ─── Colour math ──────────────────────────────────────────────────────────────

def cl(v): return max(0, min(255, int(round(v))))
def clamp3(r, g, b): return (cl(r), cl(g), cl(b))
def blend(c1, c2, t): return tuple(cl(c1[i]*(1-t) + c2[i]*t) for i in range(3))
def lighten(c, n=30): return clamp3(c[0]+n, c[1]+n, c[2]+n)
def darken(c, n=30):  return clamp3(c[0]-n, c[1]-n, c[2]-n)
def tint(c, r=0, g=0, b=0): return clamp3(c[0]+r, c[1]+g, c[2]+b)

# ─── Draw primitives ──────────────────────────────────────────────────────────

def put(px, x, y, c):
    if 0 <= x < SIZE and 0 <= y < SIZE:
        px[y][x] = (c[0], c[1], c[2], 255)

def fill(px, u, v, w, h, c):
    for y in range(v, min(v+h, SIZE)):
        for x in range(u, min(u+w, SIZE)):
            put(px, x, y, c)

# ─── Fish-scale diamond tile ──────────────────────────────────────────────────
# 4×4 tile; values: 0=shadow, 1=base, 2=highlight
_TILE = [
    [0, 2, 2, 0],
    [2, 2, 1, 1],
    [2, 1, 1, 0],
    [0, 0, 0, 0],
]

def _scale_color(base, val):
    if val == 2: return lighten(base, 35)
    if val == 0: return darken(base, 40)
    return base

def draw_scales(px, u, v, w, h, base, scale=4):
    """Fish-scale tile pattern with hex offset on alternate rows."""
    for py in range(h):
        ay = v + py
        tile_row = ay // scale
        offset   = (tile_row % 2) * (scale // 2)
        ty = ay % scale
        for px_ in range(w):
            ax = u + px_
            tx = (ax + offset) % scale
            put(px, ax, ay, _scale_color(base, _TILE[ty][tx]))

def draw_gradient_scales(px, u, v, w, h, c_top, c_bot, scale=4):
    """Scale pattern where base colour transitions top→bottom."""
    for py in range(h):
        ay  = v + py
        t   = py / max(h-1, 1)
        base = blend(c_top, c_bot, t)
        tile_row = ay // scale
        offset   = (tile_row % 2) * (scale // 2)
        ty = ay % scale
        for px_ in range(w):
            ax = u + px_
            tx = (ax + offset) % scale
            put(px, ax, ay, _scale_color(base, _TILE[ty][tx]))

# ─── Gradient fill (no scales) ────────────────────────────────────────────────

def draw_gradient(px, u, v, w, h, c_top, c_bot):
    for py in range(h):
        t = py / max(h-1, 1)
        c = blend(c_top, c_bot, t)
        for px_ in range(w):
            put(px, u+px_, v+py, c)

def draw_gradient_h(px, u, v, w, h, c_left, c_right):
    for py in range(h):
        for px_ in range(w):
            t = px_ / max(w-1, 1)
            put(px, u+px_, v+py, blend(c_left, c_right, t))

# ─── Wing membrane with diagonal veins ────────────────────────────────────────

def draw_membrane(px, u, v, w, h, base, vein, spacing=4):
    fill(px, u, v, w, h, base)
    for k in range(0, w + h, spacing):
        for py in range(h):
            ppx = k - py
            if 0 <= ppx < w:
                put(px, u+ppx, v+py, vein)
    # Edge fade
    for py in range(h):
        for ppx in range(w):
            e = min(ppx, w-1-ppx, py, h-1-py)
            if e < 3:
                r, g, b, _ = px[v+py][u+ppx]
                f = 0.45 + e * 0.18
                put(px, u+ppx, v+py, clamp3(r*f, g*f, b*f))

# ─── Horn gradient ────────────────────────────────────────────────────────────

def draw_horn(px, u, v, w, h, base, tip):
    """Gradient from base (bottom, high v) to tip (top, low v)."""
    for py in range(h):
        t = 1.0 - py / max(h-1, 1)   # 1 at top (tip), 0 at bottom (base)
        c = blend(base, tip, t)
        for ppx in range(w):
            put(px, u+ppx, v+py, c)

# ─── Detailed face painters ───────────────────────────────────────────────────

def _eye_2x2(px, u, v, iris, pupil_left=True):
    """Paint a 2×2 slit eye at (u,v).

    pupil_left=True  (left eye):   sclera | glint   /  black | iris
    pupil_left=False (right eye):  glint  | sclera  /  iris  | black
    """
    sclera = (min(255, iris[0]+100), min(255, iris[1]+100), min(255, iris[2]+100))
    glint  = (255, 255, 255)
    black  = (0, 0, 0)
    if pupil_left:
        put(px, u,   v,   sclera);  put(px, u+1, v,   glint)
        put(px, u,   v+1, black);   put(px, u+1, v+1, iris)
    else:
        put(px, u,   v,   glint);   put(px, u+1, v,   sclera)
        put(px, u,   v+1, iris);    put(px, u+1, v+1, black)

def draw_drake_face(px, nu, nv, nw, nh, p):
    """Overlay premium face features on the drake head's north (front) face.

    Assumes the face has already been painted with scales.  Works for an 8×8
    region (nw=8, nh=8).  Adds:
      • brow-ridge highlight across the top row
      • two separate 2×2 slit eyes (left third / right third of face)
      • nostril dots on the lower half of the face
      • jaw-line shadow on the bottom row
    """
    eye_c   = p['eye']
    head_c  = p['head_top']
    head_s  = p['head_side']
    nostril = darken(head_s, 55)

    # Brow highlight (top row)
    brow = lighten(head_c, 30)
    for col in range(nw):
        put(px, nu+col, nv, brow)

    # Brow shadow (row 1)
    for col in range(nw):
        put(px, nu+col, nv+1, darken(head_c, 8))

    # Left eye — cols 1-2, rows 2-3
    _eye_2x2(px, nu+1, nv+2, eye_c, pupil_left=True)

    # Right eye — cols 5-6, rows 2-3 (mirrored)
    _eye_2x2(px, nu+5, nv+2, eye_c, pupil_left=False)

    # Cheek shadow (row 4)
    for col in range(nw):
        r, g, b, _ = px[nv+4][nu+col]
        put(px, nu+col, nv+4, darken((r,g,b), 12))

    # Nostril dots (row 5, cols 2 and 5)
    if nh > 5:
        put(px, nu+2, nv+5, nostril)
        put(px, nu+5, nv+5, nostril)

    # Jaw-line shadow (bottom row)
    for col in range(nw):
        r, g, b, _ = px[nv+nh-1][nu+col]
        put(px, nu+col, nv+nh-1, darken((r,g,b), 22))

def draw_jaw_teeth(px, nu, nv, nw, nh, p):
    """Paint ivory fang tips on the bottom row of the jaw's north face.

    Fangs appear at alternating columns; the row above shows the fang body
    blended between jaw colour and ivory.
    """
    jaw_c    = p['jaw_in']
    ivory    = (228, 218, 190)  # bone/tooth colour independent of variant

    bottom = nv + nh - 1
    for col in (1, 3, 5):
        if nu + col < SIZE:
            put(px, nu+col, bottom, ivory)
            if nh >= 2:
                put(px, nu+col, bottom-1, blend(jaw_c, ivory, 0.45))

def draw_wyvern_face(px, nu, nv, nw, nh, p):
    """Overlay premium face features on the wyvern head's north (front) face.

    Works for a 6×6 region (nw=6, nh=6).  Adds:
      • brow-ridge highlight (top row)
      • two 2×2 slit eyes in the upper half
      • nostril dots in the lower half
      • jaw-line shadow on the bottom row
    """
    eye_c   = p['eye']
    head_c  = p['head_top']
    head_s  = p['head_side']
    nostril = darken(head_s, 55)

    # Brow highlight
    brow = lighten(head_c, 30)
    for col in range(nw):
        put(px, nu+col, nv, brow)

    # Left eye — cols 1-2, rows 1-2
    _eye_2x2(px, nu+1, nv+1, eye_c, pupil_left=True)

    # Right eye — cols 3-4, rows 1-2 (mirrored)
    _eye_2x2(px, nu+3, nv+1, eye_c, pupil_left=False)

    # Nostril dots (row 4, cols 1 and 4)
    if nh > 4:
        put(px, nu+1, nv+4, nostril)
        put(px, nu+4, nv+4, nostril)

    # Jaw-line shadow (bottom row)
    for col in range(nw):
        r, g, b, _ = px[nv+nh-1][nu+col]
        put(px, nu+col, nv+nh-1, darken((r,g,b), 22))

def draw_snout_nostrils(px, nu, nv, nw, nh, p):
    """Paint two nostril dots on the wyvern snout's north face (3×2)."""
    nostril = darken(p['snout'], 55)
    if nh >= 2:
        put(px, nu,       nv+nh-1, nostril)
        put(px, nu+nw-1,  nv+nh-1, nostril)

# ─── Crack / energy vein overlay ─────────────────────────────────────────────

def _line(px, x0, y0, x1, y1, c):
    dx = abs(x1-x0); dy = abs(y1-y0)
    sx = 1 if x0 < x1 else -1
    sy = 1 if y0 < y1 else -1
    err = dx - dy
    while True:
        put(px, x0, y0, c)
        if x0 == x1 and y0 == y1: break
        e2 = 2*err
        if e2 > -dy: err -= dy; x0 += sx
        if e2 <  dx: err += dx; y0 += sy

def draw_cracks(px, segments, c):
    """Draw a list of (x0,y0,x1,y1) line segments in colour c."""
    for x0, y0, x1, y1 in segments:
        _line(px, x0, y0, x1, y1, c)
        # Glow: one-pixel softer halo
        halo = darken(lighten(c, 0), -60)  # just a dim version
        for dx in (-1, 0, 1):
            for dy in (-1, 0, 1):
                if dx != 0 or dy != 0:
                    _line(px, x0+dx, y0+dy, x1+dx, y1+dy,
                          (cl(c[0]*0.4), cl(c[1]*0.4), cl(c[2]*0.4)))

# ─── Box-UV face calculator ───────────────────────────────────────────────────

def faces(u0, v0, W, H, D) -> dict:
    """Return UV rect for each face of a Bedrock box-UV cube (W×H×D) at (u0,v0)."""
    return {
        'top':    (u0+D,       v0,   W, D),
        'bottom': (u0+D+W,     v0,   W, D),
        'east':   (u0,         v0+D, D, H),
        'north':  (u0+D,       v0+D, W, H),
        'west':   (u0+D+W,     v0+D, D, H),
        'south':  (u0+D+W+D,   v0+D, W, H),
    }

# ─────────────────────────────────────────────────────────────────────────────
#   COLOUR PALETTES
# ─────────────────────────────────────────────────────────────────────────────

DRAKE_PALETTES = {
    "common": {
        "dorsal":       (48, 42, 42),
        "belly":        (118, 32, 32),
        "side_top":     (58, 50, 48),
        "side_bot":     (100, 35, 35),
        "neck":         (55, 48, 46),
        "head_top":     (52, 44, 43),
        "head_side":    (62, 52, 50),
        "jaw_in":       (100, 25, 25),
        "horn_base":    (68, 60, 54),
        "horn_tip":     (200, 188, 165),
        "eye":          (255, 175, 0),
        "limb":         (52, 46, 44),
        "tail_base":    (55, 48, 46),
        "tail_tip":     (70, 62, 58),
        "wing_bone":    (40, 36, 35),
        "wing_top":     (90, 26, 26),
        "wing_bot":     (68, 18, 18),
        "wing_vein":    (148, 52, 18),
        "bg":           (15, 8, 6),
        "cracks":       None,
    },
    "rare": {
        "dorsal":       (18, 15, 22),
        "belly":        (22, 16, 25),
        "side_top":     (22, 18, 28),
        "side_bot":     (16, 12, 20),
        "neck":         (20, 17, 24),
        "head_top":     (20, 16, 26),
        "head_side":    (24, 20, 30),
        "jaw_in":       (200, 80, 10),
        "horn_base":    (22, 18, 28),
        "horn_tip":     (240, 115, 0),
        "eye":          (255, 120, 0),
        "limb":         (20, 16, 25),
        "tail_base":    (20, 17, 24),
        "tail_tip":     (25, 20, 30),
        "wing_bone":    (16, 13, 20),
        "wing_top":     (35, 20, 5),
        "wing_bot":     (22, 14, 3),
        "wing_vein":    (220, 100, 0),
        "bg":           (8, 6, 12),
        "cracks":       (230, 100, 0),
    },
    "elite": {
        "dorsal":       (28, 20, 10),
        "belly":        (85, 42, 0),
        "side_top":     (36, 24, 12),
        "side_bot":     (70, 36, 0),
        "neck":         (32, 22, 12),
        "head_top":     (30, 22, 10),
        "head_side":    (38, 26, 12),
        "jaw_in":       (255, 148, 0),
        "horn_base":    (30, 22, 10),
        "horn_tip":     (255, 200, 0),
        "eye":          (255, 255, 0),
        "limb":         (32, 24, 12),
        "tail_base":    (30, 22, 12),
        "tail_tip":     (45, 34, 18),
        "wing_bone":    (24, 17, 8),
        "wing_top":     (65, 32, 5),
        "wing_bot":     (42, 22, 3),
        "wing_vein":    (255, 160, 0),
        "bg":           (10, 6, 3),
        "cracks":       (255, 175, 0),
    },
    "molten": {                         # NEW – red-hot metallic
        "dorsal":       (185, 62, 0),
        "belly":        (255, 200, 95),
        "side_top":     (205, 80, 10),
        "side_bot":     (240, 160, 40),
        "neck":         (195, 72, 5),
        "head_top":     (200, 74, 5),
        "head_side":    (215, 86, 10),
        "jaw_in":       (255, 240, 175),
        "horn_base":    (185, 62, 0),
        "horn_tip":     (255, 240, 195),
        "eye":          (255, 255, 255),
        "limb":         (190, 68, 5),
        "tail_base":    (195, 72, 5),
        "tail_tip":     (215, 90, 15),
        "wing_bone":    (165, 52, 0),
        "wing_top":     (225, 105, 12),
        "wing_bot":     (200, 82, 5),
        "wing_vein":    (255, 232, 148),
        "bg":           (28, 8, 0),
        "cracks":       (255, 240, 100),
    },
    "void": {                           # NEW – deep purple / shadow energy
        "dorsal":       (30, 10, 48),
        "belly":        (25, 8, 40),
        "side_top":     (40, 14, 62),
        "side_bot":     (22, 7, 34),
        "neck":         (35, 12, 54),
        "head_top":     (34, 11, 52),
        "head_side":    (44, 16, 66),
        "jaw_in":       (140, 50, 225),
        "horn_base":    (30, 10, 48),
        "horn_tip":     (200, 100, 255),
        "eye":          (180, 80, 255),
        "limb":         (32, 11, 50),
        "tail_base":    (34, 12, 52),
        "tail_tip":     (44, 18, 66),
        "wing_bone":    (24, 8, 38),
        "wing_top":     (52, 16, 82),
        "wing_bot":     (36, 10, 58),
        "wing_vein":    (165, 62, 255),
        "bg":           (10, 3, 16),
        "cracks":       (175, 75, 255),
    },
}

WYVERN_PALETTES = {
    "common": {
        "dorsal":       (55, 125, 200),
        "belly":        (215, 232, 255),
        "side_top":     (75, 148, 215),
        "side_bot":     (185, 215, 250),
        "neck":         (68, 138, 208),
        "head_top":     (62, 130, 200),
        "head_side":    (72, 142, 210),
        "snout":        (70, 136, 205),
        "eye":          (255, 255, 255),
        "leg":          (62, 128, 196),
        "tail_base":    (66, 136, 205),
        "tail_tip":     (88, 158, 220),
        "wing_bone":    (52, 118, 182),
        "wing_top":     (88, 168, 238),
        "wing_bot":     (148, 208, 255),
        "wing_vein":    (200, 228, 255),
        "bg":           (4, 8, 20),
        "cracks":       None,
    },
    "rare": {
        "dorsal":       (0, 175, 198),
        "belly":        (175, 238, 242),
        "side_top":     (8, 195, 212),
        "side_bot":     (150, 232, 238),
        "neck":         (4, 182, 204),
        "head_top":     (2, 178, 200),
        "head_side":    (8, 192, 212),
        "snout":        (4, 182, 204),
        "eye":          (0, 255, 255),
        "leg":          (0, 172, 195),
        "tail_base":    (4, 182, 204),
        "tail_tip":     (14, 205, 222),
        "wing_bone":    (0, 162, 185),
        "wing_top":     (8, 188, 210),
        "wing_bot":     (148, 232, 240),
        "wing_vein":    (218, 245, 248),
        "bg":           (2, 6, 14),
        "cracks":       (180, 248, 255),
    },
    "elite": {
        "dorsal":       (10, 30, 92),
        "belly":        (20, 54, 128),
        "side_top":     (14, 40, 112),
        "side_bot":     (18, 50, 125),
        "neck":         (12, 34, 100),
        "head_top":     (11, 32, 96),
        "head_side":    (14, 40, 108),
        "snout":        (12, 34, 100),
        "eye":          (218, 238, 255),
        "leg":          (12, 32, 96),
        "tail_base":    (12, 34, 100),
        "tail_tip":     (8, 24, 72),
        "wing_bone":    (8, 24, 76),
        "wing_top":     (14, 40, 112),
        "wing_bot":     (18, 50, 130),
        "wing_vein":    (198, 224, 255),
        "bg":           (2, 4, 16),
        "cracks":       (200, 226, 255),
    },
    "blizzard": {                       # NEW – white / silver ice-crystal
        "dorsal":       (218, 224, 235),
        "belly":        (255, 255, 255),
        "side_top":     (232, 238, 248),
        "side_bot":     (248, 252, 255),
        "neck":         (225, 230, 240),
        "head_top":     (222, 228, 238),
        "head_side":    (234, 240, 248),
        "snout":        (226, 232, 241),
        "eye":          (100, 178, 255),
        "leg":          (222, 228, 238),
        "tail_base":    (224, 230, 240),
        "tail_tip":     (244, 248, 253),
        "wing_bone":    (208, 218, 230),
        "wing_top":     (228, 236, 248),
        "wing_bot":     (246, 250, 255),
        "wing_vein":    (148, 198, 255),
        "bg":           (8, 12, 24),
        "cracks":       (148, 198, 255),
    },
    "storm": {                          # NEW – dark storm-grey + electric blue
        "dorsal":       (44, 50, 62),
        "belly":        (62, 70, 86),
        "side_top":     (52, 60, 74),
        "side_bot":     (60, 68, 84),
        "neck":         (48, 55, 66),
        "head_top":     (46, 52, 64),
        "head_side":    (56, 63, 76),
        "snout":        (48, 55, 66),
        "eye":          (48, 178, 255),
        "leg":          (46, 52, 64),
        "tail_base":    (48, 55, 66),
        "tail_tip":     (40, 46, 56),
        "wing_bone":    (38, 44, 56),
        "wing_top":     (54, 62, 76),
        "wing_bot":     (64, 72, 88),
        "wing_vein":    (48, 148, 255),
        "bg":           (6, 8, 14),
        "cracks":       (48, 152, 255),
    },
}

# ─────────────────────────────────────────────────────────────────────────────
#   DRAKE PAINTER
# ─────────────────────────────────────────────────────────────────────────────

# Predefined crack/vein line segments (x0,y0,x1,y1) in texture space
DRAKE_CRACK_SEGS = [
    # Body area
    (10, 6,  14, 12),  (14, 12, 12, 18),
    (22, 4,  25, 9),   (25, 9,  28, 15),
    (38, 10, 40, 16),
    # Head area
    (68, 2,  72, 7),
    # Wing membrane
    (70, 82, 74, 90),  (74, 90, 72, 95),
]

WYVERN_CRACK_SEGS = [
    # Body
    (5,  2,  9,  8),   (9,  8,  7, 14),
    (25, 4,  28, 10),
    # Head
    (4,  52, 7,  57),
    # Wing membrane
    (68, 20, 74, 30),  (74, 30, 70, 38),
]

def paint_drake(px, p):
    bg = p['bg']

    # ── Body (10,10,18) at UV(0,0) ────────────────────────────────────────────
    f = faces(0, 0, 10, 10, 18)
    draw_scales(px,          *f['top'],    p['dorsal'])
    draw_scales(px,          *f['bottom'], p['belly'])
    draw_gradient_scales(px, *f['east'],   p['side_top'], p['side_bot'])
    draw_gradient_scales(px, *f['north'],  p['side_top'], p['side_bot'])
    draw_gradient_scales(px, *f['west'],   p['side_top'], p['side_bot'])
    draw_gradient_scales(px, *f['south'],  p['side_top'], p['side_bot'])
    # Dorsal spine ridge: 1-px highlight down centre of top face
    tu, tv, tw, th = f['top']
    cx = tu + tw // 2
    for sy in range(tv, tv + th):
        put(px, cx, sy, lighten(p['dorsal'], 22))

    # ── Neck segments ─────────────────────────────────────────────────────────
    neck_cubes = [(0,28,6,6,6), (0,40,5,5,6), (22,40,4,4,6)]
    for i, (u0, v0, W, H, D) in enumerate(neck_cubes):
        tone = blend(p['neck'], p['head_top'], i / max(len(neck_cubes)-1, 1))
        f = faces(u0, v0, W, H, D)
        for r in f.values():
            draw_scales(px, *r, tone)

    # ── Head (8,8,10) at UV(64,0) ─────────────────────────────────────────────
    f = faces(64, 0, 8, 8, 10)
    draw_scales(px,          *f['top'],    p['head_top'])
    draw_scales(px,          *f['bottom'], darken(p['jaw_in'], 18))
    draw_gradient_scales(px, *f['east'],   p['head_top'], p['head_side'])
    draw_gradient_scales(px, *f['north'],  p['head_top'], p['head_side'])  # front
    draw_gradient_scales(px, *f['west'],   p['head_top'], p['head_side'])
    draw_scales(px,          *f['south'],  p['head_top'])
    # Detailed face on the front (north) face
    draw_drake_face(px, *f['north'], p)

    # ── Jaw (8,3,8) at UV(64,18) ──────────────────────────────────────────────
    f = faces(64, 18, 8, 3, 8)
    draw_scales(px, *f['top'],    p['jaw_in'])
    fill(px,        *f['bottom'], lighten(p['jaw_in'], 22))
    for side in ('east', 'north', 'west', 'south'):
        draw_scales(px, *f[side], p['jaw_in'])
    # Fang tips on the front of the jaw
    draw_jaw_teeth(px, *f['north'], p)

    # ── Horns (2,6,2) at UV(64,29) and UV(72,29) ──────────────────────────────
    for u0 in (64, 72):
        f = faces(u0, 29, 2, 6, 2)
        for side in ('east', 'north', 'west', 'south'):
            draw_horn(px, *f[side], p['horn_base'], p['horn_tip'])
        draw_horn(px, *f['top'],    p['horn_base'], lighten(p['horn_tip'], 15))
        fill(px,      *f['bottom'], darken(p['horn_base'], 20))

    # ── Front legs: upper (3,6,3) at (0,64), lower (3,5,3) at (12,64) ────────
    for u0, v0, W, H, D in [(0,64,3,6,3), (12,64,3,5,3)]:
        f = faces(u0, v0, W, H, D)
        for r in f.values():
            draw_scales(px, *r, p['limb'])

    # ── Back legs: upper (4,8,4) at (24,64), lower (4,6,4) at (40,64) ────────
    for u0, v0, W, H, D in [(24,64,4,8,4), (40,64,4,6,4)]:
        f = faces(u0, v0, W, H, D)
        draw_gradient_scales(px, *f['north'], p['limb'], darken(p['limb'], 12))
        draw_gradient_scales(px, *f['west'],  p['limb'], darken(p['limb'], 12))
        draw_gradient_scales(px, *f['east'],  p['limb'], darken(p['limb'], 12))
        draw_gradient_scales(px, *f['south'], p['limb'], darken(p['limb'], 12))
        draw_scales(px, *f['top'],    lighten(p['limb'], 10))
        fill(px,    *f['bottom'],     darken(p['limb'], 15))

    # ── Tail segments: taper from base to tip ─────────────────────────────────
    tail_segs = [(0,76,4,4,6),(20,76,4,4,6),(40,76,3,3,5),(0,86,3,3,4),(14,86,2,2,4)]
    for i, (u0, v0, W, H, D) in enumerate(tail_segs):
        t = i / (len(tail_segs)-1)
        tone = blend(p['tail_base'], p['tail_tip'], t)
        f = faces(u0, v0, W, H, D)
        draw_scales(px, *f['top'],    darken(tone, 12))
        draw_scales(px, *f['bottom'], lighten(tone, 8))
        for side in ('east', 'north', 'west', 'south'):
            draw_scales(px, *f[side], tone)

    # ── Wing bones: (3,10,3) at (64,64) and (76,64) ───────────────────────────
    for u0 in (64, 76):
        f = faces(u0, 64, 3, 10, 3)
        draw_gradient(px, *f['north'], p['wing_bone'], lighten(p['wing_bone'], 22))
        draw_gradient(px, *f['east'],  p['wing_bone'], lighten(p['wing_bone'], 18))
        draw_gradient(px, *f['west'],  p['wing_bone'], lighten(p['wing_bone'], 18))
        fill(px, *f['top'],    lighten(p['wing_bone'], 10))
        fill(px, *f['bottom'], darken(p['wing_bone'], 10))
        fill(px, *f['south'],  p['wing_bone'])

    # ── Wing membranes (per-face UV) ──────────────────────────────────────────
    draw_membrane(px, 64, 77, 14, 18, p['wing_top'], p['wing_vein'], 4)
    draw_membrane(px, 78, 77, 14, 18, p['wing_bot'], darken(p['wing_vein'], 15), 4)
    fill(px, 64, 95, 14,  1, p['wing_top'])
    fill(px, 78, 95, 14,  1, p['wing_bot'])
    fill(px, 92, 77,  1, 18, p['wing_bone'])
    fill(px, 93, 77,  1, 18, p['wing_bone'])

    # ── Variant crack / energy-vein overlay ───────────────────────────────────
    if p['cracks']:
        draw_cracks(px, DRAKE_CRACK_SEGS, p['cracks'])


# ─────────────────────────────────────────────────────────────────────────────
#   WYVERN PAINTER
# ─────────────────────────────────────────────────────────────────────────────

def paint_wyvern(px, p):
    bg = p['bg']

    # ── Body (8,8,20) at UV(0,0) ──────────────────────────────────────────────
    f = faces(0, 0, 8, 8, 20)
    draw_scales(px,          *f['top'],    p['dorsal'])
    draw_scales(px,          *f['bottom'], p['belly'])
    draw_gradient_scales(px, *f['east'],   p['side_top'], p['side_bot'])
    draw_gradient_scales(px, *f['north'],  p['side_top'], p['side_bot'])
    draw_gradient_scales(px, *f['west'],   p['side_top'], p['side_bot'])
    draw_gradient_scales(px, *f['south'],  p['side_top'], p['side_bot'])
    # Dorsal ridge
    tu, tv, tw, th = f['top']
    cx = tu + tw // 2
    for sy in range(tv, tv + th):
        put(px, cx, sy, lighten(p['dorsal'], 20))

    # ── Neck segments (4 segs, each 4,4,6) ────────────────────────────────────
    neck_cubes = [(0,28,4,4,6),(20,28,4,4,6),(40,28,4,4,6),(0,38,4,4,6)]
    for i, (u0,v0,W,H,D) in enumerate(neck_cubes):
        tone = blend(p['neck'], p['head_top'], i / max(len(neck_cubes)-1, 1))
        f = faces(u0, v0, W, H, D)
        for r in f.values():
            draw_scales(px, *r, tone)

    # ── Head (6,6,8) at UV(0,48) ──────────────────────────────────────────────
    f = faces(0, 48, 6, 6, 8)
    draw_scales(px,          *f['top'],    p['head_top'])
    fill(px,                 *f['bottom'], darken(p['head_top'], 18))
    draw_gradient_scales(px, *f['east'],   p['head_top'], p['head_side'])
    draw_gradient_scales(px, *f['north'],  p['head_top'], p['head_side'])  # front
    draw_gradient_scales(px, *f['west'],   p['head_top'], p['head_side'])
    draw_scales(px,          *f['south'],  p['head_top'])
    # Detailed face on the front (north) face
    draw_wyvern_face(px, *f['north'], p)

    # ── Snout (3,2,5) at UV(28,48) ────────────────────────────────────────────
    f = faces(28, 48, 3, 2, 5)
    for r in f.values():
        draw_scales(px, *r, p['snout'])
    # Nostril dots on the snout front face
    draw_snout_nostrils(px, *f['north'], p)

    # ── Back legs: upper (4,10,4) at (0,64), lower (3,6,3) at (16,64) ────────
    for u0, v0, W, H, D in [(0,64,4,10,4),(16,64,3,6,3)]:
        f = faces(u0, v0, W, H, D)
        for r in f.values():
            draw_gradient_scales(px, *r, p['leg'], darken(p['leg'], 14))

    # ── Tail (6 segments) ─────────────────────────────────────────────────────
    tail_segs = [(0,78,3,3,6),(18,78,3,3,6),(36,78,3,3,5),(0,87,2,2,5),(14,87,2,2,4),(26,87,2,2,4)]
    for i, (u0,v0,W,H,D) in enumerate(tail_segs):
        t = i / (len(tail_segs)-1)
        tone = blend(p['tail_base'], p['tail_tip'], t)
        f = faces(u0, v0, W, H, D)
        draw_scales(px, *f['top'],    darken(tone, 12))
        draw_scales(px, *f['bottom'], lighten(tone, 8))
        for side in ('east','north','west','south'):
            draw_scales(px, *f[side], tone)

    # ── Wing upper arm (4,12,4) at UV(64,0) ───────────────────────────────────
    f = faces(64, 0, 4, 12, 4)
    draw_gradient(px, *f['north'], p['wing_bone'], lighten(p['wing_bone'], 20))
    draw_gradient(px, *f['east'],  p['wing_bone'], lighten(p['wing_bone'], 18))
    draw_gradient(px, *f['west'],  p['wing_bone'], lighten(p['wing_bone'], 18))
    fill(px, *f['top'],    lighten(p['wing_bone'], 10))
    fill(px, *f['bottom'], darken(p['wing_bone'], 10))
    fill(px, *f['south'],  p['wing_bone'])

    # ── Wing forearm (3,10,3) at UV(80,0) ─────────────────────────────────────
    f = faces(80, 0, 3, 10, 3)
    draw_gradient(px, *f['north'], p['wing_bone'], lighten(p['wing_bone'], 18))
    for side in ('east', 'west', 'top', 'bottom', 'south'):
        fill(px, *f[side], p['wing_bone'])

    # ── Wing membranes (per-face UV) ──────────────────────────────────────────
    draw_membrane(px, 64, 16, 18, 20, p['wing_top'], p['wing_vein'], 4)
    draw_membrane(px, 82, 16, 18, 20, p['wing_bot'], darken(p['wing_vein'], 15), 4)
    fill(px, 64, 36, 18,  1, p['wing_top'])
    fill(px, 82, 36, 18,  1, p['wing_bot'])
    fill(px, 100,16,  1, 20, p['wing_bone'])
    fill(px, 101,16,  1, 20, p['wing_bone'])

    # ── Variant crack / energy overlay ────────────────────────────────────────
    if p['cracks']:
        draw_cracks(px, WYVERN_CRACK_SEGS, p['cracks'])


# ─────────────────────────────────────────────────────────────────────────────
#   ENTRY POINT
# ─────────────────────────────────────────────────────────────────────────────

if __name__ == "__main__":
    base = (Path(__file__).parent.parent
            / "src/main/resources/assets/emberaether/textures/entity")
    base.mkdir(parents=True, exist_ok=True)

    print("Generating premium EmberAether textures…")

    for variant, pal in DRAKE_PALETTES.items():
        cvs = make_canvas(pal['bg'])
        paint_drake(cvs, pal)
        write_png(str(base / f"infernal_drake_{variant}.png"), cvs)

    for variant, pal in WYVERN_PALETTES.items():
        cvs = make_canvas(pal['bg'])
        paint_wyvern(cvs, pal)
        write_png(str(base / f"frost_wyvern_{variant}.png"), cvs)

    # Remove legacy alias PNGs if they still exist from a previous run
    import shutil
    for legacy in ("infernal_drake.png", "frost_wyvern.png"):
        p = base / legacy
        if p.exists():
            p.unlink()
            print(f"  Removed legacy alias: {p}")

    print("Done! All 10 variant textures generated.")
