# Architecture Diagram — Kids Minecraft Lite v4

```text
+---------------------------+
|        MainActivity       |
| - creates game view       |
| - wires save lifecycle    |
+-------------+-------------+
              |
              v
+---------------------------+
|      BlockQuestLiteView   |
|---------------------------|
| Render Canvas HUD/world   |
| Touch -> engine commands  |
| Tick loop (postDelayed)   |
+-------------+-------------+
              |
              v
+---------------------------+
|    BlockQuestLiteEngine   |
|---------------------------|
| World grid + inventory    |
| Mine/place rules          |
| Crafting rules            |
| Day/night state machine   |
| Mob behaviors (3 types)   |
| Shelter score computation |
| Stars/unlocks progression |
+-------------+-------------+
              |
              v
+---------------------------+
| BlockQuestLiteProgressMgr |
|---------------------------|
| Encode snapshot string    |
| Save/load SharedPrefs     |
+---------------------------+

Data Flow:
Touch -> View hit-test -> Engine mutation -> Snapshot -> View redraw
Tick -> Engine.tick() -> Night/day + mobs -> View redraw -> periodic save

Test Surface (pure Kotlin):
- day/night transitions
- shelter score
- crafting validity
- mine/place inventory
- mob danger and boss rewards
```
