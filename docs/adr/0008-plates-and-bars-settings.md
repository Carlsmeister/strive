# 0008. The gym's plates and bars: two settings per unit

- Status: accepted
- Date: 2026-11-20
- Issue: #48

## Context

#48 loads a total onto the bar and builds a warm-up ladder, so it needs the plates and bars
Carl's gym has. **The decision reaches beyond #48:** #63 builds the screen that edits both
lists, and it must write exactly what #48 reads. Weights follow the exercise's unit (ADR
0007), so a gym's plates in kg say nothing about its plates in lb.

## Options

**Two settings per unit, each a comma-joined list of weights.** No migration: the `setting`
table and the `Settings` interface already exist. A list carries no plate counts.

**A table of plates and bars.** Room for a count per plate and a name per bar. A migration,
queries and a repository for two short lists that nothing edits yet.

**One setting holding JSON.** Room for counts and names in one key. A serializer and a schema
inside a string, for the same two lists.

## Decision

**Four settings, one plates list and one bars list per unit, read through `data/Plates.kt`.**

- **The keys are `plates_<unit>` and `bars_<unit>`,** where `<unit>` is `WeightUnit.key`:
  `plates_kg`, `plates_lb`, `bars_kg`, `bars_lb` (`platesKey`, `barsKey`).
- **A value is weights joined by `,` with `.` decimals,** such as `25,20,10,5,2.5,1.25`.
  Spaces around an entry are allowed.
- **Reading forgives a bad value.** An entry that is not a finite number, or rounds to under
  0.01, is skipped. A key that is unset, or keeps no entry, reads as the default
  (`Settings.weights`). A corrupt setting never reaches the plate maths.
- **The default plates** are every kg plate but 15: 25, 20, 10, 5, 2.5, 1.25. In lb they are
  EL-07-lb's 45, 35, 25, 10, 5, 2.5 (`defaultPlates`, plan ruling 2).
- **The default bars** are Carl's gym: Olympic, Short, EZ and Hex, which weigh 20, 15, 7.5
  and 25 kg or 45, 35, 15 and 55 lb (`defaultBars`, plan ruling 7).
- **None is always offered and never stored** in a bars list. An exercise without a bar
  stores a null `exercise.bar`.
- **A bar is stored as a weight, never a name.** `exercise.bar` holds the number, and
  `namedBar()` names it from the named bar in `Bars` that weighs it in the exercise's unit:
  20 kg reads "Olympic 20 kg". A weight no named bar has reads "Bar 10". Switching an
  exercise's unit in WO-12 keeps a named bar by name (Olympic 20 kg becomes Olympic 45 lb);
  an unnamed one keeps its number.
- **#63's control writes these keys,** in this format. Nothing writes them yet, so every gym
  reads the defaults.

Settings, because two short lists of numbers per unit need no schema of their own, and the
`weight_unit` key (ADR 0007) already set the pattern. Weights over names, because the plate
maths and the ladder need the number, and the name follows from it.

## Consequences

- **Easier:** #63 writes four strings and needs no migration. A typo in a stored list costs
  one plate, not the screen.
- **Not built: plate counts.** `load()` assumes unlimited plates of each size. A count per
  plate needs a new format, and a new record superseding this one.
- **An empty list cannot be stored.** It reads as the default, so a gym with no plates in a
  unit is not expressible. #63 must not offer "no plates" as a state, or must change this.
- **A bar's name follows from its weight.** A custom bar with a name of its own needs that
  name stored beside its weight.
- **Revisit when** #63 needs plate counts, named custom bars, or an empty plates list.
