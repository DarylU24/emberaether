# Dragon Mounts — Technical Specification Document
**Project:** EmberAether | **Target:** Minecraft Forge 1.16.5 (Java Edition)  
**Status:** Design Phase — No Final Code

---

## Table of Contents

1. [Entity Architecture & Geometry](#1-entity-architecture--geometry)
   - 1.1 [Breed A — Skittering Wyvern](#11-breed-a--skittering-wyvern)
   - 1.2 [Breed B — Goliath Drake](#12-breed-b--goliath-drake)
2. [Visuals & Variants](#2-visuals--variants)
   - 2.1 [Skittering Wyvern Variants](#21-skittering-wyvern-variants)
   - 2.2 [Goliath Drake Variants](#22-goliath-drake-variants)
3. [Immersive Behavior & Ecology](#3-immersive-behavior--ecology)
   - 3.1 [Spawning System](#31-spawning-system)
   - 3.2 [AI Goal Selector](#32-ai-goal-selector)
4. [Interaction Systems](#4-interaction-systems)
   - 4.1 [Taming — Multi-Stage Protocol](#41-taming--multi-stage-protocol)
   - 4.2 [Breeding — Genetic Inheritance](#42-breeding--genetic-inheritance)

---

## 1. Entity Architecture & Geometry

### Design Philosophy

All geometry is expressed in **Minecraft model units** (1 unit = 1/16 of a block).  
The box-model coordinate origin `(0, 0, 0)` is placed at the entity's **center of mass at ground level**.  
`rotationPoint` values are given as `(x, y, z)` offsets from the parent bone's pivot.  
All `ModelRenderer` declarations follow the pattern:

```
ModelRenderer(model, texOffsetX, texOffsetY)
  .setRotationPoint(px, py, pz)
  .addBox(offsetX, offsetY, offsetZ, width, height, depth)
```

Positive Y is **downward** in Minecraft's model space.

---

### 1.1 Breed A — Skittering Wyvern

**Design Archetype:** Pivot-Heavy. Each segment is a small, independent box linked to the previous, giving the creature a fluid, serpentine motion driven entirely by `setRotationAngles`.

**Bounding Box (Entity):** 1.4 W × 1.8 H blocks (tamed/resting); 2.8 W × 1.2 H (airborne, wings spread).

---

#### 1.1.1 Torso

The torso is the root bone. All other bones are children (directly or transitively).

| Property | Value |
|---|---|
| `texOffset` | `(0, 0)` |
| `rotationPoint` | `(0, 0, 0)` — entity origin |
| `addBox` offset | `(-3, -4, -5)` |
| Dimensions (W × H × D) | `6 × 8 × 10` |
| Notes | Narrow and elongated. Center of chest sits 4 units above ground origin. |

---

#### 1.1.2 Neck (3-segment chain anchored to front of Torso)

Each neck segment rotates independently. This allows the head to sweep, recoil, and "crane" without a single rigid tube.

**Neck Segment 1 (base)**

| Property | Value |
|---|---|
| `texOffset` | `(32, 0)` |
| `rotationPoint` | `(0, -4, -5)` — front-top of Torso |
| `addBox` offset | `(-2, -4, -4)` |
| Dimensions | `4 × 4 × 8` |
| Rest Rotation | `X: -0.25 rad` (pitched slightly forward) |

**Neck Segment 2 (mid)**

| Property | Value |
|---|---|
| `texOffset` | `(32, 12)` |
| `rotationPoint` | `(0, -4, -4)` — tip of Seg 1 |
| `addBox` offset | `(-2, -3, -4)` |
| Dimensions | `4 × 4 × 7` |
| Rest Rotation | `X: -0.15 rad` |

**Neck Segment 3 (upper)**

| Property | Value |
|---|---|
| `texOffset` | `(32, 23)` |
| `rotationPoint` | `(0, -3, -4)` — tip of Seg 2 |
| `addBox` offset | `(-1.5, -3, -3)` |
| Dimensions | `3 × 3 × 6` |
| Rest Rotation | `X: -0.10 rad` |

---

#### 1.1.3 Head (Diamond-Shaped)

The "diamond" silhouette is achieved by using two overlapping boxes: a wide base and a narrow snout.

**Head Base**

| Property | Value |
|---|---|
| `texOffset` | `(0, 32)` |
| `rotationPoint` | `(0, -3, -3)` — tip of Neck Seg 3 |
| `addBox` offset | `(-3, -3, -4)` |
| Dimensions | `6 × 6 × 5` |
| Notes | Widest part of the head. |

**Snout**

| Property | Value |
|---|---|
| `texOffset` | `(22, 32)` |
| `rotationPoint` | `(0, 0, 0)` — relative to Head Base pivot |
| `addBox` offset | `(-2, -1, -6)` |
| Dimensions | `4 × 3 × 6` |
| Notes | Protrudes forward, tapering effect. |

**Jaw (Lower)**

| Property | Value |
|---|---|
| `texOffset` | `(22, 41)` |
| `rotationPoint` | `(0, 1, -1)` — hinge at rear of snout |
| `addBox` offset | `(-1.5, 0, -5)` |
| Dimensions | `3 × 1.5 × 5` |
| Animation | Rotates on X-axis for bite/idle mouth open. |

---

#### 1.1.4 Tail (3-Segment Chain, anchored to rear of Torso)

The tail tapers progressively. Each segment's `addBox` depth is shorter than its parent.

**Tail Segment 1 (base)**

| Property | Value |
|---|---|
| `texOffset` | `(52, 0)` |
| `rotationPoint` | `(0, -2, 5)` — rear of Torso |
| `addBox` offset | `(-2.5, -2.5, 0)` |
| Dimensions | `5 × 5 × 9` |

**Tail Segment 2 (mid)**

| Property | Value |
|---|---|
| `texOffset` | `(52, 14)` |
| `rotationPoint` | `(0, 0, 9)` — tip of Seg 1 |
| `addBox` offset | `(-2, -2, 0)` |
| Dimensions | `4 × 4 × 8` |

**Tail Segment 3 (tip)**

| Property | Value |
|---|---|
| `texOffset` | `(52, 26)` |
| `rotationPoint` | `(0, 0, 8)` — tip of Seg 2 |
| `addBox` offset | `(-1, -1, 0)` |
| Dimensions | `2 × 2 × 7` |
| Notes | Terminating in a flattened "blade" created by the narrow H dimension. |

---

#### 1.1.5 Wings (2-Joint Membranes, Left side mirrored to Right)

The Wyvern's wings attach at the shoulder (top-side of Torso) and unfurl via two pivot joints per wing. The membrane is represented as a very flat box (depth = 1 unit).

**Left Wing — Arm (Humerus)**

| Property | Value |
|---|---|
| `texOffset` | `(0, 52)` |
| `rotationPoint` | `(-3, -3, -2)` — left shoulder of Torso |
| `addBox` offset | `(-8, -1, -1)` |
| Dimensions | `8 × 2 × 2` |
| Rest Rotation | `Z: +0.8 rad` (folded inward) |
| Animation Range | `Z: -1.2 rad` (fully extended) to `Z: +1.0 rad` (folded tight) |

**Left Wing — Forearm (Radius)**

| Property | Value |
|---|---|
| `texOffset` | `(0, 57)` |
| `rotationPoint` | `(-8, 0, 0)` — tip of Arm pivot |
| `addBox` offset | `(-7, -1, -1)` |
| Dimensions | `7 × 2 × 2` |

**Left Wing — Membrane Panel**

| Property | Value |
|---|---|
| `texOffset` | `(28, 52)` |
| `rotationPoint` | `(-7, 0, 0)` — tip of Forearm |
| `addBox` offset | `(0, 0, -0.5)` |
| Dimensions | `12 × 14 × 1` |
| Notes | A flat quad-like box. UV rows map horizontal stripes of the membrane texture. The membrane stretches from forearm tip back toward the Torso side and partially toward the tail base using a secondary "trailing edge" box. |

**Left Wing — Trailing Edge (connects membrane to torso flank)**

| Property | Value |
|---|---|
| `texOffset` | `(28, 68)` |
| `rotationPoint` | `(-3, -1, -2)` — same shoulder pivot as Arm, but child of Torso directly |
| `addBox` offset | `(-10, 0, 0)` |
| Dimensions | `10 × 1 × 12` |
| Notes | This panel "fills in" the gap between arm/membrane and Torso body. Rendered semi-transparent via texture alpha channel. |

**Right Wing:** Mirror all Left Wing bones by negating X `rotationPoint` and X `addBox` offsets, and negating the Z-rotation sign.

---

#### 1.1.6 Legs (Wyvern has forelimbs fused with wings; rear legs only)

**Left Rear Leg — Upper**

| Property | Value |
|---|---|
| `texOffset` | `(0, 72)` |
| `rotationPoint` | `(-3, 2, 3)` — rear-lower flank of Torso |
| `addBox` offset | `(-2, 0, -2)` |
| Dimensions | `4 × 6 × 4` |

**Left Rear Leg — Lower**

| Property | Value |
|---|---|
| `texOffset` | `(14, 72)` |
| `rotationPoint` | `(0, 6, 0)` — bottom of Upper |
| `addBox` offset | `(-1.5, 0, -1.5)` |
| Dimensions | `3 × 5 × 3` |

**Left Rear Leg — Foot (3-toed)**

| Property | Value |
|---|---|
| `texOffset` | `(26, 72)` |
| `rotationPoint` | `(0, 5, 0)` — bottom of Lower |
| `addBox` offset | `(-3, 0, -4)` |
| Dimensions | `6 × 1 × 5` |

**Right Rear Leg:** Mirror Left by negating X `rotationPoint`.

---

### 1.2 Breed B — Goliath Drake

**Design Archetype:** Root-Heavy. The massive Torso is the immovable anchor. Legs are defined as thick pillars. Animation focuses on weight and "stomping" momentum rather than serpentine articulation.

**Bounding Box (Entity):** 2.8 W × 2.6 H blocks (resting); 5.0 W × 2.8 H (airborne).

---

#### 1.2.1 Torso (Barrel Chest — Root Bone)

| Property | Value |
|---|---|
| `texOffset` | `(0, 0)` |
| `rotationPoint` | `(0, 0, 0)` — entity origin |
| `addBox` offset | `(-8, -6, -12)` |
| Dimensions (W × H × D) | `16 × 12 × 24` |
| Notes | Deepest dimension is Z (front-back). This is the largest single box in the model. |

**Underbelly Panel**

| Property | Value |
|---|---|
| `texOffset` | `(64, 36)` |
| `rotationPoint` | `(0, 0, 0)` — same as Torso |
| `addBox` offset | `(-5, 5, -10)` |
| Dimensions | `10 × 2 × 20` |
| Notes | Sits flush against the bottom of Torso (`offsetY = 5` places it at Torso bottom when Torso starts at `offsetY -6`). This panel has its own UV island to allow a unique scale texture (softer, lighter scales) and lava-crack emissive layer. |

**Spinal Plates (5 plates along the dorsal ridge)**

Each plate is a thin, tall, slightly angled box. All share the Torso as parent.

| Plate | `texOffset` | `rotationPoint (x, y, z)` | `addBox offset` | Dimensions |
|---|---|---|---|---|
| Plate 1 (neck-end) | `(64, 0)` | `(0, -6, -9)` | `(-1, -5, -1)` | `2 × 5 × 2` |
| Plate 2 | `(64, 0)` | `(0, -6, -3)` | `(-1, -6, -1)` | `2 × 6 × 2` |
| Plate 3 (tallest) | `(64, 0)` | `(0, -6, 3)` | `(-1, -7, -1)` | `2 × 7 × 2` |
| Plate 4 | `(64, 0)` | `(0, -6, 7)` | `(-1, -6, -1)` | `2 × 6 × 2` |
| Plate 5 (tail-end) | `(64, 0)` | `(0, -6, 11)` | `(-1, -4, -1)` | `2 × 4 × 2` |

All plates share `texOffset (64, 0)` — the UV sheet tile-maps a consistent "armored ridgeback" texture strip.

---

#### 1.2.2 Neck (Short & Armored — 2 segments only)

**Neck Segment 1 (base)**

| Property | Value |
|---|---|
| `texOffset` | `(32, 0)` |
| `rotationPoint` | `(0, -5, -12)` — front-top of Torso |
| `addBox` offset | `(-3, -5, -6)` |
| Dimensions | `6 × 6 × 6` |
| Rest Rotation | `X: -0.35 rad` |

**Neck Segment 2 (upper)**

| Property | Value |
|---|---|
| `texOffset` | `(32, 18)` |
| `rotationPoint` | `(0, -5, -6)` — front of Seg 1 |
| `addBox` offset | `(-3, -4, -5)` |
| Dimensions | `6 × 5 × 5` |
| Rest Rotation | `X: -0.20 rad` |

---

#### 1.2.3 Head (Broad & Armored)

**Head Base**

| Property | Value |
|---|---|
| `texOffset` | `(0, 36)` |
| `rotationPoint` | `(0, -4, -5)` — tip of Neck Seg 2 |
| `addBox` offset | `(-4, -4, -7)` |
| Dimensions | `8 × 7 × 7` |

**Brow Ridge (armor plate above eyes)**

| Property | Value |
|---|---|
| `texOffset` | `(30, 36)` |
| `rotationPoint` | `(0, 0, 0)` — relative to Head Base |
| `addBox` offset | `(-4, -5, -3)` |
| Dimensions | `8 × 2 × 4` |

**Snout**

| Property | Value |
|---|---|
| `texOffset` | `(30, 48)` |
| `rotationPoint` | `(0, 0, 0)` — relative to Head Base |
| `addBox` offset | `(-3, -2, -9)` |
| Dimensions | `6 × 4 × 6` |

**Jaw (Lower)**

| Property | Value |
|---|---|
| `texOffset` | `(30, 54)` |
| `rotationPoint` | `(0, 2, -2)` — hinge behind snout |
| `addBox` offset | `(-2.5, 0, -7)` |
| Dimensions | `5 × 2 × 7` |

---

#### 1.2.4 Tail (2-Segment — Short & Heavy)

**Tail Segment 1 (base)**

| Property | Value |
|---|---|
| `texOffset` | `(52, 0)` |
| `rotationPoint` | `(0, -3, 12)` — rear of Torso |
| `addBox` offset | `(-4, -4, 0)` |
| Dimensions | `8 × 8 × 12` |

**Tail Segment 2 (end)**

| Property | Value |
|---|---|
| `texOffset` | `(52, 20)` |
| `rotationPoint` | `(0, 0, 12)` — tip of Seg 1 |
| `addBox` offset | `(-3, -3, 0)` |
| Dimensions | `6 × 6 × 8` |

---

#### 1.2.5 Wings (Broad Spanning — 2-Joint, Left mirrored to Right)

**Left Wing — Arm (Humerus)**

| Property | Value |
|---|---|
| `texOffset` | `(0, 84)` |
| `rotationPoint` | `(-8, -5, -4)` — upper flank of Torso |
| `addBox` offset | `(-10, -2, -2)` |
| Dimensions | `10 × 4 × 4` |
| Rest Rotation | `Z: +0.9 rad` (folded) |

**Left Wing — Forearm**

| Property | Value |
|---|---|
| `texOffset` | `(0, 94)` |
| `rotationPoint` | `(-10, 0, 0)` — arm tip |
| `addBox` offset | `(-9, -1.5, -1.5)` |
| Dimensions | `9 × 3 × 3` |

**Left Wing — Membrane Panel**

| Property | Value |
|---|---|
| `texOffset` | `(44, 84)` |
| `rotationPoint` | `(-9, 0, 0)` — forearm tip |
| `addBox` offset | `(0, 0, -0.5)` |
| Dimensions | `18 × 20 × 1` |
| Notes | Significantly larger than Wyvern's membrane. UV should span the full-width texture island designated for wing membranes. Gold-trimmed variant adds a 2-unit border strip on the UV outer edge. |

**Left Wing — Trailing Edge (dorsal connector)**

| Property | Value |
|---|---|
| `texOffset` | `(44, 106)` |
| `rotationPoint` | `(-8, -4, -4)` — same shoulder as Arm |
| `addBox` offset | `(-14, 0, 0)` |
| Dimensions | `14 × 1 × 18` |

**Right Wing:** Mirror all Left Wing bones by negating X values.

---

#### 1.2.6 Legs (4 Pillar-Like Legs)

All four legs use the same general "pillar" design. The upper and lower segments are intentionally thick to evoke a heavyweight, load-bearing silhouette.

**Front-Left Leg — Upper (Thigh)**

| Property | Value |
|---|---|
| `texOffset` | `(0, 100)` |
| `rotationPoint` | `(-6, 3, -7)` — front-lower flank of Torso |
| `addBox` offset | `(-4, 0, -4)` |
| Dimensions | `8 × 8 × 8` |

**Front-Left Leg — Lower (Shin)**

| Property | Value |
|---|---|
| `texOffset` | `(20, 100)` |
| `rotationPoint` | `(0, 8, 0)` — bottom of Thigh |
| `addBox` offset | `(-3, 0, -3)` |
| Dimensions | `6 × 8 × 6` |

**Front-Left Leg — Foot**

| Property | Value |
|---|---|
| `texOffset` | `(40, 100)` |
| `rotationPoint` | `(0, 8, 0)` — bottom of Shin |
| `addBox` offset | `(-4, 0, -5)` |
| Dimensions | `8 × 2 × 6` |
| Notes | Wide, splayed foot. 3 toe boxes can be added as children at `(-3, 0, -5)`, `(0, 0, -5)`, `(+3, 0, -5)` with `2×1×3` each. |

**Front-Right, Rear-Left, Rear-Right Legs:** Mirror/offset from Front-Left using:
- Front-Right: negate X of `rotationPoint`
- Rear-Left: same X, shift Z to `+7`
- Rear-Right: negate X, shift Z to `+7`

---

## 2. Visuals & Variants

### UV Texture Sheet Layout

Both breeds use a **256 × 256 px** texture atlas. The sheet is divided into horizontal bands:

| Band (px row) | Content |
|---|---|
| 0 – 83 | Torso top, underbelly, chest |
| 84 – 115 | Wings (membrane + arm bones) |
| 116 – 139 | Legs (all four) |
| 140 – 179 | Neck segments |
| 180 – 215 | Head, snout, jaw, brow |
| 216 – 231 | Tail segments |
| 232 – 255 | Spinal plates (Drake only) / Trim details |

A separate **32 × 32 px** "Emissive Overlay" texture (`_emissive.png`) maps only the eyes, nostril glow spots, and underbelly lava cracks. It is rendered additively on top of the main texture. Non-emissive areas of this sheet must be `rgba(0,0,0,0)`.

---

### 2.1 Skittering Wyvern Variants

#### Variant W-1: Verdant

**Theme:** Forest/Jungle camouflage. Active, naturalistic.

| Element | Description |
|---|---|
| **Base color** | Desaturated moss green `#4A6741` blending into bark brown `#5C3D1E` at limb extremities |
| **Scale pattern** | Layered Voronoi-cell noise. Each cell is 4–8 px wide. Cell edges are 1 px darker than cell interior. |
| **Belly/underside** | Pale cream `#C8B87A` with subtle horizontal banding (3 px bands, alternating ±10% brightness) |
| **Wing membrane** | Semi-transparent leaf-vein pattern. Base: `#3A5530` at 80% opacity. Vein lines: `#2A3D22` at 100% opacity, 1 px wide, branching from arm bone inward. |
| **Eyes** | Amber `#E8A020` — emissive layer intensity: 0.7 |
| **Emissive markings** | None beyond eyes |
| **Texture noise** | Add a high-frequency (2 px grain) grayscale noise layer at 15% opacity over the entire sheet to break up flat color areas |
| **Biome alignment** | Primary: `minecraft:forest`, `minecraft:jungle`, `minecraft:bamboo_jungle` |

#### Variant W-2: Frost-Vein

**Theme:** Tundra/Ice. Ethereal, dangerous.

| Element | Description |
|---|---|
| **Base color** | Ice blue `#A8C8E0` on dorsal surfaces. Pale lavender `#C4B4D8` on belly. |
| **Scale pattern** | Cracked-ice pattern. Large irregular polygons (20–40 px) with narrow dark-blue `#2A4060` cracks between them. Interior of each polygon has a subtle radial gradient (lighter at center). |
| **Wing membrane** | Translucency effect via alpha ramp: 100% opaque at arm bone, fading to 40% opacity at membrane tip. Color: pale cyan `#D0EEF8`. Faint horizontal striations (`#B0D8F0`, 1 px every 4 px). |
| **Eyes** | Piercing white-blue `#C0ECFF` — emissive layer intensity: 1.0 (maximum brightness) |
| **Emissive markings** | A single "frost vein" stripe running from behind each eye along the dorsal neck to Torso midpoint. Color `#A0D8F8`, 2 px wide on texture, emissive intensity 0.5. |
| **Texture noise** | Low-frequency sparkle noise: random single pixels set to `#FFFFFF` at 5% density across dorsal surface to simulate ice crystalline reflections. |
| **Biome alignment** | Primary: `minecraft:snowy_tundra`, `minecraft:ice_spikes`, `minecraft:frozen_river` |

#### Variant W-3: Shadow-Stalker

**Theme:** Pitch black with bioluminescent purple markings. Nocturnal predator.

| Element | Description |
|---|---|
| **Base color** | Near-black `#0D0D12` with a very subtle cool-blue sheen (`#151520`) on raised scale edges |
| **Scale pattern** | Micro-scale grid barely visible against base. Scale edges at `#1A1A26`, 1 px. Interior `#0D0D12`. |
| **Belly/underside** | Deep charcoal `#141416` — nearly identical to dorsal. Provides no visual "softening." |
| **Wing membrane** | Black `#080810` at 95% opacity. No vein detail on the base layer — detail is handled by emissive overlay only. |
| **Eyes** | Vivid magenta-purple `#CC00FF` — emissive intensity: 1.0 |
| **Emissive markings** | Spotted "constellation" pattern along flanks: 8–12 circular spots of radius 2 px each, color `#8800DD`, emissive intensity 0.8. Also, a faint slit glow inside each nostril `#6600AA`. |
| **Texture noise** | None — visual interest comes entirely from emissive layer contrast against the black base. |
| **Biome alignment** | Primary: `minecraft:dark_forest`, `minecraft:soul_sand_valley` |

---

### 2.2 Goliath Drake Variants

#### Variant G-1: Basalt

**Theme:** Volcanic. Heavy, geological, terrifying.

| Element | Description |
|---|---|
| **Base color** | Dark charcoal `#222222` dorsal. Mid-grey `#3A3A3A` on leg/underbelly transitions. |
| **Scale pattern** | Large irregular hexagonal plates (25–50 px cells). Plate borders dark `#111111`, 2 px wide. Interior of plates have a subtle bevel (lighter by 15% in top-left corner of each cell, darker by 10% in bottom-right). |
| **Underbelly** | Deep red-orange `#6B1A00` base. "Lava crack" network: a branching network of 1–2 px lines in bright orange-yellow `#FF6B00` covering roughly 20% of the underbelly UV island surface area. |
| **Spinal plates** | Near-black `#191919` with a 1 px highlight edge in `#444444` |
| **Wing membrane** | Dark ashy grey `#2A2A2A` at 90% opacity. Faint vein detail in `#3D1A00` |
| **Eyes** | Molten orange `#FF4400` — emissive intensity: 0.9 |
| **Emissive markings** | The full lava crack network on the underbelly UV island rendered in the emissive layer at intensity 0.8. Additionally, nostril slits glow orange `#FF3300`, emissive 0.6. |
| **Biome alignment** | Primary: `minecraft:basalt_deltas`, `minecraft:nether_wastes` |

#### Variant G-2: Alabaster

**Theme:** Ancient, divine, regal. Contrasting with G-1.

| Element | Description |
|---|---|
| **Base color** | Pure off-white `#F5F0E8` dorsal. Soft warm white `#FAF8F0` underbelly. |
| **Scale pattern** | Very fine, dense scale grid (6–8 px cells). Cell borders `#D8D0C0`, 1 px. Interior plain white — no bevel, projecting a smooth, almost marble-like appearance. |
| **Underbelly** | Warm ivory `#FFF8E8`. No lava cracks. Instead, subtle horizontal grooves: `#EEE4D0` 1 px lines every 6 px. |
| **Spinal plates** | Ivory `#EEE8DA` with a gold leaf accent: a 2 px border stripe on the leading/trailing edge of each plate in `#C8A400`. |
| **Wing membrane** | Bright white `#FFFFF0` at 85% opacity. "Gold trim" border: 3 px inner border on all membrane edges (arm side and trailing edge) in `#C8A400` at 100% opacity. Fine gold-vein internal lines at 2 px spacing, `#D4B020` at 30% opacity. |
| **Eyes** | Molten gold `#FFD700` — emissive intensity: 0.85 |
| **Emissive markings** | Gold-rimmed eye socket halo: 2 px ring around each eye in `#B89000`, emissive 0.4. Faint golden glow in nostril slit, `#C8A000` emissive 0.3. |
| **Biome alignment** | Primary: `minecraft:mountains`, `minecraft:gravelly_mountains` |

#### Variant G-3: Copper-Head

**Theme:** Metallic, weathered, industrial-organic.

| Element | Description |
|---|---|
| **Base color** | Metallic orange-brown `#8B4513` dorsal body. |
| **Scale pattern** | Medium irregular plates (15–25 px cells) with a slight "hammered metal" texture: each cell interior has 3–5 random small dents (darker circular spots, 2 px radius, `#6B3000`). Cell borders `#5A2800`, 1.5 px. |
| **Underbelly** | Lighter copper `#B06020`. Patina patches: irregular blobs (10–20 px) of verdigris green `#4A8060` scattered across 30–40% of the underbelly area. |
| **Spinal plates** | Weathered copper `#7A4010` with verdigris green `#3D7050` patches on upper 30% of each plate face. |
| **Wing membrane** | Oxidized copper-green `#5A8060` at 75% opacity. Fine network of darker green veins `#3D6048`, 1 px. |
| **Eyes** | Teal-green `#20C8A0` — emissive intensity: 0.75 |
| **Emissive markings** | A subtle patina-glow effect: the verdigris patches on underbelly have a faint bioluminescent teal `#20A880` emissive at intensity 0.2. Eye sockets have a 1 px green ring `#20C8A0` at emissive 0.4. |
| **Biome alignment** | Primary: `minecraft:savanna`, `minecraft:badlands`, `minecraft:eroded_badlands` |

---

### Hidden "Hatch Biome" Variants

These are triggered at hatching time (see Section 4.2). They are not selectable during breeding and have no direct parent color basis.

| Breed | Trigger Biome | Hidden Variant Name | Key Visual Delta from Base |
|---|---|---|---|
| Wyvern | `minecraft:desert` | **Sandglass** | Sandy ochre `#C8A040` body, white belly, no emissive |
| Wyvern | `minecraft:mushroom_fields` | **Spore-Touched** | Pale violet `#B08090` body with white spot clusters |
| Drake | `minecraft:desert` | **Dust** | Pale tan `#D4B880` body, faded lava cracks replaced by sand-colored grooves |
| Drake | `minecraft:deep_cold_ocean` | **Abyssal** | Near-black `#0A0A14`, bioluminescent cyan `#00FFCC` emissive spots, translucent membrane |

---

## 3. Immersive Behavior & Ecology

### 3.1 Spawning System

Spawning uses Forge's `ISpawnPlacementRegistry` extended with a custom `BiomeSpawnWeight` registry. **No structures are required.** Spawning is purely biome + world-height conditional.

#### Skittering Wyvern — Spawn Rules

| Parameter | Value |
|---|---|
| **Entity Group** | `MONSTER` (so it obeys mob cap and despawns if untamed) |
| **Placement Type** | `ON_GROUND` |
| **Light Level** | Any (spawns day/night) |
| **Min Y** | 64 (does not spawn underground) |
| **Min Pack Size** | 1 |
| **Max Pack Size** | 2 (rarely a pair; solitary by default) |

**Biome Weight Table:**

| Biome | Weight | Notes |
|---|---|---|
| `minecraft:mountains` | 8 | Core biome |
| `minecraft:gravelly_mountains` | 10 | Highest weight |
| `minecraft:wooded_mountains` | 5 | Secondary |
| `minecraft:forest` | 3 | Low frequency — "vagrant" spawns only |
| `minecraft:taiga` | 4 | Medium |
| `minecraft:snowy_taiga` | 3 | Frost-Vein variant biased here at 70% |
| `minecraft:dark_forest` | 2 | Shadow-Stalker variant biased at 90% |
| All other biomes | 0 | Will not spawn |

**Variant Spawn Bias Logic:**

When a Wyvern spawns, the game checks the exact biome and runs a weighted random against the three variant IDs. Default is uniform 33/33/33. Biome-specific override weights are applied as a multiplier:

- In `snowy_taiga` / `snowy_tundra` / `ice_spikes`: Frost-Vein weight ×3.
- In `dark_forest` / `soul_sand_valley`: Shadow-Stalker weight ×5.
- In `forest` / `jungle` / `bamboo_jungle`: Verdant weight ×4.

#### Goliath Drake — Spawn Rules

| Parameter | Value |
|---|---|
| **Entity Group** | `MONSTER` |
| **Placement Type** | `ON_GROUND` |
| **Light Level** | ≤ 7 (prefers darkness; does not spawn in open daylight areas) |
| **Min Y** | -16 (can spawn in deepslate caves and surface) |
| **Max Y** | 60 (does not spawn at high altitude) |
| **Min Pack Size** | 1 |
| **Max Pack Size** | 1 (always solitary) |

**Biome Weight Table:**

| Biome | Weight | Notes |
|---|---|---|
| `minecraft:basalt_deltas` | 12 | Core Nether biome |
| `minecraft:nether_wastes` | 6 | Common Nether |
| `minecraft:crimson_forest` | 3 | Sparse |
| `minecraft:soul_sand_valley` | 4 | Secondary Nether |
| `minecraft:dripstone_caves` | 8 | Core Overworld underground |
| `minecraft:lush_caves` | 2 | Very rare; Copper-Head variant biased here at 80% |
| `minecraft:badlands` | 4 | Surface spawn at night |
| `minecraft:eroded_badlands` | 5 | Copper-Head biased here at 75% |
| All other biomes | 0 | Will not spawn |

**Variant Spawn Bias Logic:**

- In `basalt_deltas` / `nether_wastes`: Basalt variant weight ×5.
- In `dripstone_caves`: Basalt weight ×3; Alabaster weight ×1.
- In `badlands` / `eroded_badlands` / `lush_caves`: Copper-Head weight ×4.
- In `mountains` (if summoned by egg only — no wild spawn): Alabaster weight ×5.

---

### 3.2 AI Goal Selector

Goals are ordered by **priority** (lower number = higher priority). Both breeds share the same Goal class hierarchy but with breed-specific parameter tuning.

```
GoalSelector
├── Priority 1: SwimGoal (shared — prevents drowning)
├── Priority 2: [Custom] DragonPanicGoal
├── Priority 3: [Custom] DragonAttackGoal
├── Priority 4: [Custom] DragonHuntGoal
├── Priority 5: [Custom] DragonRoostGoal
├── Priority 6: [Custom] DragonIdleWanderGoal
└── Priority 7: LookAtPlayerGoal / LookRandomlyGoal
```

#### Goal Definitions

**DragonPanicGoal** (Priority 2)
- Triggers when health drops below 30% of max.
- Wyvern: Activates flight flee behavior. Launches into air, pathfinds away from attacker at 1.5× normal speed. Breaks off after 10 seconds or when attacker is > 32 blocks away.
- Drake: Does NOT flee. Instead, activates a "Rage Stance": stops all other goals, roars (plays sound, triggers particle burst), and temporarily grants +4 armor points for 8 seconds.

**DragonAttackGoal** (Priority 3)
- Standard melee attack when a target is set (player within aggro range, attacked entity, etc.).
- Wyvern: Fast-strike attack. Executes 3 rapid hit attempts in 1.5 seconds, then backs off 5 blocks before re-engaging. Attack speed multiplier: 1.8.
- Drake: Slow, powerful single strikes. 2.5-second wind-up animation (jaw open) before impact. Attack deals additional knockback equal to 1.5× vanilla. Attack speed multiplier: 0.6.

**DragonHuntGoal** (Priority 4)
- Active goal when no target is set and dragon is "hungry" (NBT `HungerTicks` > 4000).
- Targets: passive mobs (cows, sheep, pigs, chickens). Preference for the nearest.
- Wyvern: Uses flight approach — lifts off, dives at target. If target is indoors (Y blocked), diverts to ground approach.
- Drake: Ground-only approach. Pursues at normal walk speed. Plays ambient hunting growl every 8 seconds while goal is active.
- On successful kill: `HungerTicks` resets to 0. Play satiated sound. Entity lies down for 12 seconds (Roosting state).

**DragonRoostGoal** (Priority 5)
- Activates when `HungerTicks` < 1000 OR health < 70% AND dragon is not in combat.
- Dragon pathfinds to the nearest solid elevated surface (Y > standing ground + 3 blocks) or its "home position" NBT tag.
- Once at roost position, enters "roosting" animation state: wings fold, head rests on torso, eyes half-closed.
- Wyvern roosts for 30–60 seconds, then transitions to Idle Wander.
- Drake roosts for 120–300 seconds. During roost, health regenerates at 0.5 HP/sec.

**DragonIdleWanderGoal** (Priority 6)
- Default state. Dragon wanders within a 24-block radius of its "home position" (set on first spawn or taming).
- Wyvern: Alternates between ground wander and short 10-second flight loops. Plays idle chirp sounds randomly.
- Drake: Exclusively ground wander. Slow, deliberate steps. Pauses every 15–30 seconds to "look around" (LookRandomlyGoal takes over for a few seconds).

**Target Selector Goals:**

```
TargetSelector
├── Priority 1: [Custom] DragonOwnerHurtByTargetGoal (if tamed — retaliate for owner)
├── Priority 2: HurtByTargetGoal (retaliate against attacker)
└── Priority 3: [Custom] DragonNearestAttackableGoal (proactive aggro radius)
```

**DragonNearestAttackableGoal parameters:**

| Breed | Aggro Radius | Aggro Condition |
|---|---|---|
| Wyvern | 16 blocks | Players in the Wyvern's forward FOV (90° cone). Will NOT aggro players approaching from behind. |
| Drake | 24 blocks | All-directional. Will aggro any non-owner player entering radius at night. During daytime: radius reduced to 12 blocks. |

---

## 4. Interaction Systems

### 4.1 Taming — Multi-Stage Protocol

Taming is a **three-stage process** tracked via the entity's NBT data. Simple right-click feeding is intentionally avoided to enforce immersive engagement.

#### Stage Tracking NBT

```
{
  "TrustLevel": 0,          // 0 = wild, 1 = approach-tolerant, 2 = feeding-ready, 3 = tamed
  "GazeLockTicks": 0,       // Running counter while player is in Gaze-Lock condition
  "LastFeeder": UUID,        // UUID of the player who initiated taming
  "FeedCount": 0             // How many feeding items have been accepted
}
```

---

#### Stage 0 → Stage 1: Gaze-Lock (Proximity Trust)

**Condition:** `TrustLevel == 0`

1. The dragon continuously scans for players within 20 blocks.
2. If a player enters the dragon's forward FOV (120° cone) and **does not move** (velocity magnitude < 0.01 blocks/tick) for **5 consecutive seconds**, the `GazeLockTicks` counter increments.
3. If the player moves, `GazeLockTicks` resets to 0.
4. At `GazeLockTicks >= 100` (5 seconds at 20 ticks/second), the dragon:
   - Plays a calm, low rumble sound.
   - Displays a particle ring (green `dust` particles in a 2-block radius circle around the dragon).
   - Sets `TrustLevel = 1`.
5. **Failure condition:** If the player **sneaks** (crouches) during gaze-lock, it counts as movement and resets the counter.
6. **Aggro override:** If `TrustLevel == 0` and the player moves aggressively (attacks, sprints toward dragon), the `DragonNearestAttackableGoal` immediately activates.

**Wyvern specifics:** Requires only 4 seconds of gaze-lock (80 ticks). More easily spooked — if the player gets within 5 blocks before Stage 1 is reached, the Wyvern flees.

**Drake specifics:** Requires 8 seconds of gaze-lock (160 ticks). Will not flee at any proximity during gaze-lock; instead, stares back and emits a deep rumble. Player must hold position at 8–12 block distance.

---

#### Stage 1 → Stage 2: First Feeding

**Condition:** `TrustLevel == 1`

**Required Item by Breed:**

| Breed | Taming Item | Notes |
|---|---|---|
| Skittering Wyvern | Glow Berries (`minecraft:glow_berries`) | Reflects the Wyvern's cave-dwelling prey associations. |
| Goliath Drake | Raw Mutton (`minecraft:mutton`) | Reflects the Drake's large prey hunting behavior. |

**Mechanic:**
1. Player right-clicks the dragon while **holding the correct item** and `TrustLevel == 1`.
2. Dragon plays an acceptance animation (head lowers, sniffs the item).
3. One item is consumed from the stack.
4. `FeedCount` increments by 1.
5. At `FeedCount == 3`, `TrustLevel = 2`. Dragon plays a louder rumble/purr sound. Hearts particles briefly emit.
6. **Rejection:** If the player offers the wrong item, the dragon snaps at (but doesn't damage) the player's hand (short animation) and `TrustLevel` drops back to 0. The gaze-lock process must restart.

---

#### Stage 2 → Stage 3: Full Taming

**Condition:** `TrustLevel == 2`

1. At `TrustLevel == 2`, the dragon enters a "submission" idle animation (sits, holds eye contact with the player who last fed it).
2. The same player must right-click the dragon one final time **without holding any item** (empty hand).
3. This triggers the taming finalization:
   - `TrustLevel = 3`
   - Dragon is marked as tamed (`setTamed(true)`).
   - Owner UUID is set.
   - Standard taming sound + large heart particle burst plays.
   - Dragon pathfinds to sit beside the player.
4. **Timeout:** If no interaction occurs for 60 seconds after `TrustLevel == 2`, it decays back to `TrustLevel == 1`. The player must feed again.

---

### 4.2 Breeding — Genetic Inheritance

Breeding requires two **tamed** dragons of the **same breed** with `TrustLevel == 3`. Dragons cannot breed with wild individuals.

#### Triggering Breeding

1. Two tamed Wyverns (or two tamed Drakes) owned by the **same player** must be within 6 blocks of each other.
2. The player feeds both dragons their respective taming items (Glow Berries or Raw Mutton), one after the other, within a 10-second window.
3. Both dragons enter a "mating circle" behavior (orbit each other at 3-block distance for 4 seconds).
4. An egg is spawned at the midpoint between the two parents and placed on the ground as a block entity (similar to Turtle egg).

---

#### Egg NBT Structure

```
{
  "BreedType": "wyvern" | "drake",
  "Parent1Variant": "verdant" | "frost_vein" | "shadow_stalker" | "basalt" | "alabaster" | "copper_head",
  "Parent2Variant": "...",
  "StatInheritance": {
    "MaxHealth": <average of parent1 and parent2 base health>,
    "AttackDamage": <max(parent1, parent2) * 0.75 + min(parent1, parent2) * 0.25>,
    "MoveSpeed": <weighted average, favoring higher-speed parent at 60/40>
  },
  "HatchBiome": "",          // Written at hatch time, not at egg creation
  "InheritedVariant": ""     // Resolved at hatch time
}
```

---

#### Genetic Variant Resolution (at Hatch Time)

When the egg receives a hatch tick (exposed to sufficient warmth — defined as being placed within 4 blocks of a fire, lava, or campfire; or in a Nether biome), the following sequence runs:

1. **Record `HatchBiome`** — The current biome ID where the egg block entity exists is stored.
2. **Check for Hidden Variant trigger:**
   - Query the `HatchBiomeTriggerMap` registry.
   - If the `HatchBiome` matches a trigger entry for the breed, the hidden variant is assigned to `InheritedVariant` (overriding all parent data).
   - Example: A Drake egg hatching in `minecraft:desert` → `InheritedVariant = "dust"`.
3. **If no hidden variant triggered:**
   - Roll a weighted random:
     - 40% chance: offspring takes `Parent1Variant`.
     - 40% chance: offspring takes `Parent2Variant`.
     - 20% chance: offspring takes a completely random variant from the breed's pool (mutation).
4. **Spawn offspring entity** with stats from `StatInheritance` and texture from `InheritedVariant`.

---

#### `HatchBiomeTriggerMap` (Full Definition)

| Breed | Biome ID | Hidden Variant Triggered |
|---|---|---|
| Wyvern | `minecraft:desert` | `sandglass` |
| Wyvern | `minecraft:mushroom_fields` | `spore_touched` |
| Wyvern | `minecraft:deep_cold_ocean` | *(none — egg sinks, does not hatch)* |
| Drake | `minecraft:desert` | `dust` |
| Drake | `minecraft:deep_cold_ocean` | `abyssal` |
| Drake | `minecraft:mushroom_fields` | *(none — egg hatches normally, no trigger)* |

---

#### Offspring Stats Scaling

Offspring begin as juveniles at **40% of adult size** and grow over **IRL 3 days** (configurable in `EmberAetherConfig`). Stat scaling follows a linear ramp from 40% to 100% of the inherited `StatInheritance` values over the growth period.

Growth is tracked via `AgeTicks` NBT (increments each tick the dragon is loaded). Food accelerates growth: each taming item fed to a juvenile adds 1000 `AgeTicks` equivalent.

---

*End of Dragon Mounts Technical Specification Document.*
*All coordinate values, weights, and timers are initial tuning targets and should be validated against in-engine playtesting.*
