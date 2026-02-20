# Spec — Kids Minecraft Lite v4

## 1) Gameplay systems spec

### 1.1 World
- Grid world: 10x8 tile map
- Block types (MVP): AIR, DIRT, WOOD, STONE, PLANK, TORCH, CRYSTAL
- Rendering: 2.5D-styled tile shading in custom Android View

### 1.2 Player interaction
- Modes:
  - Mine mode (tap tile -> mine)
  - Place mode (tap tile -> place selected block)
- Inventory cycling button for placeable blocks

### 1.3 Crafting (tiny panel, 4 recipes)
1. WOOD x2 -> PLANK x2
2. STONE x2 + WOOD x1 -> PICKAXE x1
3. PLANK x1 + CRYSTAL x1 -> TORCH x2
4. DIRT x2 + WOOD x1 -> STONE x1 (compressed mud block analog)

Crafting constraints:
- Recipe fails cleanly with icon feedback if resources missing
- No nested crafting trees in MVP

### 1.4 Day/night state machine
- Timed repeating cycle (short loop)
- Phases:
  - DAY: gather/build/craft
  - NIGHT: danger active
- Dusk transition warning in HUD

### 1.5 Mob archetypes
- **Glowmew** (friendly): gives periodic gift resources during day
- **Boom Sprout** (chaser): night pressure, attacks if unsheltered
- **Sky Wyrm** (boss event): periodic night event, grants bonus stars if endured/repelled

### 1.6 Shelter safety
- Safety score 0..100 from enclosure around shelter anchor
- Score increases from roof/walls/floor completion
- Score >= threshold marks sheltered state
- Night damage chance/rate reduced when sheltered

### 1.7 Progression and stars
- Stars earned by:
  - surviving sunrise
  - crafting at least one item in cycle
  - defeating/enduring boss event
- Unlock tiers driven by total stars (new placeables or better starting kit)

### 1.8 Fail-state philosophy
- Forgiving fail state:
  - if hearts depleted, rescue/reset with minor penalty
  - no harsh progress wipe

### 1.9 Save format
- SharedPreferences keys:
  - world snapshot string
  - inventory string
  - stars, hearts, cycle, easyMode, unlock tier
- Save triggers:
  - after actions and periodic ticks
  - on view detach / activity pause

---

## 2) UX/UI spec (kid-simple)

- Icon-first HUD with tiny labels
- Large action buttons (mine/place/craft/easy mode)
- Craft panel uses icon recipes + counts
- Color/emoji signals:
  - day/night clearly distinct
  - safe/unsafe shelter states obvious
- Short prompts only; no paragraph text

---

## 3) Technical quality spec

- Pure Kotlin engine with deterministic tick updates
- Android custom View renderer and touch router
- Unit tests must cover:
  - day-night transitions
  - mine/place invariants
  - crafting validity/failure
  - shelter scoring
  - mob pressure rules

Acceptance command:
- `./gradlew test assembleDebug`

---

## 4) Legal-safe spec

- All names/art are original:
  - Glowmew, Boom Sprout, Sky Wyrm
- No trademarked names/asset copies in app text or visuals
- Archetype inspiration retained only at high level

Mapping reference:
- `docs/fantasy-gap-analysis.md`

---

## 5) Deep-research to spec traceability

| Research insight | Spec decision |
|---|---|
| Kids need short tension-release loops | 3–8 min day-night cycle |
| Cognitive load must be low | 2 interaction modes + 4 recipes only |
| Parents need trustable safety defaults | no chat/UGC + easy mode + forgiving fail |
| Fantasy fidelity beats abstract missions | shelter-before-night and mob pressure core loop |
