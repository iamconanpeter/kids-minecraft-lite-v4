# Technical Plan — Kids Minecraft Lite v4

## 1. Tech stack
- Android native (Kotlin + Gradle)
- Single `app` module
- Custom `View` rendering (Canvas)
- Pure Kotlin game engine for deterministic logic + unit tests

## 2. Planned module structure

- `BlockQuestLiteEngine.kt`
  - world state
  - day/night tick machine
  - mining/placing
  - crafting validation
  - mob behaviors
  - shelter score
  - progression stars/unlocks
- `BlockQuestLiteProgressManager.kt`
  - snapshot encode/decode
  - SharedPreferences save/load
- `BlockQuestLiteView.kt`
  - rendering
  - touch input -> engine actions
  - game loop runnable
- `MainActivity.kt`
  - owns view and save lifecycle

## 3. Data model plan

Core enums:
- BlockType
- ItemType
- MobType
- InputMode

Core data:
- World grid (flattened mutable array/list)
- Inventory map
- Mob state (friendly, chaser, boss)
- Time state (tick + phase + nights survived)
- Safety/progression state (shelter score, stars, unlock tier)

## 4. State update strategy

- Engine reducer-like API:
  - `tick()`
  - `tapTile(x,y)`
  - `setMode()`
  - `cyclePlaceItem()`
  - `craft(recipeId)`
  - `toggleEasyMode()`
- Every mutation returns/updates a snapshot-friendly state

## 5. Rendering strategy

- 2.5D tile illusion via top face + side shade
- HUD includes:
  - day/night indicator
  - hearts
  - stars
  - shelter score meter
  - mob status icons
- Bottom action bar:
  - mine/place toggle
  - inventory cycle
  - craft panel toggle
  - easy mode toggle

## 6. Save strategy

- Persist compact snapshot string in SharedPreferences
- Save schedule:
  - after major actions
  - every N ticks
  - lifecycle detach
- Load snapshot on app start

## 7. Test plan

Unit tests:
1. day-night transition and night counters
2. mine/place behavior and inventory effects
3. crafting success/fail paths
4. shelter score thresholds
5. mob pressure (night damage, boss bonus)

Gate:
- `./gradlew test assembleDebug` must pass

## 8. Risks and mitigations

1. **Risk:** custom View touch complexity
   - Mitigation: explicit hitbox map for controls/craft panel
2. **Risk:** logic drift between UI and engine
   - Mitigation: keep all rules in engine only
3. **Risk:** legal confusion around archetypes
   - Mitigation: original naming + mapping doc and no copied assets
4. **Risk:** over-scope from deep systems
   - Mitigation: strict MVP cap (4 recipes, 3 mobs, one world)

## 9. Codex CLI evidence policy

Planning and coding decisions are backed by Codex CLI runs:
- `docs/codex-planning-output.txt`
- `docs/codex-coding-output.txt`
