# Snapshot model for session exercise logs

When a `WorkoutSession` is created, the service copies each `PlannedExercise` from the parent `WorkoutDay` template into a new `ExerciseLog` row, snapshotting the target sets / rep range / rest / RPE at that moment. The `ExerciseLog` has no foreign key back to the `PlannedExercise` it was copied from — only an `exercise_id` pointing at the catalog `Exercise`.

This means edits to the cycle template never propagate to sessions that have already been created. Fix a typo in microcycle 4's planned exercises and microcycles 1-3's sessions stay exactly as they were. New sessions (microcycle 5 onward) pick up the new template at session-create time.

The trade-off considered: a live FK (`planned_exercise_id` on `ExerciseLog`) would allow retroactive updates and "% adherence to plan" analytics. Both were rejected — retroactive updates surprise the user mid-cycle, and adherence analytics aren't in scope (this is a logbook, not a coaching tool). The snapshot model also makes it safe to delete or restructure a cycle's template without orphaning historical logs.

A reader who sees no FK from `ExerciseLog` to `PlannedExercise` and assumes it's an oversight should not add one without revisiting this decision.
