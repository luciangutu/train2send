# Train4Send — Climbing Training App

A native Android app (Kotlin + Jetpack Compose) to schedule, execute, and track climbing training programs. Fully configurable — no hardcoded routines.

---

## Training Categories

| Category | Color | Purpose |
|----------|-------|---------|
| **Endurance** | 🔵 `#1E88E5` | Aerobic capacity (ARC, 4x6, 1-on-1-off) |
| **Power Endurance** | 🟠 `#FB8C00` | Anaerobic intervals (Intensive Triples, 8-set circuits) |
| **Strength** | 🔴 `#E53935` | Max finger strength, low rep pulling |
| **Power** | 🟣 `#8E24AA` | Explosive contact strength, dynos |
| **Mobility & Recovery** | 🟢 `#4CAF50` | Stretching, CARs, tissue care |
| **Antagonist & Core** | 🔵 `#00ACC1` | Push-ups, TRX, shoulder stability |

---

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

---

## Project Structure

```
app/src/main/java/com/train4send/
├── Train4SendApp.kt           # Application class (DI / repo init)
├── MainActivity.kt            # Single activity, edge-to-edge
├── data/
│   ├── model/                 # Room entities + enums
│   │   ├── ExerciseCategory.kt
│   │   ├── ExerciseSection.kt
│   │   ├── ExerciseEntity.kt
│   │   ├── TrainingPlanEntity.kt
│   │   ├── PlanDayEntity.kt
│   │   ├── PlannedExerciseEntity.kt
│   │   └── WorkoutLogEntity.kt
│   ├── dao/                   # Room DAOs
│   │   ├── ExerciseDao.kt
│   │   ├── TrainingPlanDao.kt
│   │   └── WorkoutLogDao.kt
│   ├── local/
│   │   ├── AppDatabase.kt    # Room database singleton
│   │   └── Converters.kt     # Type converters for enums
│   └── repository/
│       ├── ExerciseRepository.kt
│       ├── TrainingPlanRepository.kt
│       └── WorkoutLogRepository.kt
├── domain/
│   └── timer/
│       ├── TimerState.kt      # Sealed class (Idle/Running/Paused/Finished)
│       └── FlexibleTimerEngine.kt  # Coroutine-based interval timer
└── ui/
    ├── theme/Theme.kt         # Material 3 dynamic color theme
    ├── navigation/
    │   ├── Screen.kt          # Route definitions
    │   └── AppNavigation.kt   # NavHost setup
    └── screens/
        ├── home/HomeScreen.kt
        ├── exercises/
        │   ├── ExerciseListScreen.kt
        │   └── ExerciseCreateScreen.kt
        ├── timer/TimerScreen.kt
        └── history/HistoryScreen.kt
```

---

## Build & Run

```bash
# Clone and open in Android Studio
# Requires: Android Studio Koala+ / AGP 8.5+ / JDK 17

./gradlew assembleDebug
```

Min SDK: 26 (Android 8.0) · Target SDK: 35
