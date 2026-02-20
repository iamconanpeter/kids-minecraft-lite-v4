# Functional Spec — Kids Minecraft Lite v4

## 1) Gameplay systems

### 1.1 World
- Grid size: 10x8
- Block types: AIR, DIRT, WOOD, STONE, PLANK, TORCH, CRYSTAL
- Initial world includes shallow ground, wood nodes, and crystal nodes

### 1.2 Input modes
- `MINE`: tap non-air tile to collect resource
- `PLACE`: tap air tile to place selected inventory block
- Placeable cycle: DIRT, WOOD, STONE, PLANK, TORCH

### 1.3 Crafting (4 recipes)
1. `plank_bundle`: WOOD x2 -> PLANK x2
2. `torch_pair`: PLANK x1 + CRYSTAL x1 -> TORCH x2
3. `stone_press`: DIRT x2 + WOOD x1 -> STONE x1
4. `pickaxe`: STONE x2 + WOOD x1 -> PICKAXE x1 (requires 1 star)

### 1.4 Time cycle
- Total cycle: 180 ticks
- DAY: 80
- DUSK: 20
- NIGHT: 60
- DAWN: 20
- At cycle wrap: sunrise rewards and day increment

### 1.5 Mob archetypes
- **Glowmew**: gives periodic day gifts (wood/crystal)
- **Boom Sprout**: night pressure via deterministic damage checks
- **Sky Wyrm**: periodic boss event every 3rd day at night, increases pressure and star rewards

### 1.6 Shelter safety
- Shelter score computed near anchor (center-top area)
- Score influenced by walls/roof/floor closure and nearby torches
- Higher score lowers night danger probability

### 1.7 Progression
- Stars gained on sunrise:
  - +1 base survive cycle
  - +1 if crafted during cycle
  - +1 if boss event cycle completed
- Unlock tier = `1 + stars / 4`

### 1.8 Fail-state
- Hearts reach 0 -> immediate rescue reset
- Hearts refill, stars lose 1 (floor 0), loop continues

### 1.9 Save format
- Serialized compact payload:
  - world ordinals
  - inventory map
  - scalar game state
- Stored under SharedPreferences key `v4_state`

## 2) UI spec

- Custom Android `View` Canvas renderer
- Distinct day/night background palette
- HUD shows:
  - phase + day
  - hearts, stars, shelter score
  - mode/easy status and mob cues
- Bottom action buttons:
  - mode toggle
  - place item cycle
  - craft panel toggle
  - easy mode toggle
- Craft panel displays icon-forward recipes and costs

## 3) Test spec

Unit tests cover:
1. Day/night transitions and day increment
2. Mine/place inventory/world invariants
3. Craft success/failure paths
4. Mob pressure and boss event presence
5. Shelter score improvement and save/load roundtrip

Gate command:
- `./gradlew test assembleDebug`
