# PRD — Kids Minecraft Lite v4

## 1. Product summary

Kids Minecraft Lite v4 is a native Android block-world survival adventure for kids 6–7+.

Core promise:
- Kid: “I mine, build shelter, and survive the spooky night with my pet friend.”
- Parent: “It is simple, safe, short-session, and not predatory.”

This v4 replaces v3 with a fantasy-first design informed by deep market and kid-psychology analysis.

## 2. Problem statement

The prior version underdelivered on recognizable survival fantasy. It had kid-friendly UX but lacked day/night urgency and shelter stakes.

v4 solves this by centering the expected fantasy pillars and preserving child simplicity.

## 3. Goals (must hit)

1. Deliver unmistakable block-world survival/building fantasy in short loops.
2. Keep controls icon-first and early-reader friendly.
3. Provide legal-safe original archetypes (no trademarked assets/naming).
4. Build parent trust via safe defaults and forgiving fail states.

## 4. Non-goals (MVP)

- No online multiplayer
- No chat or UGC sharing
- No account requirement
- No live-ops economy

## 5. Audience

Primary:
- Kids 6–9 (especially 6–7 early readers)

Secondary:
- Parents wanting safe short sessions and low cognitive overload

## 6. Core gameplay loop (3–8 min)

1. Day: mine blocks, place walls/roof, craft key tools
2. Dusk: warning phase and shelter prep
3. Night: chaser pressure + possible boss event
4. Morning: stars, unlock progress, try again

## 7. Core features (MVP)

### 7.1 World interaction
- Voxel-like tile world (2.5D acceptable)
- Mining + placing loop always available

### 7.2 Tiny crafting
- 3–5 recipes max
- Recipes must have immediate survival impact

### 7.3 Day/night and danger
- Repeating day/night timer
- Night increases threat and pressure

### 7.4 Mob archetypes
- Friendly helper pet: **Glowmew**
- Night chaser: **Boom Sprout**
- Boss event mob: **Sky Wyrm**

### 7.5 Shelter score
- Safety increases as shelter becomes enclosed
- Night damage/pressure is reduced by shelter safety

### 7.6 Progression
- Stars earned from survival/crafting/mob events
- Unlock thresholds for placeable/craftable content

### 7.7 Save
- Local persistence via SharedPreferences snapshot

### 7.8 Parent easy mode
- Optional reduced pressure (lighter danger, baseline shelter bonus)

## 8. Kid UX principles

- Icon-first controls
- Minimal text, short labels
- Large touch targets
- Short and forgiving loops
- Fast retry after setbacks

## 9. Safety and legal requirements

- No trademarked names/assets in shipped app
- Original naming/art/audio for all characters and items
- No external communication systems
- No manipulative retention mechanics

See mapping: `docs/fantasy-gap-analysis.md`.

## 10. Success criteria

Product/experience:
1. First-time player reaches first night in <= 2 minutes
2. Player can mine/place/craft without reading long text
3. Shelter score visibly affects survival outcome
4. One complete day-night loop fits within 3–8 minutes

Quality/engineering:
1. Unit tests cover world logic/crafting/mob/day-night/shelter
2. `./gradlew test assembleDebug` passes
3. Local save/load survives app restart

## 11. Deep-research integration checklist

This PRD explicitly incorporates:
- competitor teardown (12 comps)
- kid-psychology loop design
- moat matrix
- retrospective of previous failure

Reference: `docs/deep-research.md`
