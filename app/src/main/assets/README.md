# Training Plan JSON Structure

This directory contains training plans and exercise libraries for the Train2Send app in JSON format. These files can be imported into the app via the Backup/Import feature or synced from the online repository.

## Top-Level Objects

| Key | Type | Description |
| :--- | :--- | :--- |
| `version` | `Int` | The backup schema version (currently `2`). |
| `exportedAt` | `Long` | Unix timestamp (ms) when the data was exported. |
| `exercises` | `Array` | A list of exercise definitions available for use in plans. |
| `plans` | `Array` | A list of training plans consisting of scheduled days and exercises. |

---

## Exercise Definition (`exercises`)

Defines the default properties for a specific exercise type.

*   **`id`**: Unique string identifier (e.g., `ex-max-finger`).
*   **`name`**: Display name of the exercise.
*   **`category`**: Enum string (`STRENGTH`, `POWER`, `POWER_ENDURANCE`, `ENDURANCE`, `MOBILITY`, `CONDITIONING`).
*   **`climbingType`**: Enum string (`BOULDERING`, `ROPE`, `ANY`). Used to toggle between exercises based on the training environment: rope wall or bouldering.
*   **`description`**: Detailed instructions or notes for the exercise.
*   **`defaultSets` / `defaultReps`**: Default count for sets and repetitions.
*   **`defaultDurationSec`**: Default duration of a single repetition (in seconds).
*   **`defaultRestSec`**: Default rest duration between repetitions (in seconds).
*   **`defaultRestBetweenSetsSec`**: Default rest duration between sets (in seconds).
*   **`defaultPrepareTimeSec`**: Default initial countdown before the exercise starts (defaults to 3s if omitted).

---

## Plan Structure (`plans`)

A plan organizes exercises into specific days of the week.

### Plan Day (`days`)
*   **`dayOfWeek`**: Integer `1` (Monday) through `7` (Sunday).
*   **`dayTitle`**: A descriptive name for the training day (e.g., "Strength & Power").
*   **`exercises`**: A list of `PlannedExercise` objects.

### Planned Exercise
These objects reference an exercise by ID and can override its default parameters.

*   **`exerciseId`**: References the `id` from the global `exercises` list.
*   **`section`**: Where the exercise appears in the UI (`MAIN`, `SECONDARY`, `COMPLEMENTARY`).
*   **`isSelected`**: Boolean determining if the exercise is active by default. Linked with **`climbingType`** and **`alternativeGroupId`** to toggle between rope and bouldering walls.
*   **`alternativeGroupId`**: (Optional) String ID to group exercises together as alternatives (only one can be selected at a time).
*   **`custom...` Fields**: Any field prefixed with `custom` (e.g., `customSets`, `customPrepareTimeSec`) will override the exercise's `default` value when this plan is active.
