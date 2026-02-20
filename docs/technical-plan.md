# Technical Plan — Kids Minecraft Lite v4

## Stack
- Kotlin + Android Gradle (single app module)
- Custom Canvas `View`
- Pure Kotlin gameplay engine class (no Android dependencies)
- SharedPreferences for local save

## Core classes

- `BlockQuestLiteEngine.kt`
  - deterministic world state
  - mine/place/craft logic
  - day/night cycle
  - mob archetype behavior hooks
  - shelter scoring
  - progression/stars/unlocks
  - serialization

- `BlockQuestLiteView.kt`
  - render world + HUD + controls
  - touch routing to engine actions
  - fixed tick loop with `postDelayed`

- `BlockQuestLiteProgressManager.kt`
  - load/save serialized engine payload

- `MainActivity.kt`
  - instantiate progress manager + view
  - persist on pause

## Determinism strategy

- Engine uses fixed integer tick steps
- Night pressure uses deterministic roll formula from current state
- Boss event cadence based on day count modulo 3
- Deterministic loops simplify unit tests and debugging

## Save strategy

- Compact string payload via `engine.toSavePayload()`
- Saved in `SharedPreferences` under `v4_state`
- Saved periodically and on lifecycle pause/detach

## Risks and mitigations

1. **UI hitbox complexity**
   - Mitigation: explicit RectF controls and separate worldRect tap mapping
2. **State drift between view and logic**
   - Mitigation: all game rules centralized in engine
3. **Scope creep**
   - Mitigation: strict cap on recipes, blocks, and systems in MVP
4. **Regression risk**
   - Mitigation: unit tests for all required logic groups

## Quality gate procedure

1. Set Java/SDK env vars
2. Run `./gradlew test`
3. Run `./gradlew assembleDebug`
4. If failure, fix and rerun until both pass
