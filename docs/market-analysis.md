# Market Analysis — Kids Minecraft Lite v4

Date: 2026-02-20
Target cohort: kids 6–7 early readers (with parent gatekeeping)

## 1) Competitor teardown (12 references across iOS / Android / online)

| Product | Platform | What kids love | Where they churn | Design takeaway for v4 |
|---|---|---|---|---|
| Minecraft | iOS/Android | Open creativity + survival fantasy + iconic loop | Complexity/menu depth for younger kids; long-session cognitive load | Keep gather/build/survive pillars but simplify controls to icon-first |
| Roblox | iOS/Android/online | Infinite novelty and social identity | Safety/moderation complexity + broad age mismatch | No open chat/UGC sharing in MVP; curated offline-safe play |
| Block Craft 3D | iOS/Android | Fast onboarding and immediate building | Lighter survival stakes, ad fatigue | Keep instant building feel but add real night pressure |
| Survivalcraft 2 | iOS/Android | Deep survival systems and simulation depth | Too many variables for 6–7; steeper comprehension | Preserve pressure, cap systems/recipes heavily |
| Terraria | iOS/Android | Strong progression and craft/combat payoff | Dense controls and text-heavy understanding | Keep progression dopamine in shorter, lower-text loops |
| Luanti (Minetest) | Android/online | Moddable sandbox freedom | Inconsistent onboarding/content quality | Curated one-mode experience beats mod complexity for this age |
| Crafting and Building | Android | Familiar block fantasy, low install barrier | Clone-noise UX quality and trust concerns | Differentiate with quality, safety, and original cast |
| MultiCraft | iOS/Android | Familiar survival vocabulary | Ad/noise stigma and less polished onboarding | Parent-trust defaults and clean HUD are strategic |
| PK XD | iOS/Android | Avatar expression + playful world | Social/monetization complexity and loop drift | Borrow charm and personality without social risk |
| KoGaMa | online | UGC experimentation and novelty | Safety/content consistency for young kids | No public UGC in MVP; local-only progression |
| Paper Minecraft | online | Instant lightweight fantasy hit | Basic presentation and shallow long-term loop | First 10 seconds must feel immediate, but retain progression depth |
| LEGO DUPLO World / Toca-style early learning sandboxes | iOS/Android | Strong low-text onboarding and parental trust | Paywall confusion and content gating frustration | Clarity in progression and no predatory interruptions |

### Reference links
- Minecraft (Android): https://play.google.com/store/apps/details?id=com.mojang.minecraftpe
- Minecraft (iOS): https://apps.apple.com/us/app/minecraft/id479516143
- Roblox (Android): https://play.google.com/store/apps/details?id=com.roblox.client
- Roblox (iOS): https://apps.apple.com/us/app/roblox/id431946152
- Block Craft 3D (Android): https://play.google.com/store/apps/details?id=com.fungames.blockcraft
- Block Craft 3D (iOS): https://apps.apple.com/us/app/block-craft-3d-building-games/id981633844
- Survivalcraft 2 (Android): https://play.google.com/store/apps/details?id=com.candyrufusgames.survivalcraft2
- Terraria (iOS): https://apps.apple.com/us/app/terraria/id640364616
- Luanti/Minetest docs: https://docs.luanti.org/for-players/mobile/
- PK XD (Android): https://play.google.com/store/apps/details?id=com.movile.playkids.pkxd
- KoGaMa (online): https://www.kogama.com/
- Paper Minecraft (online): https://www.miniplay.com/game/paper-minecraft
- LEGO DUPLO World (iOS): https://apps.apple.com/us/app/lego-duplo-world/id1458749093
- Toca Boca World (iOS): https://apps.apple.com/us/app/toca-boca-world/id1208138685

## 2) Child psychology loop analysis → concrete mechanics

### Autonomy
- **Need:** kids choose and feel ownership quickly.
- **v4 mechanics:** always-available mine/place loop, selectable place item cycle, persistent local world save.

### Competence
- **Need:** short achievable goals with visible mastery.
- **v4 mechanics:** tiny recipe set (4), clear shelter score meter, stars on sunrise, unlock tiers every 4 stars.

### Social imagination (without unsafe social systems)
- **Need:** role-play and emotional attachment.
- **v4 mechanics:** named archetypes (Glowmew helper, Boom Sprout chaser, Sky Wyrm event), icon-led emotional cues in HUD.

### Short-session reward design
- **Need:** complete tension-release cycle in 3–8 minutes.
- **v4 mechanics:** deterministic day/night cycle, dusk warning, night danger, sunrise rewards, forgiving rescue reset.

## 3) Design implications from market + psychology

1. Keep core verbs very small: mine, place, craft, survive.
2. Prioritize fantasy clarity over system count.
3. Avoid social and monetization complexity in MVP.
4. Make shelter effect measurable and immediate.
5. Keep UI icon-first with minimal reading required.
