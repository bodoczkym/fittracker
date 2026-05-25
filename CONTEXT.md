# FitTracker

Personal bodybuilding workout tracker for a single user (the author). Models periodised strength training: a fixed-length plan, executed across repeated rotations through the planned workout days, with actual performance logged against a prescribed template.

## Language

**Training cycle**:
A periodised training block consisting of a fixed number of microcycles, all following the same workout template. Has a start date and an optional end date set when the cycle finishes.
_Avoid_: mesocycle, block, program

**Active cycle**:
The single **training cycle** the user is currently executing — the one with no `endDate` set. At most one active cycle exists at any time; starting a new cycle implicitly ends the previous active one.
_Avoid_: current cycle, open cycle (in code; "current" is OK in conversation)

**Microcycle**:
One full rotation through the planned workout days. Typically 7–9 calendar days depending on rest days; not a calendar week. Numbered 1..N within a training cycle.
_Avoid_: week, training week

**Workout day**:
A slot in the cycle template (e.g. "Day 2 — Push"). Executed once per microcycle.
_Avoid_: training day, session template

**Workout session**:
A single executed workout — one instance of a **workout day** inside a specific **microcycle**.
_Avoid_: workout, training session, log

**Planned exercise**:
An exercise prescribed inside a **workout day** template, with sets, rep range, rest period, and target RPE. Reused across every **microcycle**.
_Avoid_: prescribed exercise, programmed exercise

**Exercise log**:
The actual performance recorded for an exercise during a **workout session** — actual sets, reps, RPE, notes.
_Avoid_: set log, performance log

**Exercise**:
A reusable movement in the catalog (e.g. "Barbell Bench Press"), classified by **exercise category**.
_Avoid_: lift, movement

**Exercise category**:
A muscle-group / movement-pattern bucket used to organise the exercise catalog (e.g. `BENCH_PRESS`, `HORIZONTAL_PULL`, `ACCESSORY_BICEPS`).
_Avoid_: muscle group, movement pattern (as a top-level concept)

## Example dialogue

> **Dev**: I'm adding the "copy previous cycle" endpoint — does it copy the workout sessions too?
> **Author**: No. It copies the **training cycle**, its **workout days**, and their **planned exercises**. Sessions are the actual workouts I do later — those start empty.
> **Dev**: Got it. So when I open Day 2 of microcycle 3, I'll see the planned exercises from the template plus any exercise logs I've already recorded for that session?
> **Author**: Right. Planned exercises live on the workout day. Exercise logs live on the workout session.
