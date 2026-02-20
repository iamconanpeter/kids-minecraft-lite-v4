# PRD — Kids Minecraft Lite v4

## Product summary

Kids Minecraft Lite v4 is a native Android MVP for ages 6–7+ that delivers a kid-readable block survival loop:
- mine and place blocks
- craft tiny high-impact recipes
- build shelter before night
- survive night mobs
- gain stars and keep local progress

## Problem statement

Prior versions were usable but lacked core fantasy pressure and emotional arc. Kids expected "build before dark" survival, but previous loops felt too abstract.

## Goals (must hit)

1. Strong fantasy fidelity in first session (mine/build/survive clearly visible).
2. Icon-first controls with minimal text.
3. 3–8 minute complete loop with tension and reward.
4. Parent-safe defaults: offline progression, no chat.

## Non-goals (MVP)

- Multiplayer or online sharing
- Account systems
- Live ops economy
- Large crafting trees

## Target users

- Primary: kids 6–9, especially early readers (6–7)
- Secondary: parents who want short, safe sessions

## Core loop

1. Day: mine/place/craft
2. Dusk: warning and shelter prep
3. Night: Boom Sprout pressure + periodic Sky Wyrm event
4. Sunrise: stars, unlock tier progress, repeat

## MVP feature requirements

- Day/night cycle with deterministic phases
- Mine/place world loop
- Tiny crafting (4 recipes)
- Mob archetypes:
  - Glowmew (friendly helper)
  - Boom Sprout (night chaser pressure)
  - Sky Wyrm (periodic boss event)
- Shelter safety score affecting night danger
- Progression (stars, tiers)
- Local save/load via SharedPreferences

## UX requirements

- One-screen icon controls
- Large touch targets
- Minimal text labels
- Fast restart after failure

## Success criteria

Experience:
1. Child reaches first night quickly (<2 minutes normal play)
2. Child can mine/place/craft without reading long text
3. Shelter score visibly matters at night

Engineering:
1. Unit tests for world/crafting/mob/day-night/shelter logic
2. `./gradlew test assembleDebug` passes
3. Save/load survives app restart

## Research traceability

- Competitor teardown and child-loop mapping: `docs/market-analysis.md`
- Moat strategy: `docs/moat-and-positioning.md`
- Retrospective/fantasy gap closure: `docs/fantasy-gap-analysis.md`
