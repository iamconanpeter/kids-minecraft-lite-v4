# Release Summary — Kids Minecraft Lite v4.2 (Gameplay + UI Polish)

Date: 2026-02-20  
Repo: https://github.com/iamconanpeter/kids-minecraft-lite-v4

## Scope delivered (v4.2)

1. **Shelter scoring upgraded for real safety + light quality**
   - Replaced flat shelter scoring with a structured shelter evaluator.
   - Safety now accounts for enclosure integrity, roof/floor support, openings, and sky exposure.
   - Light quality now accounts for torch proximity and darkness penalties.
   - New/changed methods:
     - `BlockQuestLiteEngine.evaluateShelter`
     - `BlockQuestLiteEngine.refreshShelter`
     - `BlockQuestLiteEngine.isOpenToSky`
     - `BlockQuestLiteEngine.isStructural`

2. **Gentler day-night pressure tuning for age 6-7**
   - Retuned night threat chance with softer early-day/easy-mode pressure and clearer scaling by shelter + threat.
   - Added calmer damage cadence for easy mode.
   - New/changed methods:
     - `BlockQuestLiteEngine.computeNightPressureChance`
     - `BlockQuestLiteEngine.computeNightDamageCooldownTicks`
     - `BlockQuestLiteEngine.processNightPressure`

3. **Kid-friendly progression: buddy trust meter**
   - Added persistent `buddyTrust` and trust-based hint charges.
   - Trust now influences hint availability and sunrise reward bonus.
   - New/changed methods:
     - `BlockQuestLiteEngine.adjustBuddyTrust`
     - `BlockQuestLiteEngine.computeBuddyHintCharges`
     - `BlockQuestLiteEngine.deliverBuddyHint`
     - `BlockQuestLiteEngine.onSunrise`

4. **Fairness improvement: adaptive grace after repeated failures**
   - Added rescue streak tracking and adaptive grace nights.
   - Repeated rescues now activate temporary protection and support item assistance.
   - New/changed methods:
     - `BlockQuestLiteEngine.applyAdaptiveGraceAfterRescue`
     - `BlockQuestLiteEngine.rescuePlayer`
     - `BlockQuestLiteEngine.startNightPhase`

5. **UI/UX polish (icon-first, low-text, readable, touch-safe)**
   - Added persistent objective strip during play (`🎯` icon-first objective always visible).
   - Added strong action feedback chip with tone colors (success/error/warning/danger) and minimal text.
   - Upgraded onboarding visuals to clearer step-node flow + completion signal (`🎉`).
   - Increased bottom control target height to 72dp and normalized button hit regions.
   - Improved contrast/readability with darker HUD/chip surfaces and brighter text.
   - New/changed methods in view:
     - `BlockQuestLiteView.drawFeedbackChip`
     - `BlockQuestLiteView.drawObjectiveStrip`
     - `BlockQuestLiteView.drawOnboarding`
     - `BlockQuestLiteView.drawControls`
     - `BlockQuestLiteView.feedbackColor`
     - `BlockQuestLiteView.objectiveFor`

6. **Unit tests expanded for all new logic methods**
   - Added direct tests for:
     - `evaluateShelter`
     - `computeNightPressureChance`
     - `computeBuddyHintCharges`
     - `adjustBuddyTrust`
     - `applyAdaptiveGraceAfterRescue`
   - Added integration tests for trust-based rewards and save/restore of new state fields.
   - File:
     - `app/src/test/java/com/iamconanpeter/kidsminecraftlite/BlockQuestLiteEngineTest.kt`

## Why this is better for kids (6-7)

- Safer learning loop: better shelter scoring teaches enclosure + lighting, not just random block placement.
- Lower frustration curve: easier early nights and adaptive grace reduce fail spirals.
- Positive progression signal: buddy trust creates clear, friendly reinforcement with hints and small bonus rewards.
- Faster comprehension: persistent objective + color/emoji feedback reduce reading load and improve action clarity.

## Changed files

- `app/src/main/java/com/iamconanpeter/kidsminecraftlite/BlockQuestLiteEngine.kt`
- `app/src/main/java/com/iamconanpeter/kidsminecraftlite/BlockQuestLiteView.kt`
- `app/src/test/java/com/iamconanpeter/kidsminecraftlite/BlockQuestLiteEngineTest.kt`
- `docs/release-summary.md`

## Codex CLI evidence snippets / commands used

Commands run:

```bash
ls -la
find app/src/main -type f | sort
find app/src/test -type f | sort
./gradlew test assembleDebug
JAVA_HOME=/home/openclaw/.openclaw/workspace/.jdks/jdk-17.0.18+8 PATH=/home/openclaw/.openclaw/workspace/.jdks/jdk-17.0.18+8/bin:$PATH ./gradlew test assembleDebug
```

Output evidence snippets:

```text
Android Gradle plugin requires Java 17 to run. You are currently using Java 11.
```

```text
> Task :app:testDebugUnitTest
> Task :app:testReleaseUnitTest
> Task :app:test
> Task :app:assembleDebug

BUILD SUCCESSFUL in 10s
64 actionable tasks: 14 executed, 50 up-to-date
```

## Quality gate

Executed:

```bash
JAVA_HOME=/home/openclaw/.openclaw/workspace/.jdks/jdk-17.0.18+8 PATH=/home/openclaw/.openclaw/workspace/.jdks/jdk-17.0.18+8/bin:$PATH ./gradlew test assembleDebug
```

Result: PASS

---

## Prior release note

v4.1 summary content was superseded by this v4.2 release and focused mainly on onboarding/control UX changes.
