# One active training cycle at a time

At most one `TrainingCycle` may have a null `endDate` at any given time. A new cycle (whether via `POST /api/v1/cycles` or `POST /api/v1/cycles/copy`) implicitly sets `endDate = today` on any currently-active cycle before creating the new one. The invariant is enforced in `TrainingCycleService`.

This matches the physical reality of a solo lifter: you run one program at a time. Allowing overlapping active cycles introduces an "active vs which-active" question for every consumer (the frontend, the export endpoint, the future history view) and creates a class of bug where a forgotten old cycle silently competes with the new one. The constraint costs one extra UPDATE on cycle creation and removes a permanent category of confusion.

A reader hitting `POST /cycles` and noticing that it mutates *other* rows should not "fix" the side effect — the side effect is the point.
