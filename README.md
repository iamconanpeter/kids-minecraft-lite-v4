# Kids Minecraft Lite v4 (Block Quest Lite)

Android native MVP focused on a kid-friendly survival sandbox loop:
- day/night urgency
- mine + place interaction
- tiny crafting (4 recipes)
- original mob archetypes (Glowmew / Boom Sprout / Sky Wyrm event)
- shelter safety score
- progression stars + local save

## Build & test

```bash
export JAVA_HOME=/home/openclaw/.openclaw/workspace/.local/jdk-17
export ANDROID_SDK_ROOT=/home/openclaw/.openclaw/workspace/.local/android-sdk
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew test assembleDebug
```

## Install debug APK

```bash
./gradlew installDebug
```
