# Tasks — Kids Minecraft Lite v4

## Phase A — Planning & deep analysis

- [x] Create `docs/fantasy-gap-analysis.md`
  - Acceptance: includes retrospective + legal-safe fantasy mapping table.
- [x] Create `docs/deep-research.md`
  - Acceptance: includes >=10 competitor teardown across iOS/Android/online, kid-psychology loop analysis, moat matrix.
- [x] Create `docs/prd.md`
  - Acceptance: goals, scope, safety/legal constraints, success criteria.
- [x] Create `docs/spec.md`
  - Acceptance: concrete gameplay/UX/technical specs tied to research.
- [x] Create `docs/technical-plan.md`
  - Acceptance: architecture, save/test strategy, risks/mitigations.
- [x] Create `docs/architecture-diagram.md`
  - Acceptance: clear component and data flow diagram.
- [x] Capture Codex CLI planning evidence
  - Acceptance: `docs/codex-planning-output.txt` exists.

## Phase B — Implementation (Android native)

- [x] Rename app/package from template to v4 identity
  - Acceptance: Gradle namespace/applicationId updated and app launches.
- [x] Implement voxel-like tile world renderer (2.5D acceptable)
  - Acceptance: visible block grid with day/night visual differentiation.
- [x] Implement mine/place loop
  - Acceptance: tile mining gives resources; placing consumes resources.
- [x] Implement tiny crafting panel (3–5 recipes)
  - Acceptance: valid craft success; invalid craft fails gracefully.
- [x] Implement day/night cycle with night danger
  - Acceptance: repeated transitions and increased danger at night.
- [x] Implement 3 mob archetypes
  - Acceptance: friendly helper, chaser threat, boss event all represented in state/UI.
- [x] Implement shelter score safety mechanic
  - Acceptance: safety score changes with enclosure and mitigates night risk.
- [x] Implement stars + unlock progression
  - Acceptance: stars earned from loop events and unlocks trigger by thresholds.
- [x] Implement local save/load
  - Acceptance: restart app and state persists.
- [x] Capture Codex CLI coding evidence
  - Acceptance: `docs/codex-coding-output.txt` exists.

## Phase C — Quality gates

- [x] Add unit tests for world logic
  - Acceptance: tests cover mine/place invariants.
- [x] Add unit tests for crafting
  - Acceptance: recipe success/failure validated.
- [x] Add unit tests for day/night
  - Acceptance: phase and night counters deterministic.
- [x] Add unit tests for mob rules
  - Acceptance: friendly/chaser/boss behaviors tested.
- [x] Add unit tests for shelter safety
  - Acceptance: score thresholds validated.
- [x] Run quality gate command
  - Acceptance: `./gradlew test assembleDebug` passes.

## Phase D — Delivery

- [x] Write `docs/release-summary.md`
  - Acceptance: feature checklist + quality output + Codex snippets + “why this now feels like Minecraft for kids”.
- [ ] Initialize git and commit final state
  - Acceptance: clean history with clear final commit.
- [ ] Push repository to GitHub `iamconanpeter/kids-minecraft-lite-v4`
  - Acceptance: remote repo accessible with final commit.
