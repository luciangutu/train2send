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

*   **`id`**: Unique string identifier for the plan (e.g., `plan-7c-target`).
*   **`title`**: Display name of the plan (e.g., "Target 7c").
*   **`isActive`**: Boolean indicating whether this is the currently active plan.
*   **`createdAt`**: Unix timestamp (ms) when the plan was created.
*   **`days`**: A list of `PlanDay` objects.

### Plan Day (`days`)
*   **`id`**: Unique string identifier for the day (e.g., `day-7c-mon`).
*   **`dayOfWeek`**: Integer `1` (Monday) through `7` (Sunday).
*   **`dayTitle`**: A descriptive name for the training day (e.g., "Strength & Power").
*   **`exercises`**: A list of `PlannedExercise` objects.

### Planned Exercise
These objects reference an exercise by ID and can override its default parameters.

*   **`id`**: Unique string identifier for this planned-exercise entry (e.g., `pe-7c-mon-1`). Must be unique within the file.
*   **`exerciseId`**: References the `id` from the global `exercises` list.
*   **`section`**: Where the exercise appears in the UI (`MAIN`, `SECONDARY`, `COMPLEMENTARY`). **Section is the primary sort key in the day view** — see [Ordering behaviour](#ordering-behaviour) below.
*   **`orderIndex`**: Integer controlling the order **within a section** (ascending; defaults to `0` if omitted). It does *not* order exercises across different sections. Alternatives in the same `alternativeGroupId` typically share the same `orderIndex`.
*   **`isSelected`**: Boolean determining if the exercise is active by default. Linked with **`climbingType`** and **`alternativeGroupId`** to toggle between rope and bouldering walls.
*   **`alternativeGroupId`**: (Optional) String ID to group exercises together as alternatives (only one can be selected at a time). Use `null` for standalone exercises.
*   **`description`**: (Optional) Per-plan note that overrides/augments the exercise's own `description` for this entry. Stored internally as `notes`.
*   **`custom...` Fields**: Override the exercise's matching `default` value when this plan is active. Supported fields: `customSets`, `customReps`, `customDurationSec`, `customRestSec`, `customRestBetweenSetsSec`, `customPrepareTimeSec`.

### Ordering behaviour

The order in which exercises appear in UI within a day is determined in **two stages**:

1.  **Section first (fixed order):** exercises are grouped by `section` and the groups are always rendered in this order regardless of `orderIndex`:
    `MAIN` → `SECONDARY` → `COMPLEMENTARY`.
2.  **`orderIndex` second (within each section):** inside a single section, exercises are sorted by `orderIndex` ascending.

