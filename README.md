# Train4Send — Climbing Training App

A native Android app (Kotlin + Jetpack Compose) to schedule, execute, and track climbing training programs. Fully configurable — no hardcoded routines. Made with AI.

## Screenshots

![Main Screen](screenshots/main_screen.png)

## Training Categories

| Category | Color | Purpose |
|----------|-------|---------|
| **Endurance** | 🔵 | Aerobic capacity (ARC, 4x6, 1-on-1-off) |
| **Power Endurance** | 🟠 | Anaerobic intervals (Intensive Triples, 8-set circuits) |
| **Strength** | 🔴 | Max finger strength, low rep pulling |
| **Power** | 🟣 | Explosive contact strength, dynos |
| **Mobility & Recovery** | 🟢 | Stretching, CARs, tissue care |
| **Antagonist & Core** | 🔵 | Push-ups, TRX, shoulder stability |

## Architecture

```
[ UI Layer — Jetpack Compose + Material 3 ]
                    │
[ ViewModel Layer — StateFlow / Events ]
                    │
[ Domain Layer — Timer Engine / Use Cases ]
                    │
[ Data Layer — Room DB / Repositories ]
```

**Stack:** Kotlin 2.0 · Compose BOM 2024.06 · Room · Navigation Compose · Material 3

## Build & Run

```bash
./gradlew assembleDebug
```

Requires Android Studio Koala+ / AGP 8.5+ / JDK 17

Min SDK: 26 (Android 8.0) · Target SDK: 35

## License

This project is licensed under the [GNU General Public License v3.0](LICENSE).
