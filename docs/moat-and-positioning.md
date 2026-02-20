# Moat and Positioning — Kids Minecraft Lite v4

## Positioning statement

**For kids 6–7 and their parents, Kids Minecraft Lite v4 is a short-session, icon-first survival sandbox that captures the core gather/build/survive fantasy while removing unsafe social complexity and cognitive overload.**

## Moat matrix (strategy + execution implications)

| Strategic moat | Why it matters | Execution implications |
|---|---|---|
| 1) Early-reader survival UX moat | Most voxel games optimize for older users, not 6–7-year-olds | Keep one-screen icon controls, huge tap targets, <5 active verbs |
| 2) Trust moat (offline + no chat) | Parent trust determines install retention | No open chat, no UGC sharing, local save only, predictable content |
| 3) Fantasy-fidelity moat | Competitors often simplify too far and lose survival identity | Preserve day/night pressure + shelter consequence + archetype mobs |
| 4) Loop-efficiency moat | Busy family contexts reward complete loops in minutes | Deterministic 3–8 minute tension/release sessions |
| 5) Original-IP archetype moat | Legal-safe familiarity is hard to execute consistently | Original names/visual identity for helper/chaser/boss with no trademark overlap |
| 6) Performance moat on low-end Android | Big addressable market in lower-spec devices | Pure Kotlin logic, lightweight Canvas render, no heavy assets/network dependency |

## Moat-to-feature traceability in v4 MVP

- Early-reader UX moat -> icon-first controls, minimal text labels.
- Trust moat -> local persistence, no networking features.
- Fantasy-fidelity moat -> day/night + mine/place + crafting + mobs + shelter score.
- Loop-efficiency moat -> short deterministic cycle and fast recovery on failure.
- Original-IP moat -> Glowmew, Boom Sprout, Sky Wyrm.
- Performance moat -> simple 2D/2.5D-style tile render and deterministic engine ticks.

## Strategic anti-patterns to avoid

1. Adding chat or public sharing too early.
2. Expanding recipes/items before loop readability is validated.
3. Replacing survival pressure with menu-heavy mission scaffolding.
4. Introducing aggressive monetization before trust baseline is established.
