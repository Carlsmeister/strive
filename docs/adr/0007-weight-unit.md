# 0007. Weight unit: one per exercise, stored weights never converted

- Status: accepted
- Date: 2026-11-17
- Issue: #58

## Context

Until #58 a weight was a bare number with no unit, read as kg. **The spec
stores weights in the exercise's unit:** the number Carl entered is the number he sees again.
The decision reaches beyond #58. #63 builds the control for the app default, and #48 builds
bars, whose weights follow the exercise's unit.

## Options

**A unit per exercise, weights stored in it.** One nullable column. A set keeps the number
Carl typed. Only a total across exercises has to convert. A stored weight's unit is whatever
its exercise says today, so changing that unit rereads history.

**A unit per set.** Every set carries its own unit, so no change rereads anything. A column on
`workout_set`, a unit on every write, and a unit to pick on every input for a choice Carl
makes once per exercise.

**Everything stored in kg, converted for display.** One unit in the database. Every read and
write converts, and a weight typed in lb is stored as a fraction of a kilo.

## Decision

**One unit per exercise, nullable, with weights stored in that unit and never converted.**

- **`exercise.unit` is `kg`, `lb` or null** (`5.sqm`, `Exercise.sq`). Null means the app
  default. No seeded exercise carries a unit; Carl sets one in WO-12 (`setSettings`).
- **The app default is the setting `weight_unit`** (`UNIT_KEY` in `data/Units.kt`), read by
  `defaultUnit()`, kg until one is stored. #63's control writes that key. Nothing writes it yet.
- **`unitIn(default)` gives the unit an exercise's weights read in:** its own, or the default.
- **Only totals across exercises convert,** through the one `convert()` in `data/Units.kt`
  (1 lb = 0.45359237 kg). Today that is `summary()`'s volume in `workout/Logic.kt`: each set
  converts from its exercise's unit, or the default when that is null, into the default.

Per exercise, because the spec stores weights in the exercise's unit and Carl picks a unit per
exercise, not per set. Not converted, because a converted kg number never reads back as the lb
Carl typed.

## Consequences

- **Easier:** a set is the number Carl entered. History's volume adds kg and lb correctly.
- **Not built: nothing converts stored weights when the unit behind them changes.** Three
  paths reinterpret them instead. Changing an exercise's unit in WO-12 rereads its logged
  weights; this is #58's ruling. Swapping to an exercise in another unit moves the logged sets
  over unchanged (`swapExercise` in `Session.sq`). Changing the app default rereads every
  exercise whose unit is null, which is every exercise logged before #58. None has happened:
  nothing writes `weight_unit`, and no seeded exercise has a unit. The issue filed with #58's
  PR covers the three paths.
- **#63 must pin null units to the old default before it changes the default,** in the same
  transaction: `UPDATE exercise SET unit = :old WHERE unit IS NULL`. Without it, the first
  switch to lb turns every logged 100 kg into 100 lb.
- **Revisit when** a reread path has to convert instead, or Carl needs one exercise in two
  units, which only a unit per set allows.
