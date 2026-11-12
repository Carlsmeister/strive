# SetRows

A's set rows as D draws them: badge · Previous · values · ✓, with rest lines between and the plates under the next barbell set.

Every set has its own ✓ that logs it as it stands. Provide `.srow` with `.badge` (number; `.w` W amber, `.f` failure number coral, `.d` D violet; a tap changes the kind), `.sprev` (last time, "80 × 8 @3"), value `.cell`s (64 and 56 pt; one 88 pt cell for time or distance; a tap opens the dock on that field), and `.ok`. `.srow.done` drops the cell fills and fills the ✓; `.srow.next` is tinted (`state-next-row`) with an outlined ✓. Rest lines `.restl` sit between sets, `.restl.cur` after the last logged one. A superset wraps its rows in `.ssgroup` under `.sslabel` ("Superset · rest 1:30 after each round") with `.tagl` letters, in lifting order. Swipe a row left to delete it.
