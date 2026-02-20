# Architecture Diagram — Kids Minecraft Lite v4

```text
+------------------------------+
|          MainActivity        |
|------------------------------|
| - creates ProgressManager    |
| - creates BlockQuestLiteView |
| - onPause -> persistState()  |
+---------------+--------------+
                |
                v
+------------------------------+
|       BlockQuestLiteView     |
|------------------------------|
| Canvas rendering             |
| Touch -> engine actions      |
| Tick loop (postDelayed)      |
| HUD + craft panel controls   |
+---------------+--------------+
                |
                v
+------------------------------+
|      BlockQuestLiteEngine    |
|------------------------------|
| World grid state             |
| Mine/place/craft rules       |
| Day/Dusk/Night/Dawn machine  |
| Mob archetype behavior       |
| Shelter score computation    |
| Stars/unlock progression      |
| Save payload serialize/parse |
+---------------+--------------+
                |
                v
+------------------------------+
| BlockQuestLiteProgressManager|
|------------------------------|
| SharedPreferences load/save  |
+------------------------------+
```

## Runtime flow

1. User taps world/control in `BlockQuestLiteView`
2. View calls engine mutation (`tapTile`, `craft`, `toggle...`)
3. Tick loop calls `engine.tick()` every ~130ms
4. View reads `engine.currentState()` and redraws
5. Save payload written periodically and on pause

## Unit test surface (pure logic)

- day-night transitions
- mine/place invariants
- crafting success/fail
- mob pressure + boss cadence
- shelter scoring + save roundtrip
