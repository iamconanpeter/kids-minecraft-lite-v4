# Kids Minecraft Lite v4 (Block Quest Lite)

A kid-first Android block-world survival MVP for ages 6–7+.

## Core loop
- Mine + place blocks
- Craft a tiny set of survival recipes
- Build shelter before night
- Survive chaser pressure and periodic boss events
- Earn stars and unlock stronger building options

## Safety & legal
- Original character/archetype implementation (no trademarked names/assets)
- Offline local save only
- No chat/UGC in MVP
- Optional parent easy mode

## Build & test

```bash
export JAVA_HOME=/home/openclaw/.openclaw/workspace/.local/jdk-17
export ANDROID_SDK_ROOT=/home/openclaw/.openclaw/workspace/.local/android-sdk
export PATH=$JAVA_HOME/bin:$PATH
./gradlew test assembleDebug
```

## Install

```bash
./gradlew installDebug
```

## Planning and research docs
- `docs/deep-research.md`
- `docs/fantasy-gap-analysis.md`
- `docs/prd.md`
- `docs/spec.md`
- `docs/technical-plan.md`
- `docs/architecture-diagram.md`
- `docs/tasks.md`
