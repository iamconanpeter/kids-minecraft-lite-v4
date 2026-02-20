# Tasks — Kids Minecraft Lite v4

## A) Research and planning (mandatory depth)

- [x] Competitor teardown (>=10 references across iOS/Android/online)
  - Acceptance: includes what kids love, churn points, and design takeaways.
  - Evidence: `docs/market-analysis.md`
- [x] Child psychology loop analysis mapped to mechanics
  - Acceptance: autonomy, competence, social imagination, short-session reward mapping present.
  - Evidence: `docs/market-analysis.md`
- [x] Moat matrix with >=5 strategic moats + execution implications
  - Acceptance: moat and implementation consequences clearly documented.
  - Evidence: `docs/moat-and-positioning.md`
- [x] Retrospective of prior-version gap and v4 fixes
  - Acceptance: table maps each failure mode to correction.
  - Evidence: `docs/fantasy-gap-analysis.md`

## B) Planning docs deliverables

- [x] `docs/fantasy-gap-analysis.md`
- [x] `docs/market-analysis.md`
- [x] `docs/moat-and-positioning.md`
- [x] `docs/prd.md`
- [x] `docs/spec.md`
- [x] `docs/technical-plan.md`
- [x] `docs/architecture-diagram.md`
- [x] `docs/tasks.md`

## C) MVP implementation (Android native Gradle)

- [x] Day/night cycle
  - Acceptance: deterministic phase transitions and sunrise rewards.
- [x] Mine/place loop
  - Acceptance: mine collects resources; place consumes inventory.
- [x] Tiny crafting (3–5 recipes)
  - Acceptance: 4 recipes with input checks and outputs.
- [x] Original mob archetypes
  - Acceptance: Glowmew helper, Boom Sprout chaser pressure, Sky Wyrm periodic boss-event pressure.
- [x] Shelter safety score
  - Acceptance: score updates with enclosure/torch state and influences night danger.
- [x] Progression + local save
  - Acceptance: stars/tier progression and SharedPreferences save/load payload.
- [x] Icon-first minimal text, kid-friendly controls
  - Acceptance: emoji-forward HUD/buttons with large touch targets.

## D) Quality gates

- [x] Unit tests for world/crafting/mob/day-night/shelter logic
  - Acceptance: tests exist in `app/src/test/.../BlockQuestLiteEngineTest.kt`
- [x] `./gradlew test assembleDebug` pass run recorded
  - Acceptance: successful command output captured in release summary.

## E) Delivery

- [x] Codex CLI planning evidence
  - Acceptance: `docs/codex-planning-output.txt`
- [x] Codex CLI coding evidence
  - Acceptance: `docs/codex-coding-output.txt`
- [x] `docs/release-summary.md`
  - Acceptance: includes codex snippets + test output + “Why this feels like Minecraft for kids”.
- [ ] Git init/commit/push to `iamconanpeter/kids-minecraft-lite-v4`
  - Acceptance: remote contains final commit hash.
