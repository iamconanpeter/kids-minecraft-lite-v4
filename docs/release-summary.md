# Release Summary — Kids Minecraft Lite v4.1 (UX Patch)

Date: 2026-02-20
Repo: https://github.com/iamconanpeter/kids-minecraft-lite-v4
Commit: `b7a8cae`

## Scope delivered (UX Patch v4.1)

1. **Control touch targets increased to kid-safe size**
   - Bottom control buttons raised from ~44dp to **64dp height** with larger icon surfaces.
   - Files: `app/src/main/java/com/iamconanpeter/kidsminecraftlite/BlockQuestLiteView.kt`

2. **HUD reduced from text-heavy to icon-first**
   - Top bar now prioritizes icon status chips (time/day, hearts, stars, shelter health, mode/item, danger mood).
   - Long text lines removed from primary HUD path.
   - Files: `app/src/main/java/com/iamconanpeter/kidsminecraftlite/BlockQuestLiteView.kt`

3. **Onboarding 3-step flow added**
   - New visual onboarding strip with progress for:
     - mine block
     - place block
     - build shelter before night
   - Added engine progress state (`blocksMined`, `blocksPlaced`, `onboardingShelterBuilt`) + persistence.
   - Files:
     - `app/src/main/java/com/iamconanpeter/kidsminecraftlite/BlockQuestLiteView.kt`
     - `app/src/main/java/com/iamconanpeter/kidsminecraftlite/BlockQuestLiteEngine.kt`

4. **Craft lock feedback changed to icon/color cues**
   - Replaced text lock messaging pattern with lock/unlock iconography (`🔒` / `🔓`) and color-coded craft cards.
   - Minimal star gating cue remains as icon-only star markers.
   - Files:
     - `app/src/main/java/com/iamconanpeter/kidsminecraftlite/BlockQuestLiteView.kt`
     - `app/src/main/java/com/iamconanpeter/kidsminecraftlite/BlockQuestLiteEngine.kt`

5. **Parent UX entry + calm/easy helper added**
   - Added dedicated **Parent** control button (`👪`) in primary controls for discoverability.
   - Added parent panel containing calm/brave toggle with short helper copy.
   - Files: `app/src/main/java/com/iamconanpeter/kidsminecraftlite/BlockQuestLiteView.kt`

6. **Gameplay logic preserved**
   - Core cycle/survival/crafting systems unchanged; UX state tracking added only to support onboarding feedback and persistence.

7. **Tests updated for behavior change**
   - Added onboarding state persistence test.
   - File: `app/src/test/java/com/iamconanpeter/kidsminecraftlite/BlockQuestLiteEngineTest.kt`

## Changed files

- `app/src/main/java/com/iamconanpeter/kidsminecraftlite/BlockQuestLiteView.kt`
- `app/src/main/java/com/iamconanpeter/kidsminecraftlite/BlockQuestLiteEngine.kt`
- `app/src/test/java/com/iamconanpeter/kidsminecraftlite/BlockQuestLiteEngineTest.kt`
- `docs/release-summary.md`
- `docs/codex-ux-v4_1-output.txt`

## Codex CLI evidence snippet (mandatory)

Source: `docs/codex-ux-v4_1-output.txt`

> "Added onboarding progress state (`blocksMined`, `blocksPlaced`, `onboardingShelterBuilt`) with save/restore support"
>
> "Bottom controls redesigned to larger icon+caption buttons; easy mode moved into new parent panel"
>
> "Craft panel restyled with unlock/lock visuals and star cues"

## Quality gates

Executed:

```bash
JAVA_HOME=/home/openclaw/.openclaw/workspace/.jdks/jdk-17.0.18+8 PATH=/home/openclaw/.openclaw/workspace/.jdks/jdk-17.0.18+8/bin:$PATH ./gradlew test assembleDebug
```

Output excerpt:

```text
> Task :app:testDebugUnitTest
> Task :app:test
> Task :app:assembleDebug

BUILD SUCCESSFUL in 1s
64 actionable tasks: 5 executed, 59 up-to-date
```
