# Design 1 — Strive as built, polished and completed

**Status: fixed after the review — 2026-11-10.** Every screen and state in the inventory has a board: 333 screen boards,
4 motion boards and 86 mode sheets, all published (423 files). **All 64 flows play tap by tap** (`tools/flows.py --check`
over `design-1/flows.json`). What is not verified and what is missing are at the end, said plainly.

- System: https://claude.ai/artifact/3cuFKpGnj48MxnEphtVvav (Strive 1 system, v7)
- Canvas: https://claude.ai/artifact/D938qJPYBGyUgVKonitcsE (Strive 1, v12)

## Review fixes (2026-11-10)

All ten findings in `review.md`'s design 1 section are fixed; none was judged wrong.

1. **16 broken flows.** Every flow is now a board path in `flows.json` and checked tap by tap; all 64 play (see Flows
   wired). New boards for the stand-ins: WO-02-benA, WO-02-pulla, PL-04-pulla (F-04, F-36, F-64), WO-02-swap (F-19),
   WO-02-s5c (F-15, A 2 in focus), WO-01-logged and PL-01-saved (F-28), PL-05-added and PL-01-axlar with PL-06-* redrawn
   over the new template (F-38), WO-25-dock (F-30), EX-09-picker and EX-03-created (F-46), EX-11-done → EX-10-archived
   (F-47), EX-05/06/07/08-incline (F-44), EL-02-next and EL-02-6mobar (F-53), WO-02-note, WO-09-sticky, WO-02-removed
   (F-18). Relinks (`tools/relink.py`): ⌄ while resting → WO-17-rest, Plan from Home with a workout open → WO-17-plan
   (F-22); EL-01's Import and Account and sync rows → EL-15/EL-16, Food (v1) → EL-17 (F-62); EX-01-filter's Incline row;
   SH-07-start → SH-07-check (F-63); the rest day's Pull A → Pull A.
2. **O-25.** Header, minimised bar, Home's card and the Live Activity count working sets only: "1 of 14 sets", "1/14"
   (91 counts on 70 boards, `tools/o25.py`; Armar has no warm-up and stays "0 of 9").
3. **Eight missing states.** PL-02-short, WO-13-warmup, EX-11-done, PL-03-archived, PL-08-runmenu, -runtarget and -tempo
   (O-12), WO-02-abc (O-21), RU-02-island (O-07), WO-02-note and WO-09-sticky (F-18).
4. **Dark-HC red.** `color-error` (text) #ffbdb6: 8.8:1 on surface-container-high, 7.1:1 on -highest. The swipe slab
   keeps #ff6961 as `color-error-container` with black text.
5. **Exercise menu order.** Exercise details now sits after Exercise settings (WO-09-menu, -superset, -noweight,
   WO-10-one; WO-09-rest shows only its submenu). Listed as a polish.
6. **Unlisted polishes.** Every board of WO-01, WO-03–WO-17, WO-21, WO-24, WO-26 and EX-01–EX-08 was diffed against
   strings.xml and the Kotlin on `origin/dev` (`design-1/polish-diff.json`). Twelve rewordings went back to built copy
   (`tools/revert_copy.py`): the four mark captions, the quick-picks caption, "No", "20 kg", the Rename, Change start
   time, Create superset and Custom time… titles, "Discard workout", the summary's "Workout note". Everything else is
   listed under Polishes.
7. **Contradictions.** WO-02-s15 is Copenhagen Plank (no history; EX-03-search finds it); the 8 Nov long run wears Nike
   Pegasus 41 on EL-12 too; WO-18 says set 2 like WO-19; PL-01-calib says "1 week without a deload"; WO-03's strip has no RIR.
8. **Board count.** The status line counts 333 + 4 + 86.
9. **Dynamic Island colours.** `var(--color-tertiary)` and `var(--color-primary)`; `.island` (and `.sys-dark` outside it)
   resolves the dark scheme's accents in the light modes, since the island is always dark.
10. **Springs.** The README gives the release spring as Compose `spring(dampingRatio = 0.6f, stiffness = 400f)` and
    SwiftUI `.spring(response: 0.31, dampingFraction: 0.6)` beside the curve.

## The language in five lines

- **Colours:** strive's dark-first teal tokens exactly (15-tokens), four modes (dark, dark-hc, light, light-hc); teal only for what can be tapped; amber rest and warm-up, coral failure, violet drop, blue superset; surface roles, never tonal fills in Increase Contrast.
- **Type:** the system face (SF Pro / Roboto), Apple's text styles under Material 3 names, tabular figures; D's 38 pt bold focus values, the uppercase NOW-label.
- **Layout:** D's workout (large title, focus card, A's set rows, one overview card); solid cards and lists, glass only on bars and overlays; rule A lists.
- **Navigation:** #14's five places on a floating glass tab bar with Start as the round action in the middle; Start opens four bubbles in an arc (14c); pushed screens keep their tab.
- **Motion:** press 100 ms, release spring 350 ms (slight overshoot), fade 250 ms, stagger 40 ms; logging a set slides the tint and raises the rest card; Reduce Motion keeps only fades.

## How the canvas is built

- Every board is one screen state at 390 × 844, labelled `ID · state · dark-HC`. **The mode is a Tweak on every board**
  (dark-hc default, dark, light, light-hc). The "All modes" page holds one `MD-…` sheet per screen ID that imports its
  first state in dark, light and light-HC side by side.
- Pages in the inventory's order: Shell, Workout · logging, Workout · around it, Workout · after, Plan, Exercises,
  Running, You, Later, Motion, All modes. A title note over every row; a motion sticky at the end of every row
  (`design-1/motion.json`); four timed motion boards on the Motion page (MO-log, MO-rest, MO-start, MO-gestures).
- Prototype: every tap is a link between boards (`<a href>`); press Play on Main (Home). WO-02-s2 also scrolls and folds
  the focus card into the strip in place.
- Sources of truth on disk: `design-1/boards.md` (the registry: every board, what it shows, where each tap leads),
  `design-1/flows.json` (every flow as board paths), `design-1/tools/` (lint, coverage, `flows.py`, previews).

## Coverage

Every inventory screen → its boards. Each board switches between all four modes; the `MD-…` sheet shows three at once.
Where a screen lists more states than it has boards, the other states are drawn on a neighbouring board or on the
Motion page — the notes after the table say where.


| Screen | States in the inventory | Boards (each switches dark, dark-HC, light, light-HC; `MD-…` sheet shows three at once) |
|---|---|---|
| SH-01 Tab bar and app frame | each tab selected; workout bar shown; Start open (+ turned to ×, SH-03 over the   screen); a pushed screen with the standard back chevron; Reduce Transparency (solid surface) | `SH-01-rt` (Reduce Transparency) · `MD-SH-01-rt` |
| SH-02 Home | a strength day; a run day (card offers the run); a rest day; today done (card shows   Show workout); a workout in progress (card says Continue Push A · 24:13); no programme and no   templates (first use: Create template / Start empty workout); GPS-gap warning | `Main` (strength day), `SH-02-run` (run day), `SH-02-rest` (rest day), `SH-02-done` (today done), `SH-02-inprog` (workout in progress), `SH-02-first` (first use), `SH-02-gps` (GPS-gap warning) · `MD-Main` |
| SH-03 Start bubbles | today planned; first use (Create template replaces today's bubble); a workout in   progress (one bubble only: "Continue Push A · 0:42"; two workouts never run at once); Reduce   Motion (bubbles appear without moving); today is a rest or run day (open point O-08) | `SH-03-today` (today planned), `SH-03-first` (first use), `SH-03-inprog` (workout in progress), `SH-03-rest` (rest day), `SH-03-run` (run day) · `MD-SH-03-today` |
| SH-04 Start › From template | 4 templates; many templates (scrolling); none (only Create template) | `SH-04-four` (four templates), `SH-04-many` (many templates), `SH-04-none` (no templates) · `MD-SH-04-four` |
| SH-05 Food tab | v1 placeholder only. The later screens are EL-17–EL-19 | `SH-05` (Food (v1)) · `MD-SH-05` |
| SH-06 Onboarding | each page; skipped (lands on Home, first-use states); finished. Page count and split   are open (O-04) | `SH-06-welcome` (welcome), `SH-06-units` (units), `SH-06-body` (body data) · `MD-SH-06-welcome` |
| SH-07 First-use hints in context | shown once, dismissed. Which hints exist is open (O-04) | `SH-07-check` (hint on ✓), `SH-07-dock` (hint on the dock), `SH-07-swipe` (hint on swipe), `SH-07-start` (hint on Start) · `MD-SH-07-check` |
| SH-08 Permission prompts | before asking; system dialog; granted; denied | `SH-08-loc-pre` (location, before asking), `SH-08-loc-sys` (location, system dialog), `SH-08-loc-denied` (location denied), `SH-08-notif-pre` (notifications, before asking), `SH-08-notif-sys` (notifications, system dialog), `SH-08-notif-denied` (notifications denied), `SH-08-health-sys` (Health, system sheet) · `MD-SH-08-loc-pre` |
| WO-01 Active workout, empty | quick picks from marks; quick picks from history; no marks and no history (no quick   picks); after the first quick pick (becomes WO-02 with that exercise in focus) | `WO-01-marks` (quick picks from marks), `WO-01-recent` (quick picks from history), `WO-01-none` (no quick picks), `WO-01-picked` (after the first quick pick), `WO-01-logged` (the empty workout, every set logged) · `MD-WO-01-marks` |
| WO-02 Active workout, exercise in focus | 1. Started from a template, no sets logged ("0:12", "0 of 15 sets", focus on the warm-up).   2. Sets logged, focus mid-exercise (canvas D-pass).   3. Resting: WO-06 floats over the list after Log; the rest line after the logged set is teal.   4. Exercise done, focus jumps on: it folds into the overview with "4/4 ✓", the next opens (D-next).   5. A superset in focus; its rest starts only once every set of the round is logged, in any order.   6. Dock open (WO-04); keypad open (WO-05).   7. Menus open: workout ⋯ (WO-08), exercise ⋯ (WO-09), badge (WO-14).   8. A row mid-swipe showing Delete.   9. Warm-up, failure and drop set in focus, each with its kind word and colour.   10. Bodyweight (Tricep Dips "+10 × 10"), time (Plank "0:45") and distance rows.   11. A logged set tapped: dock with "Save change".   12. A record just set: star on the row.   13. All sets logged: Finish filled.   14. An exercise skipped.   15. A new exercise with no history: Previous "–", empty fields, ✓ waits for a value.   16. Restored after the app was killed: the same screen, every logged set intact | `WO-02-s1` (s1 started from template), `WO-02-benA` (Ben A started from template), `WO-02-pulla` (Pull A started from template), `WO-02-s2` (s2 sets logged), `WO-02-rir` (s2 RIR 2 picked), `WO-02-s3` (s3 resting), `WO-02-s4` (s4 exercise done, focus jumps on), `WO-02-s5` (s5 superset in focus), `WO-02-s5b` (s5 half a round logged), `WO-02-s5c` (s5 next round, A 2 in focus), `WO-02-abc` (a giant set, A B C), `WO-02-s9f` (s9 failure set in focus), `WO-02-s9d` (s9 drop set in focus), `WO-02-s10bw` (s10 bodyweight rows), `WO-02-s10td` (s10 time and distance rows), `WO-02-s12` (s12 record just set), `WO-02-s13` (s13 all sets logged), `WO-02-s14` (s14 an exercise skipped), `WO-02-s15` (s15 new exercise, no history), `WO-02-s16` (s16 restored after a crash), `WO-02-note` (today's note open), `WO-02-removed` (an exercise removed), `WO-02-swap` (Bench swapped for Chest Press Machine) · `MD-WO-02-s1` |
| WO-03 Active workout, scrolled | strip shown; strip during rest (WO-06 still floats); scrolled to the end of the list | `WO-03-strip` (scrolled, strip shown), `WO-03-rest` (strip during rest), `WO-03-end` (scrolled to the end) · `MD-WO-03-strip` |
| WO-04 Logging dock | a set not logged (Log); a logged set ("Save change"); opened from a rest line (rest   field focused, "Save" saves without logging); RIR 4+ tapped (a row 4 · 5 · 6 · 7 · 8 · 9 · 10   opens, the segment then shows the value); keypad open (WO-05: fields drop their − and +, the rest   card hides); a heavy bar ("4 × 25 + 1,25", wraps, shrinks); lb exercise | `WO-04-new` (dock, set not logged), `WO-04-logged` (dock, logged set), `WO-04-rest` (dock from a rest line), `WO-04-rir4` (dock, RIR 4+ open), `WO-04-heavy` (dock, a heavy bar), `WO-04-lb` (dock, an lb exercise) · `MD-WO-04-new` |
| WO-05 Keypad | weight field; reps field; rest field ("Log ›"); Plates: digits swap for plate keys   25 · 20 · 15 · 10 · 5 · 2,5 · 1,25, Empty and ⌫, plates drawn as keyed, heaviest innermost; a field opened fresh is replaced by the first key | `WO-05-weight` (keypad, weight), `WO-05-reps` (keypad, reps), `WO-05-rest` (keypad, rest), `WO-05-plates` (keypad, plates) · `MD-WO-05-weight` |
| WO-06 Rest timer card (T5) | counting after a logged set; a superset round's rest; a free rest (from WO-07); last   seconds; ended (buzz, card leaves); hidden while the keypad is open; Reduce Motion (only the digits   change) | `WO-06-round` (a superset round's rest), `WO-06-free` (a free rest), `WO-06-last` (last seconds), `WO-06-ended` (ended) · `MD-WO-06-round` |
| WO-07 Free rest menu | menu open; custom-time prompt; a rest already running (the choice replaces it) | `WO-07-menu` (free rest menu), `WO-07-custom` (custom time), `WO-07-replace` (menu while resting) · `MD-WO-07-menu` |
| WO-08 Workout menu | menu open; rename prompt; note prompt (empty, filled); start-time picker | `WO-08-menu` (workout menu), `WO-08-rename` (rename), `WO-08-note-empty` (note, empty), `WO-08-note` (note, filled), `WO-08-time` (start time) · `MD-WO-08-menu` |
| WO-09 Exercise menu | menu open; rest submenu open; inside a superset; a non-weight exercise (no settings row) | `WO-09-menu` (exercise menu), `WO-09-rest` (rest submenu), `WO-09-superset` (inside a superset), `WO-09-noweight` (a non-weight exercise), `WO-09-sticky` (sticky note prompt) · `MD-WO-09-menu` |
| WO-10 Create superset | list; one exercise left (row hidden from WO-09 when only one exercise exists) | `WO-10` (create superset), `WO-10-one` (one exercise left) · `MD-WO-10` |
| WO-11 Order and skip | default; one skipped; a superset (moves as a group) | `WO-11` (order and skip), `WO-11-skip` (one skipped) · `MD-WO-11` |
| WO-12 Exercise settings (in the workout) | barbell exercise; dumbbell (no bar); lb | `WO-12-bb` (settings, barbell), `WO-12-db` (settings, dumbbells in lb) · `MD-WO-12-bb` |
| WO-13 Set type menu | working set selected; warm-up selected; on a logged set (the set stays put) | `WO-13-menu` (set type menu), `WO-13-added` (a set added), `WO-13-moved` (a warm-up moved up), `WO-13-logged` (on a logged set), `WO-13-warmup` (warm-up selected) · `MD-WO-13-menu` |
| WO-14 Swipe to delete a set | closed; revealed; full swipe deleting | `WO-14-revealed` (swipe, Delete revealed), `WO-14-full` (full swipe deleting) · `MD-WO-14-revealed` |
| WO-15 Finish sheet | one unlogged set (singular); several; all logged (no sheet) | `WO-15-one` (one set not logged), `WO-15-several` (several sets not logged) · `MD-WO-15-one` |
| WO-16 Discard workout alert | from Discard workout; from Finish on a workout with no sets at all | `WO-16-discard` (discard alert), `WO-16-empty` (finish with no sets) · `MD-WO-16-discard` |
| WO-17 Minimised workout bar | no rest; resting; after the app was killed (a workout left open is offered here); Reduce Motion (fades instead of rising) | `WO-17-plan` (minimised, no rest), `WO-17-rest` (minimised, resting), `WO-17-killed` (after the app was killed) · `MD-WO-17-plan` |
| WO-18 Rest notification (locked phone) | lock screen; banner while another app is open | `WO-18-lock` (rest notification, locked), `WO-18-banner` (banner over another app) · `MD-WO-18-lock` |
| WO-19 Live Activity: lock screen | counting; ended; workout open without rest (if the spike allows; else the activity   ends with the rest); not available with a free Apple ID (the notification stands alone) | `WO-19-counting` (Live Activity, counting), `WO-19-ended` (Live Activity, ended), `WO-19-open` (Live Activity, no rest) · `MD-WO-19-counting` |
| WO-20 Live Activity: Dynamic Island | compact; minimal; expanded; ended | `WO-20-compact` (Dynamic Island, compact), `WO-20-minimal` (Dynamic Island, minimal), `WO-20-expanded` (Dynamic Island, expanded), `WO-20-ended` (Dynamic Island, ended) · `MD-WO-20-compact` |
| WO-21 Workout summary | with records; no records ("–"); RIR on some sets ("13 of 14 sets"); no RIR ("–"); an exercise expanded; not from a template (no goal under Avg RIR); an empty-template workout   offering **Save as template** (WO-23); Done → WO-22 when the workout differs from its template; Done returns to where the workout started | `WO-21-records` (summary with records), `WO-21-expanded` (an exercise expanded), `WO-21-plain` (no records, no RIR, no template), `WO-21-empty` (empty workout, Save as template) · `MD-WO-21-records` |
| WO-22 Update template alert | one change; several | `WO-22-one` (update template, one change), `WO-22-several` (update template, several) · `MD-WO-22-one` |
| WO-23 Save as template | from the summary of an empty workout; from session detail's edit mode | `WO-23-summary` (save as template), `WO-23-edit` (save from editing) · `MD-WO-23-summary` |
| WO-24 Session detail | strength workout; a run opens RU-05 instead; with records; with a note | `WO-24` (session detail), `WO-24-expanded` (sets shown) · `MD-WO-24` |
| WO-25 Session detail, editing | editing; delete alert; unsaved changes on Cancel | `WO-25-edit` (editing), `WO-25-delete` (delete alert), `WO-25-unsaved` (unsaved changes), `WO-25-dock` (a past set in the dock) · `MD-WO-25-edit` |
| WO-26 History | All; Strength; Running; first use ("No workouts yet" with Start a workout); a week   with an extra workout; long list | `WO-26-all` (History, All), `WO-26-strength` (History, Strength), `WO-26-running` (History, Running), `WO-26-empty` (History, first use) · `MD-WO-26-all` |
| PL-01 Plan tab | this week; another week (Today shown); deload recommended (sample: "Week 9 of 12"); a deload week; a calibration week; Short week on; a missed day and an extra; today done; today a   rest day; today a run day; first use (no programme: "No programme" with Create programme, the   week still on calendar weeks, the day card offers an empty workout; no templates: "No templates   yet" with Create template and Start empty workout) | `PL-01-week` (this week), `PL-01-next` (another week), `PL-01-deloadrec` (deload recommended), `PL-01-deload` (a deload week), `PL-01-calib` (a calibration week), `PL-01-short` (Short week on), `PL-01-missed` (a missed day and an extra), `PL-01-done` (today done), `PL-01-rest` (today a rest day), `PL-01-run` (today a run day), `PL-01-noprog` (no programme), `PL-01-notmpl` (no templates), `PL-01-saved` (a template just saved), `PL-01-axlar` (a template created) · `MD-PL-01-week` |
| PL-02 Plan menus | each menu open; Short week on (its row checked); Move to list; a moved session on its   new day | `PL-02-plan` (Plan menu), `PL-02-short` (Short week on), `PL-02-type` (week type), `PL-02-day` (day card menu), `PL-02-move` (move to), `PL-02-moved` (a moved session), `PL-02-templates` (templates menu) · `MD-PL-02-plan` |
| PL-03 Template card menu | menu open; delete alert; share sheet; archived (card leaves the row) | `PL-03-menu` (template card menu), `PL-03-delete` (delete alert), `PL-03-share` (share sheet), `PL-03-archived` (archived, the card leaves the row) · `MD-PL-03-menu` |
| PL-04 Template preview | a template with a superset; never done; opened from a day in the week card (PL-01) or   from a card | `PL-04-pusha` (template preview), `PL-04-never` (never done), `PL-04-pulla` (Pull A preview) · `MD-PL-04-pusha` |
| PL-05 Template editor | a new empty template; editing Push A; keypad open (PL-06); Cancel with changes   ("Discard changes?"); delete alert; a superset | `PL-05-new` (new template), `PL-05-edit` (editing Push A), `PL-05-discard` (discard changes), `PL-05-delete` (delete alert), `PL-05-added` (new template, exercises added) · `MD-PL-05-new` |
| PL-06 Template keypad | reps field; RIR field; after Same for the rest | `PL-06-reps` (range keypad, reps), `PL-06-rir` (range keypad, RIR), `PL-06-same` (after Same for the rest) · `MD-PL-06-reps` |
| PL-07 Archived templates | list; empty | `PL-07-list` (archived templates), `PL-07-empty` (no archived templates) · `MD-PL-07-list` |
| PL-08 Programme · Weeks | this week open; another week open; the standard week open; a day changed; week 8 made a   deload; a menu open | `PL-08-week6` (programme, this week open), `PL-08-std` (standard week open), `PL-08-week8` (week 8 made a deload), `PL-08-daymenu` (a day's menu), `PL-08-changed` (a day changed), `PL-08-runmenu` (Run… submenu open), `PL-08-runtarget` (run target), `PL-08-tempo` (a run day set) · `MD-PL-08-week6` |
| PL-09 Programme · Weights | filled; a new programme ("Log a workout to see your weights") | `PL-09` (programme weights), `PL-09-new` (new programme) · `MD-PL-09` |
| PL-10 Programme · Log | notes; empty; writing a note | `PL-10` (programme log), `PL-10-empty` (no notes), `PL-10-write` (writing a note) · `MD-PL-10` |
| PL-11 Programme · Settings | fixed length; no fixed length; date picker open; archive alert | `PL-11` (programme settings), `PL-11-nofixed` (no fixed length), `PL-11-date` (date picker), `PL-11-archive` (archive alert) · `MD-PL-11` |
| PL-12 Create programme | empty; filled; no templates yet (the weekday menus offer Run and Rest only). Form is   open (O-11) | `PL-12-empty` (create programme, empty), `PL-12-filled` (create programme, filled), `PL-12-notmpl` (no templates yet) · `MD-PL-12-empty` |
| EX-01 Exercise library | default; searching; a chip on; a filter on; no match ("No exercises match"); Mobility   chip (stretches); sort menu open; no illustration (placeholder); no history (no last performance) | `EX-01` (library), `EX-01-search` (searching), `EX-01-chip` (a chip on), `EX-01-filter` (a filter on), `EX-01-nomatch` (no match), `EX-01-mobility` (Mobility), `EX-01-sort` (sort menu) · `MD-EX-01` |
| EX-02 Library filter sheet | nothing ticked; Dumbbells ticked; No ticked | `EX-02-none` (filter, nothing ticked), `EX-02-db` (Dumbbells ticked), `EX-02-no` (No ticked) · `MD-EX-02-none` |
| EX-03 Exercise picker (Add exercises) | none ticked; one ticked (Superset needs two); three ticked; searching while picking | `EX-03-none` (picker, none ticked), `EX-03-one` (one ticked), `EX-03-two` (two ticked), `EX-03-search` (searching while picking), `EX-03-tmpl` (from the template editor), `EX-03-created` (a new exercise ticked) · `MD-EX-03-none` |
| EX-04 Swap exercise | with alternatives; none | `EX-04` (swap, with alternatives), `EX-04-none` (swap, none) · `MD-EX-04` |
| EX-05 Exercise detail · About | as a screen from the library; as a sheet from the workout (O-02); no illustration yet   (text first); mark menu open; each mark | `EX-05-incline` (About, Incline Press DB), `EX-05` (About), `EX-05-sheet` (as a sheet from the workout), `EX-05-noillo` (no illustration, Swap mark), `EX-05-mark` (mark menu open) · `MD-EX-05` |
| EX-06 Exercise detail · History | filled; empty ("Log a workout to see your history") | `EX-06-incline` (History, Incline Press DB), `EX-06` (History), `EX-06-empty` (History, empty) · `MD-EX-06` |
| EX-07 Exercise detail · Chart | each metric; a point selected; empty | `EX-07-incline` (Chart, Incline Press DB), `EX-07-inclinept` (a point selected, Incline Press DB), `EX-07` (Chart), `EX-07-point` (a point selected), `EX-07-empty` (Chart, empty) · `MD-EX-07` |
| EX-08 Exercise detail · Records | filled; empty; a bodyweight or timed exercise's records | `EX-08-incline` (Records, Incline Press DB), `EX-08` (Records), `EX-08-empty` (Records, empty), `EX-08-bw` (bodyweight records) · `MD-EX-08` |
| EX-09 Create exercise | empty; a menu open; secondary muscles list; filled (Save on); from the picker's New | `EX-09-empty` (create exercise, empty), `EX-09-menu` (a menu open), `EX-09-muscles` (secondary muscles), `EX-09-filled` (filled), `EX-09-picker` (from the picker's New, filled) · `MD-EX-09-empty` |
| EX-10 Edit exercise | a used exercise; an unused one (Delete shown); archived | `EX-10-used` (edit a used exercise), `EX-10-unused` (edit an unused exercise), `EX-10-archived` (archived) · `MD-EX-10-used` |
| EX-11 Merge data | list; alert; done | `EX-11-list` (merge, choose), `EX-11-alert` (merge alert), `EX-11-done` (done) · `MD-EX-11-list` |
| RU-01 Run start | searching; ready; weak signal; location not allowed (SH-08); from the Start bubble or   from Plan's run day | `RU-01-search` (looking for GPS), `RU-01-ready` (GPS ready), `RU-01-weak` (weak signal) · `MD-RU-01-search` |
| RU-02 Live run | running; a lap just taken; GPS lost (the route gets a gap); screen locked; map   enlarged or hidden (O-06) | `RU-02` (running, distance), `RU-02-time` (running, time), `RU-02-pace` (running, current pace), `RU-02-avg` (running, average pace), `RU-02-lap` (a lap just taken), `RU-02-gps` (GPS lost), `RU-02-nomap` (map swiped away), `RU-02-locked` (screen locked), `RU-02-mini` (run minimised), `RU-02-island` (Dynamic Island, a run) · `MD-RU-02` |
| RU-03 Live run, paused | paused | `RU-03` (paused) · `MD-RU-03` |
| RU-04 Finish run | shown; a very short run (open whether to warn) | `RU-04` (finish run), `RU-04-short` (a very short run) · `MD-RU-04` |
| RU-05 Run summary and run detail | just finished (Done); from History (back, Edit); no heart rate (HR tiles and section   hidden); a run with a GPS gap; a record set; a run synced from a watch (later, #27) | `RU-05-done` (just finished), `RU-05-history` (from History), `RU-05-nohr` (no heart rate), `RU-05-gap` (a GPS gap), `RU-05-watch` (synced from a watch (later)) · `MD-RU-05-done` |
| RU-06 Run summary, sections | each section; no heart rate; a split selected | `RU-06-splits` (splits), `RU-06-charts` (elevation, pace, heart rate), `RU-06-sel` (a split selected) · `MD-RU-06-splits` |
| RU-07 Map, full screen | each map type; 3D; sources sheet open | `RU-07-std` (map, standard), `RU-07-sat` (satellite), `RU-07-3d` (3D, hybrid), `RU-07-sources` (map sources) · `MD-RU-07-std` |
| RU-08 Flyover replay | playing; paused; scrubbing; Reduce Motion (O-06) | `RU-08-play` (flyover playing), `RU-08-paused` (paused, scrubbing) · `MD-RU-08-play` |
| RU-09 Edit run | editing; delete alert | `RU-09` (edit run), `RU-09-delete` (delete alert) · `MD-RU-09` |
| EL-01 You | filled; first use (no activities: "No workouts yet" with Start a workout) | `EL-01` (You), `EL-01-first` (first use) · `MD-EL-01` |
| EL-02 Progress · Strength | 8 wk, 6 mo, 1 yr; a bar selected; each comparison; first use ("No workouts yet" with   Start a workout); fewer than four lifts picked | `EL-02-8wk` (Progress, 8 weeks), `EL-02-6mo` (6 months), `EL-02-1yr` (1 year), `EL-02-next` (8 weeks, next comparison), `EL-02-bar` (a bar selected), `EL-02-6mobar` (6 months, a bar selected), `EL-02-first` (first use), `EL-02-fewer` (fewer than four lifts) · `MD-EL-02-8wk` |
| EL-03 Progress · Running | filled; no runs yet; a bar selected | `EL-03` (Progress, Running), `EL-03-empty` (no runs yet), `EL-03-bar` (a bar selected) · `MD-EL-03` |
| EL-04 Choose main lifts | three picked; five picked (others disabled). Where it opens from is open (O-14) | `EL-04-three` (main lifts, three), `EL-04-five` (main lifts, five) · `MD-EL-04-three` |
| EL-05 Profile | filled; empty (first use, onboarding skipped). Fields beyond "body data and goals" are   open (O-15) | `EL-05` (Profile), `EL-05-empty` (Profile, empty) · `MD-EL-05` |
| EL-06 Settings | default; a menu open; lb chosen | `EL-06` (Settings), `EL-06-menu` (a menu open), `EL-06-lb` (lb chosen) · `MD-EL-06` |
| EL-07 Plates and bars | kg; lb; editing a bar's weight | `EL-07-kg` (plates and bars, kg), `EL-07-lb` (plates in lb), `EL-07-edit` (editing a bar) · `MD-EL-07-kg` |
| EL-08 Devices and apps | nothing linked; Apple Health linked; an error; linking in progress | `EL-08-none` (nothing linked), `EL-08` (Health linked, an error), `EL-08-linking` (linking) · `MD-EL-08-none` |
| EL-09 Apple Health link | before linking; linked; not available with a free Apple ID (a spike decides) | `EL-09-before` (Apple Health, before), `EL-09-linked` (Apple Health, linked), `EL-09-na` (not available) · `MD-EL-09-before` |
| EL-10 Gear | filled; a pair past its warning distance; none yet; retired list | `EL-10` (gear), `EL-10-none` (no gear yet), `EL-10-retired` (retired) · `MD-EL-10` |
| EL-11 Gear detail and edit | new; existing; retired (Unretire) | `EL-11-new` (add gear), `EL-11` (gear detail), `EL-11-retired` (retired gear) · `MD-EL-11-new` |
| EL-12 Choose gear | default chosen; another chosen | `EL-12` (choose shoes), `EL-12-other` (another chosen) · `MD-EL-12` |
| EL-13 Export | default; custom period; nothing in the period; exporting; share sheet open | `EL-13` (export), `EL-13-custom` (custom period), `EL-13-nothing` (nothing in the period), `EL-13-exporting` (exporting), `EL-13-share` (share sheet) · `MD-EL-13` |
| EL-14 About Strive | shown | `EL-14` (About Strive) · `MD-EL-14` |
| EL-15 Import (later) | v1 disabled row; choose source; preview; importing; done | `EL-15-source` (import, source (later)), `EL-15-preview` (import, preview (later)), `EL-15-importing` (importing (later)), `EL-15-done` (import done (later)) · `MD-EL-15-source` |
| EL-16 Account and sync (later) | signed out; synced; syncing; Supabase project paused | `EL-16-out` (sync, signed out (later)), `EL-16-syncing` (syncing (later)), `EL-16-synced` (synced (later)), `EL-16-paused` (project paused (later)) · `MD-EL-16-out` |
| EL-17 Food today (later) | no target; on track; over | `EL-17-notarget` (food, no target (later)), `EL-17-ontrack` (food, on track (later)), `EL-17-over` (food, over (later)) · `MD-EL-17-notarget` |
| EL-18 Log food (later) | search; recent; portion picker | `EL-18-search` (log food, search (later)), `EL-18-recent` (log food, recent (later)), `EL-18-portion` (portion (later)) · `MD-EL-18-search` |
| EL-19 Nutrition targets (later) | unset; set | `EL-19-unset` (targets unset (later)), `EL-19-set` (targets set (later)) · `MD-EL-19-unset` |

States drawn elsewhere: each tab selected (Main, SH-05, PL-01-week, EL-01); a pushed screen (any `.nav` board);
the keypad open (WO-05-*, PL-06-*); a field opened fresh (WO-05-reps, the selected value); all sets logged, no finish
sheet (WO-02-s13 → WO-21-records); Done → WO-22 or back (WO-22-*, links); a run from History (RU-05-history); with
records and a note (WO-24); a week with an extra workout (WO-26-strength, "Armar · Extra"); a superset in the editor
and in Order and skip (PL-05-edit, WO-11); archived (PL-07-list); no illustration and no history (EX-01's rows); each
mark (EX-05-mark lists them); scrubbing (RU-08-paused); location not allowed (SH-08-loc-denied); a pair past its
warning (EL-10); the v1 disabled Import row (EL-01); a free Apple ID (WO-18-banner stands alone); every Reduce Motion
state (the Motion page and the row stickies).

## Flows wired
`design-1/flows.json` writes each of the inventory's 64 flows as board paths, tap by tap, with its alternatives as extra
paths. `python3 design-1/tools/flows.py --check` walks every step and requires an `<a href>` from one board to the next.
**All 64 play.** The old `coverage.py --flows` only asked whether any board of a screen linked to any board of the next,
which is how 16 broken flows passed. Limits, said plainly:
- **Device events and gestures are not taps** (`~BOARD` in flows.json): F-13 (lock the phone, the rest ends), F-24 (the
  app killed), F-21 (the scroll: WO-02-s2 scrolls in place and folds the focus card into the strip; WO-03-strip, -rest and
  -end draw the scrolled states, and their strip taps back to WO-02 are wired).
- **F-44:** back from WO-24 goes to History, not to the chart it came from: a board has one back target.
- **F-51:** the sample gives the 8 Nov long run Nike Pegasus 41, while the flow picks Pegasus 41. The prototype follows the
  sample: EL-12 has Pegasus 41 chosen and the flow picks Novablast 4 (EL-12-other); after Done, RU-09 still shows Pegasus.
- **Where a flow stops by design:** Ben A and Pull A start (WO-02-benA, -pulla) but log nothing further; Push B and the
  other templates in SH-04, the Ben A and Push B cards in Plan, EX-01-chip's rows, Copenhagen Plank's time cell (no time
  keypad board) and EX-03-tmpl's Superset are not links.
- **One tap stands for four:** WO-01-picked's ✓ leads to WO-01-logged, the empty workout with its four sets logged.
- Flows that begin in another app state (first use, rest day, run day, the Later screens) start on that board: press Play there.

## Open-point choices (design 1's)

- O-02: an "Exercise details" row (ⓘ) after Exercise settings in the exercise ⋯ menu opens EX-05 as a sheet — two taps.
- O-07: the run's Live Activity in the Dynamic Island, expanded: distance, time, pace, Pause and Lap (RU-02-island).
- O-12: the day's Run… submenu lists Easy, Long, Tempo, Intervals; picking one opens a prompt with Distance | Time and a
  value (PL-08-runmenu, -runtarget); the day then reads "Tempo · 6 km" (PL-08-tempo).
- O-21: a giant set is one superset lettered A, B, C under one "Superset" label (WO-02-abc).
- O-11: create a programme in a full-height form sheet (PL-12).
- O-13: Gear in You (EL-10); Settings holds devices and apps (EL-08).
- O-14: an Edit button on Progress's "Main lifts" heading opens EL-04 — one tap.
- O-19: a drawn muscle glyph on a surface-container-high tile (EX-05-noillo, EX-10).
- O-20: drag down on the title bar minimises (a third, or a flick); swipe-delete thresholds 88 pt / half the row (Motion page).
- O-04 assumption: the long press on Start begins today's workout at once.

## Polishes to built screens (each becomes a code change)

- **Tab bar** (SH-01): icons and labels on a glass capsule with Start as a round action (built: text tabs, a plain circle).
- **Start** (SH-03): the 14c bubbles (built: a Material bottom sheet).
- **Card radii** unified on `shape-large-increased` 20 (built: focus card 22, overview 18, empty workout 24); set badge 8 → 10.
- **The next row's tint** as a token (`state-next-row`).
- **Minimised bar** (WO-17): the rest as a small amber ring and the time on the right (built: in the subline).
- **Menus** (WO-08, WO-09, WO-13): iOS glass pull-downs with blur (built: Material DropdownMenu on surface-container-high).
- **Restored workout** (WO-02-s16): a toast "Restored. Every logged set is here."
- **Swipe to delete** (WO-14): `error-container` / `on-error-container` as Parts.kt does; dark-HC `error-container`
  #ff6961 with black (built #ffd1cc turned the revealed action into a near-white slab). Destructive text stays light:
  dark-HC `error` #ffbdb6, 7:1 on every container (built #ffd1cc).
- **Segmented view switches** (WO-26, EL-02, EL-03, EX-05 tabs): in dark modes the chosen segment is outlined, since
  `surface-bright` equals the track there (outline-variant 1 px in dark, outline 1.5 px in Increase Contrast).
- **Exercise menu** (WO-09): an "Exercise details" row after Exercise settings (built: no such row).
- **Remove exercise** (WO-02-removed): an Undo toast (built: removed at once, no undo).
- **From the diff against `origin/dev`** (2026-11-10; exact strings and Kotlin lines in `design-1/polish-diff.json`):
  - Copy kept: "Search 553 exercises" (built "Search"); "Swap Bench Press BB" (built "Swap exercise"); EX-05's section
    "Sticky note" (built "Note"); "Log warm-up" and "Log drop set" (built "Log set W", "Log set D"); "rest 1:30 after the
    round" (built "rest 1:30 after"; the sample's words); WO-15's singular "Log it as it is and save", "Save without it"
    (built has only the plural; the inventory asks for it); the dock's "Bench Press BB · Rest after set 2" from a rest line.
  - EX-01: the back label names the parent; Sort (⋯) and New (+); the filter as an icon; rows with a thumbnail and the last
    set; no match adds a caption and "Create “zercher”". EX-02: a Clear · Filter · Done header, a footnote on No.
    EX-03: New in the title bar. EX-04: Alternatives are the Swap alternative, then the same pattern, with a caption.
  - EX-05: the mark row in a grouped list with its caption as a footnote; mark menu items captioned and checked; sections
    reordered (Muscles and equipment as rows, Sticky note, Settings with a Unit row); back "Library" and ⋯; the
    no-illustration line and an Alternative row for a Swap mark. EX-06–EX-08: filled History, Chart (3 mo · 1 yr · All,
    a metric picker) and Records with their own empty lines, where built shows "No workouts yet" on all three.
  - WO-01: the empty workout's bar shows the time only (built "0:12 · 0/0 sets"). WO-02 and WO-03: "last set" on an
    exercise's last set ("hold · last set" while resting) where built says "rest 2:00 after"; "Plan" and "Previous" note
    rows under the pinned note. WO-04 and WO-05: the plate readout and the Plates key (plates are not built).
  - WO-06 and WO-07: a free rest titled "Free rest", the menu's title row and "Replaces the rest running"; the custom time
    hint and Start. WO-08: the note's placeholder and hint; the start time as a bottom sheet with a wheel (built: a centred
    Dialog with TimeInput). WO-10: an alert-style dialog with "The one you pick moves next to Bench Press BB.". WO-11: a
    bottom sheet, a superset as one row, ↑ and ↓ disabled at the ends (built: a centred Dialog, a flat list). WO-12: a
    bottom sheet titled with the exercise, Step and Bar as pull-downs, a kg/lb Unit control (built: a centred Dialog,
    segmented rows). WO-13: the order Warm-up, Working set, Drop set, To failure (the inventory's; built puts Working set
    first), the set's own number, a caption on a logged set.
  - WO-21: Save as template on a plain workout; rows "Best 82.5 × 8 · ↑ +2.5 kg vs last"; warm-ups as W, working sets from
    1; a Week row. WO-24: back "All activities" and Edit. WO-26: back "You"; All | Strength | Running; the week header with
    a right-aligned summary; a rebuilt row (day over weekday, kind icon, volume, chevron); a first-use line.
  - Not diffed: Home, Plan and You, and colours, spacing and radii anywhere.

## What is missing or not verified
- **The canvas was never seen rendered.** No Chrome extension was connected in any session. Every board was checked
  locally with headless Chrome against the same stylesheet (`tools/preview_grid.py`), in dark-HC; the fix round's 31 new
  boards and 8 redraws also in light (their subagents) and in contact sheets (mine). Unverified on the canvas itself: the
  `ds/strive1/` stylesheet links, the mode Tweak, `<dc-import>` with hyphenated names in the 86 mode sheets, Play mode.
- Light and light-HC were spot-checked, not checked board by board.
- The flow limits under Flows wired.
- **Invented sample data**, since the sample has none: Ben A's and Pull A's previous sets (WO-02-benA, -pulla), Incline
  Press DB's history before 2 Nov (EX-06/07/08-incline), the 12 sets merged from "Bench Press" (EX-11-done), the Axlar
  and "Workout 9 Nov" templates.
- Home, Plan and You were not diffed against the Kotlin (polish-diff.json's scope).

## Progress log (2026-11-10)

- First resume: lint found every file whole; fixed 3 links to `None.dc.html`; checked Shell, Workout · logging,
  Workout · around it in dark-HC and fixed the Start panel blur, menu blur, WO-04-heavy/-lb's focus card, WO-06-ended,
  dark-HC swipe-delete. System v5 (component cards), canvas v6–v8: every SH, WO, PL, EX, RU, MO board.
- Second resume (this one): checked Workout · after, Plan part 2, Exercises, Running, You and Later in dark-HC.
  Fixed: dark segmented controls showed no selection (system); `.field.focus` inherited the focus card's layout, so
  focused text fields drew as tall centred boxes (EL-18-search, PL-05-new, PL-12, WO-23 — system); icon sizes s24, s26,
  s34 were undefined; EL-13-nothing and EL-13-exporting hid their state under the tab bar (now scrolled to it);
  "until Carl's arrives" → "until one arrives" (EX-05-noillo, EX-10); 10 workout boards linked `WO-02-s2` without
  `.dc.html`, so their close taps went nowhere (lint now catches it). Added WO-10-one (one exercise left) and EX-03-tmpl
  (the picker from the template editor). System v6; canvas v9–v11: every EL, LT and MD board.

- Fix round (after the review): O-25 counts, dark-HC `error` #ffbdb6, island tokens, spring values (system v7); exercise
  menu order; contradictions; relinks; four subagents drew 31 boards and redrew 8 (workout, plan, exercises/You) and
  diffed copy and structure against `origin/dev`; 12 rewordings reverted; `flows.json` and `tools/flows.py` check every
  flow tap by tap: 64 of 64 play. Lint: 423 boards, 7 with the known findings (WO-02-s2's scroll holes; six map and island
  boards with little text). Canvas v12.

result: design-1/fixed — https://claude.ai/artifact/D938qJPYBGyUgVKonitcsE — 10 of 10 findings fixed, 64 flows play
