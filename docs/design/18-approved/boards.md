# Strive 1+2 — the board registry

Design 1's registry, with EL-02-cmp added. Strive 1+2's changes to the boards below are listed in `design-12.md`;
MO-*, MD-* and vs-* boards are placed by `tools/prep12.py`, `tools/build_modes.py` and `tools/vs.py`, not listed here.

Every board on the canvas, in the inventory's order. **The canvas index is generated from this file**, so a board exists exactly
when it has a row here. Columns: `file` (without `.dc.html`) · `title` (the frame's label; the mode is the board's default,
dark-HC, and every board switches mode in Tweaks) · `shows` (what the board must draw, beyond the inventory's text for that state) ·
`links` (element → target file; every target is a row in this registry).

Shared link sets:
- **TAB(x)**: the tab bar with x current. Home → `Main`, Food → `SH-05`, Start → `SH-03-today`, Plan → `PL-01-week`, You → `EL-01`.
- **F1**: the workout title bar. ⌄ → `SH-02-inprog` (`WO-17-rest` while a rest runs), ⏱ → `WO-07-menu`, Finish → `WO-15-several` (filled Finish → `WO-21-records`),
  ⋯ by the workout's name → `WO-08-menu`.
- **CLOSE(x)**: grabber, a tap outside, Cancel or × → x.

## Shell · page `shell`

| file | title | shows | links |
|---|---|---|---|
| SH-01-rt | SH-01 · Reduce Transparency · dark-HC | Main with the workout bar (WO-17) above the tab bar; every glass solid (`.no-glass` on .screen). | TAB(Home); bar → WO-02-s2 |
| Main | SH-02 · strength day · dark-HC | **Built by the lead.** Home on Mon 9 Nov, Push A planned. | TAB(Home); Start Push A → WO-02-s1; Long run row → RU-05-history; Plan → PL-01-week; All activities → WO-26-all |
| SH-02-run | SH-02 · run day · dark-HC | Fri 13 Nov: "Planned today · Easy run · 8 km", the shoes, filled **Start run**; week numbers (3 of 4 workouts, 0 of 22 km). | TAB(Home); Start run → RU-01-ready |
| SH-02-rest | SH-02 · rest day · dark-HC | Wed 11 Nov: "Rest day" card, "Next: Pull A · Thursday · about 35 min" with **Start Pull A anyway** as a tinted button. | TAB(Home); Start Pull A anyway → WO-02-pulla |
| SH-02-done | SH-02 · today done · dark-HC | Push A ✓ done 17:02–18:06, 3 records, **Show workout**. Workouts 1 of 4. | TAB(Home); Show workout → WO-24 |
| SH-02-inprog | SH-02 · workout in progress · dark-HC | Card "In progress · Push A · 24:13 · 1 of 14 sets", filled **Continue Push A**; WO-17 bar above the tab bar. | TAB(Home) except Plan → WO-17-plan; Continue → WO-02-s2; bar → WO-02-s2 |
| SH-02-first | SH-02 · first use · dark-HC | No programme, no templates: card "Welcome, Carl" with **Create template** (filled) and **Start empty workout**; week numbers "No workouts yet". | TAB(Home); Create template → PL-05-new; Start empty workout → WO-01-none |
| SH-02-gps | SH-02 · GPS-gap warning · dark-HC | Main plus a warning card above Latest: "Last run saved without GPS for 0.4 km. The route has a gap." with View run. | TAB(Home); View run → RU-05-gap; Start Push A → WO-02-s1 |
| SH-03-today | SH-03 · today planned · dark-HC | Main dimmed by nothing (screen stays in view), + turned ×, the panel and 4 bubbles in an arc: **Push A** (filled, "today"), Empty workout, From template, Run. | Push A → WO-02-s1; Empty → WO-01-marks; From template → SH-04-four; Run → RU-01-ready; × or outside → Main |
| SH-03-first | SH-03 · first use · dark-HC | Create template in today's place. | Create template → PL-05-new; Empty → WO-01-none; From template → SH-04-none; Run → SH-08-loc-pre; × → SH-02-first |
| SH-03-inprog | SH-03 · workout in progress · dark-HC | One bubble only: "Continue Push A · 24:13". | bubble → WO-02-s2; × → SH-02-inprog |
| SH-03-rest | SH-03 · rest day · dark-HC | Today's bubble reads "Next · Pull A · Thu" (outlined, not filled). | Pull A → WO-02-pulla; others as SH-03-today; × → SH-02-rest |
| SH-03-run | SH-03 · run day · dark-HC | Today's bubble is the run: "Easy run · 8 km" (filled, run icon). | Easy run → RU-01-ready; × → SH-02-run |
| SH-04-four | SH-04 · four templates · dark-HC | Back first, then Push B (Sat), Pull A (Thu), Ben A (Tue), Push A (Mon), last done first, as round bubbles three across. | Back → SH-03-today; Ben A → WO-02-benA; Pull A → WO-02-pulla; Push A → WO-02-s1; Push B not wired; outside → Main |
| SH-04-many | SH-04 · many templates · dark-HC | 10 bubbles scrolling: Push B, Pull A, Ben A, Push A, Armar, Pull B, Ben B, Rörlighet, Core, Push C; the grid fades at the top edge. | Back → SH-03-today; Ben A → WO-02-benA; Pull A → WO-02-pulla; Push A → WO-02-s1; the others not wired |
| SH-04-none | SH-04 · no templates · dark-HC | Back and one bubble: Create template. | Back → SH-03-first; Create template → PL-05-new |
| SH-05 | SH-05 · Food (v1) · dark-HC | Large title Food; one card "Nutrition comes later", a line on what it will do, and **See the food log (later)**. | TAB(Food); See the food log → EL-17-notarget |
| SH-06-welcome | SH-06 · welcome · dark-HC | Page 1 of 3: "Strive", "Built by Carl for his own training." A drawn composition of the app's parts (a focus card, a week strip, a route line), 3 lines: log a set in one tap · the week in Plan · runs with GPS. Next, Skip. | Next → SH-06-units; Skip → SH-02-first |
| SH-06-units | SH-06 · units · dark-HC | Page 2: Weight kg / lb (segmented, kg on), Distance km / mi (km on). Next, Skip. | Next → SH-06-body; Skip → SH-02-first |
| SH-06-body | SH-06 · body data · dark-HC | Page 3: body weight 82.4 kg, height 183 cm, birth year (empty), "Optional. Bodyweight exercises use your weight." Ends on **Create your first template** and **Start an empty workout**; Skip. | Create → PL-05-new; Empty → WO-01-none; Skip → SH-02-first |
| SH-07-check | SH-07 · hint on ✓ · dark-HC | WO-02-s1 with a hint bubble pointing at the warm-up's ✓: "Tap ✓ to log the set as it stands." Got it. | Got it → WO-02-s1 |
| SH-07-dock | SH-07 · hint on the dock · dark-HC | WO-04-new with a hint pointing at the grabber: "Change the set here. Swipe down or tap outside to close." | Got it → WO-04-new |
| SH-07-swipe | SH-07 · hint on swipe · dark-HC | WO-02-s2's rows with a hint on set 3: "Swipe a set left to delete it." | Got it → WO-02-s2 |
| SH-07-start | SH-07 · hint on Start · dark-HC | Main with a hint over Start: "Hold Start to begin Push A straight away." | Got it → Main; Start Push A → SH-07-check |
| SH-08-loc-pre | SH-08 · location, before asking · dark-HC | RU-01 behind a sheet: location icon, "Strive records your route with GPS", "Only while a run is recording.", Continue (filled), Not now. | Continue → SH-08-loc-sys; Not now → SH-08-loc-denied |
| SH-08-loc-sys | SH-08 · location, system dialog · dark-HC | The iOS dialog over RU-01: Allow "Strive" to use your location? · Allow Once · Allow While Using App · Don't Allow. | Allow While Using → RU-01-search; Don't Allow → SH-08-loc-denied |
| SH-08-loc-denied | SH-08 · location denied · dark-HC | RU-01 with the map greyed: "Location is off for Strive. A run needs it to record distance." Open Settings. | close → Main |
| SH-08-notif-pre | SH-08 · notifications, before asking · dark-HC | WO-02-s3 (resting) with a sheet: "Get a buzz when rest is over, even with the phone locked." Allow notifications, Not now. | Allow → SH-08-notif-sys; Not now → WO-02-s3 |
| SH-08-notif-sys | SH-08 · notifications, system dialog · dark-HC | iOS: "Strive" Would Like to Send You Notifications · Don't Allow · Allow. | Allow → WO-02-s3; Don't Allow → SH-08-notif-denied |
| SH-08-notif-denied | SH-08 · notifications denied · dark-HC | WO-02-s3 with a small line in the rest card's place above it: "Rest alerts are off. Turn on in Settings." | close → WO-02-s3 |
| SH-08-health-sys | SH-08 · Health, system sheet · dark-HC | The iOS Health access sheet over EL-09: Turn On All; Allow "Strive" to write: Workouts; to read: Workouts, Heart Rate; Don't Allow · Allow. | Allow → EL-09-linked; Don't Allow → EL-09-before |

## Workout · logging · page `workout`

| file | title | shows | links |
|---|---|---|---|
| WO-01-marks | WO-01 · quick picks from marks · dark-HC | C-tomt: F1 with "Workout" and ⋯ in the middle, "0:12"; the card with the 72 pt round +, "What are you training today?", "Pick from the library, or tap an exercise below.", **Add exercises**; "Quick picks" · "Marked Yes in your programmes": + Bench Press BB, + Squat BB, + Deadlift BB, + Overhead Press BB, + Pull Up, + Incline Press DB; Discard workout. | F1; + Bench Press BB → WO-01-picked; Add exercises → EX-03-none; Discard → WO-16-discard |
| WO-01-recent | WO-01 · quick picks from history · dark-HC | Same, caption "Recently done": Curl DB, Hammer Curl DB, Tricep Pushdown, Deadlift BB, Lat Pulldown Cable, Seated Row Cable. | as WO-01-marks |
| WO-01-none | WO-01 · no quick picks · dark-HC | Same card, no Quick picks section. | Add exercises → EX-03-none; Finish → WO-16-empty |
| WO-01-picked | WO-01 · after the first quick pick · dark-HC | WO-02 for "Workout" with Bench Press BB in focus from last time (W 40 × 10, 3 × 80 × 8), 0:31, "0 of 3 sets", focus "NOW · BENCH PRESS BB · WARM-UP". | F1; ⋯ → WO-10-one; Log and W ✓ → WO-01-logged; Finish → WO-16-empty; Add exercises → EX-03-two |
| WO-01-logged | WO-01 · the empty workout, every set logged · dark-HC | "Workout" (an empty workout) at 31:40 after F-05: Bench Press BB folded into the overview "4/4 ✓" (W 40 × 10, 80 × 8, 80 × 8, 80 × 7), "3 of 3 sets", **Finish filled**, Add exercises below. The logged sets match WO-21-empty. One tap on WO-01-picked's ✓ stands for logging the four sets. | F1 except: filled Finish → WO-21-empty |
| WO-02-s1 | WO-02 · s1 started from template · dark-HC | 0:12, "0 of 14 sets", focus "NOW · BENCH PRESS BB · WARM-UP" (WARM-UP amber), 40 kg × 10 reps, no "up", Previous 40 × 10, W row next. Template note toast: "Kontrollerad nedfirning, två sekunder." | F1; Log warm-up → WO-02-s3; W ✓ → WO-02-s3; Edit → WO-04-new |
| WO-02-benA | WO-02 · Ben A started from template · dark-HC | State 1 for **Ben A** (F-04): 0:08, "0 of 13 sets", focus "NOW · SQUAT BB · WARM-UP", 60 kg × 10, Previous from Tue 3 Nov; then Romanian Deadlift DB, Leg Press, Standing Calf Raise Machine in the overview, per the sample. | F1; Log warm-up and W ✓ are not wired (no Ben A boards follow) |
| WO-02-pulla | WO-02 · Pull A started from template · dark-HC | State 1 for **Pull A** (F-36): 0:06, "0 of 12 sets", focus "NOW · DEADLIFT BB · WARM-UP", Previous from Thu 5 Nov; Lat Pulldown Cable, Seated Row Cable, Curl DB in the overview, per the sample. | F1; Log not wired |
| WO-02-s2 | WO-02 · s2 sets logged · dark-HC | **Built by the lead** (the reference). | see the file |
| WO-02-rir | WO-02 · s2 RIR 2 picked · dark-HC | WO-02-s2 with RIR 2 chosen on the card (teal segment). | Log set 2 → WO-02-s12; others as WO-02-s2 |
| WO-02-s3 | WO-02 · s3 resting · dark-HC | WO-02-s2's list (W ✓, 1 ✓, focus set 2), the rest line after set 1 teal, WO-06 floating: ring at 1:43 of 2:00. | Skip → WO-02-s2; + / − stay; ✓ set 2 → WO-02-s12 |
| WO-02-s4 | WO-02 · s4 exercise done, focus jumps on · dark-HC | 31:05, "3 of 14 sets". Bench folded into the overview card on top "4/4 ✓" (teal), dots W amber + 3 teal; the superset opens below the focus card; focus "NOW · A · INCLINE PRESS DB · SET 1 OF 3", 26 kg × 9; rest card 1:52 after Bench's set 3. | Bench row expand → WO-02-s2; Skip → WO-02-s5 |
| WO-02-s5 | WO-02 · s5 superset in focus · dark-HC | 38:40, "3 of 14 sets". Superset label "Superset · rest 1:30 after each round", edge line, rows in lifting order A1, B1, rest 1:30, A2, B2, rest, A3, B3; letter tags; focus A1 "rest 1:30 after the round". | ✓ B1 → WO-02-s5b; ✓ A1 → WO-02-s5b |
| WO-02-s5b | WO-02 · s5 half a round logged · dark-HC | B1 logged (10 × 13 @2), A1 still next; no rest yet (the round's rest waits for A1). | ✓ A1 → WO-06-round |
| WO-02-s5c | WO-02 · s5 next round, A 2 in focus · dark-HC | The superset after round 1 and its rest: A1 and B1 logged ✓, focus "NOW · A · INCLINE PRESS DB · SET 2 OF 3", "5 of 14 sets", no rest card. | F1; B 2 ✓ and A 2 ✓ not wired |
| WO-02-abc | WO-02 · a giant set, A B C · dark-HC | O-21: Incline Press DB, Lateral Raise DB and Tricep Pushdown in one superset tagged **A, B, C** (blue tags, one edge line), focus "NOW · C · TRICEP PUSHDOWN · SET 1 OF 3", "rest 1:30 after the round"; A1 and B1 logged. | F1 |
| WO-02-s9f | WO-02 · s9 failure set in focus · dark-HC | 52:40, "10 of 14 sets". Tricep Dips: focus "NOW · TRICEP DIPS · SET 2 OF 2 · TO FAILURE" (only TO FAILURE coral), "+10 kg × 9 reps" (bodyweight: added weight). Badge 2 coral. | Log → WO-02-s9d |
| WO-02-s9d | WO-02 · s9 drop set in focus · dark-HC | 58:20, "13 of 14 sets". Tricep Pushdown: 1 ✓, 2 ✓, focus "NOW · TRICEP PUSHDOWN · DROP SET" (violet), 20 kg × 12; badge D violet. | Log → WO-02-s13 |
| WO-02-s10bw | WO-02 · s10 bodyweight rows · dark-HC | Tricep Dips in focus set 1: columns Set · Previous · +kg · Reps · ✓; rows "+10 × 9 @2", "+10 × 8 @0" (set 2 coral). | Log → WO-02-s9f |
| WO-02-s10td | WO-02 · s10 time and distance rows · dark-HC | Two added exercises at the end: Plank (time: columns Set · Previous · Time · ✓, "0:45", one 88-wide cell) in focus "0:45"; Walking Lunge (distance: m, "20 m"). | Log → WO-02-s13 |
| WO-02-s12 | WO-02 · s12 record just set · dark-HC | Set 2 logged 82.5 × 8 @2 with a star by the ✓ row; toast "New record · Bench Press BB · 82.5 kg"; focus set 3 (80 × 7, hold, plates "25 + 5"), rest card 2:00. "2 of 14 sets". | Skip → WO-02-s4; ✓ set 3 → WO-02-s4 |
| WO-02-s13 | WO-02 · s13 all sets logged · dark-HC | 1:02:10, "14 of 14 sets"; Finish filled; every exercise folded in the overview with n/n ✓ and kind-coloured dots. | Finish → WO-21-records |
| WO-02-s14 | WO-02 · s14 an exercise skipped · dark-HC | Overview shows "Tricep Dips · Skipped" in grey (no dots). | Tricep Dips row → WO-11-skip |
| WO-02-s15 | WO-02 · s15 new exercise, no history · dark-HC | s15 new exercise, no history: **Copenhagen Plank** (Type Time, per side) instead of Fly Cable. Previous "–", time field empty, ✓ waits for a value. "14 of 17 sets". | kg cell → WO-05-weight |
| WO-02-s16 | WO-02 · s16 restored after a crash · dark-HC | WO-02-s2 exactly, with a toast under the bar: "Restored. Every logged set is here." | as WO-02-s2 |
| WO-02-note | WO-02 · today's note open · dark-HC | WO-02-s2 with Bench Press BB's note field open under its header (F-18 Note): "Note for today" placeholder, caret, the sticky note "Arch, feet planted." above it in grey, the keyboard up. | Done or outside → WO-02-s2 |
| WO-02-removed | WO-02 · an exercise removed · dark-HC | After Remove exercise on Bench Press BB: Bench gone, focus "NOW · A · INCLINE PRESS DB · SET 1 OF 3", "0 of 11 sets", a toast "Bench Press BB removed" with **Undo** above the bottom. | F1; Undo → WO-02-s2 |
| WO-02-swap | WO-02 · Bench swapped for Chest Press Machine · dark-HC | After Swap → Chest Press Machine (F-19): Chest Press Machine in Bench's place with what EX-04 and section 3 say carries over; focus on its next set; a toast "Swapped for Chest Press Machine". | F1 |
| WO-03-strip | WO-03 · scrolled, strip shown · dark-HC | Bar on glass with "Push A ⋯" and "24:13 · 1/14 sets" in the middle; under it the strip "NOW · BENCH PRESS BB · SET 2" / "82.5 kg × 8 RIR 2"; the list scrolled to the superset. | strip → WO-02-s2; F1 |
| WO-03-rest | WO-03 · strip during rest · dark-HC | WO-03-strip plus the rest card 1:43. | strip → WO-02-s3 |
| WO-03-end | WO-03 · scrolled to the end · dark-HC | The list's end: last exercise rows, Add exercises, Discard workout; the strip on top. | Add exercises → EX-03-none; Discard → WO-16-discard |
| WO-04-new | WO-04 · dock, set not logged · dark-HC | Over WO-02-s2 (scrim clear): grabber, "Bench Press BB · Set 2", Weight 82.5 and Reps 8 with − +, the bar line "Bar 20 ▾" with the plates drawn per side "25 + 5 + 1.25", RIR segments + Rest 2:00, **Log**. | CLOSE(WO-02-s2); Log → WO-02-s12; Weight → WO-05-weight; 4+ → WO-04-rir4 |
| WO-04-logged | WO-04 · dock, logged set · dark-HC | Set 1 (80 × 8 @2): **Save change**. | CLOSE(WO-02-s2); Save change → WO-02-s2 |
| WO-04-rest | WO-04 · dock from a rest line · dark-HC | Rest field focused with the keypad (WO-05) and **Save**: "Rest after set 2", 2:00. | CLOSE(WO-02-s2); Save → WO-02-s2 |
| WO-04-rir4 | WO-04 · dock, RIR 4+ open · dark-HC | The 4 · 5 · 6 · 7 · 8 · 9 · 10 row open under the segments, 6 chosen; the segment reads 6. | Log → WO-02-s12 |
| WO-04-heavy | WO-04 · dock, a heavy bar · dark-HC | Deadlift BB 222.5 kg: plates "4 × 25 + 1.25" per side, the drawing shrinks, the text wraps. | CLOSE(WO-02-s2) |
| WO-04-lb | WO-04 · dock, an lb exercise · dark-HC | Curl DB set in lb: 25 lb × 12, step 5 lb, no bar line. | CLOSE(WO-02-s2) |
| WO-05-weight | WO-05 · keypad, weight · dark-HC | Dock with keypad: Weight field focused showing "85" (just typed 8, 5), Reps 8, Rest 2:00 as a third field; top row Hide · Plates · − · +; digits on glass; ".", 0, ⌫; **Next**. The rest card is hidden. | Next → WO-05-reps; Plates → WO-05-plates; Hide → WO-04-new |
| WO-05-reps | WO-05 · keypad, reps · dark-HC | Reps focused, fresh (selected, replaced by the first key), no Plates key. | Next → WO-05-rest |
| WO-05-rest | WO-05 · keypad, rest · dark-HC | Rest focused "2:00", the button reads **Log ›**. | Log › → WO-02-s12 |
| WO-05-plates | WO-05 · keypad, plates · dark-HC | Plate keys 25 · 20 · 15 · 10 · 5 · 2.5 · 1.25, Empty, ⌫ (15 greyed: not in Carl's gym); keyed 25, 5, 1.25 drawn heaviest innermost; weight reads 82.5. | Next → WO-05-reps |
| WO-06-round | WO-06 · a superset round's rest · dark-HC | After A1 and B1: rest card "Rest · 1:30" full ring; focus moved to A2. | Skip → WO-02-s5c; ⌄ → WO-17-rest |
| WO-06-free | WO-06 · a free rest · dark-HC | From ⏱ 2:00: card label "Free rest", 1:58. | Skip → WO-02-s2 |
| WO-06-last | WO-06 · last seconds · dark-HC | 0:03, the ring a sliver. | Skip → WO-06-ended |
| WO-06-ended | WO-06 · ended · dark-HC | The card at 0:00 with the ring full in teal and "Rest over · set 3 next", drawn solid at 0:00; it then fades and drops 8 pt (Motion page). | anywhere → WO-02-s2 |
| WO-07-menu | WO-07 · free rest menu · dark-HC | Pull-down from ⏱: "Free rest" title, 0:30, 1:00, 2:00, 3:00, Custom time…. | 2:00 → WO-06-free; Custom → WO-07-custom; outside → WO-02-s2 |
| WO-07-custom | WO-07 · custom time · dark-HC | Prompt "Custom time", field "1:30", "Minutes and seconds, such as 1:30", Cancel, Start. | Start → WO-06-free; Cancel → WO-02-s2 |
| WO-07-replace | WO-07 · menu while resting · dark-HC | The menu open over WO-02-s3 (rest 1:43 visible below); caption row "Replaces the rest running now". | 2:00 → WO-06-free |

## Workout · around the workout · page `workout-more`

| file | title | shows | links |
|---|---|---|---|
| WO-08-menu | WO-08 · workout menu · dark-HC | Pull-down from ⋯ by Push A: Rename, Workout note, Change start time. | Rename → WO-08-rename; Note → WO-08-note-empty; Time → WO-08-time; outside → WO-02-s2 |
| WO-08-rename | WO-08 · rename · dark-HC | Prompt "Rename workout", field "Push A", keyboard not drawn; Cancel, Save. | Save/Cancel → WO-02-s2 |
| WO-08-note-empty | WO-08 · note, empty · dark-HC | Prompt "Workout note", placeholder "Energy, sleep, pain…", a mic glyph for dictation; Cancel, Save. | Save → WO-08-note |
| WO-08-note | WO-08 · note, filled · dark-HC | Prompt filled "Sov 7 h. Axeln okej." | Save → WO-02-s2 |
| WO-08-time | WO-08 · start time · dark-HC | A time picker sheet: "Started at" 17:02 as wheels (hours, minutes), Cancel, Save. | Save → WO-02-s2 |
| WO-09-menu | WO-09 · exercise menu · dark-HC | From Bench's ⋯: tiles Note, Swap; rows Sticky note; Rest for all sets ›, Create superset, Order and skip; Exercise settings, Exercise details (ⓘ, O-02); Remove exercise (red). | Note → WO-02-note; Sticky note → WO-09-sticky; Remove exercise → WO-02-removed; Swap → EX-04; Details → EX-05-sheet; Rest › → WO-09-rest; Create superset → WO-10; Order → WO-11; Settings → WO-12-bb; outside → WO-02-s2 |
| WO-09-rest | WO-09 · rest submenu · dark-HC | ‹ Rest for all sets, then 1:00, 1:30, 2:00 ✓, 2:30, 3:00. | 3:00 → WO-02-s2; back → WO-09-menu |
| WO-09-superset | WO-09 · inside a superset · dark-HC | From Incline Press DB's ⋯ (tag A): Remove from superset instead of Create superset. | outside → WO-02-s5 |
| WO-09-noweight | WO-09 · a non-weight exercise · dark-HC | From Plank's ⋯: no Exercise settings row. | outside → WO-02-s10td |
| WO-09-sticky | WO-09 · sticky note prompt · dark-HC | Alert with a text field over WO-02-s2: "Sticky note", "Shown every time you do Bench Press BB, in every template.", field "Arch, feet planted."; Cancel · Save. | Save, Cancel → WO-02-s2 |
| WO-10 | WO-10 · create superset · dark-HC | Dialog "Superset with…": Incline Press DB, Lateral Raise DB, Tricep Dips, Tricep Pushdown; Cancel. | Tricep Dips → WO-02-s5; Cancel → WO-02-s2 |
| WO-10-one | WO-10 · one exercise left · dark-HC | WO-01-picked with Bench's ⋯ menu open: every row of WO-09-menu except Create superset, which hides while the workout holds one exercise. | Note, outside → WO-01-picked; Swap → EX-04; Details → EX-05-sheet; Rest › → WO-09-rest; Order → WO-11; Settings → WO-12-bb |
| WO-11 | WO-11 · order and skip · dark-HC | Sheet "Order and skip": Bench Press BB; the superset A Incline + B Lateral as one group; Tricep Dips; Tricep Pushdown; each with ↑ ↓ and Skip. Done. | Skip on Dips → WO-11-skip; Done → WO-02-s2 |
| WO-11-skip | WO-11 · one skipped · dark-HC | Tricep Dips greyed, its button reads Include; caption "Skipped exercises stay in the log; their sets are dropped at Finish." | Done → WO-02-s14 |
| WO-12-bb | WO-12 · settings, barbell · dark-HC | Sheet "Bench Press BB": Step 2.5 kg ▾, Bar Olympic 20 kg ▾, Unit kg / lb (kg on); Done. | Done → WO-02-s2 |
| WO-12-db | WO-12 · settings, dumbbells in lb · dark-HC | "Curl DB": Step 5 lb, no Bar row, Unit lb on. | Done → WO-04-lb |
| WO-13-menu | WO-13 · set type menu · dark-HC | After + Add set: set 4 (80 × 7) added under set 3; the menu from its badge: Warm-up W (amber), Working set ✓ 1, Drop set D (violet), To failure (coral number). | Warm-up → WO-13-moved; outside → WO-13-added |
| WO-13-added | WO-13 · a set added · dark-HC | Set 4 added as a copy of set 3, "+ Add set (2:00)" under it. | badge 4 → WO-13-menu |
| WO-13-moved | WO-13 · a warm-up moved up · dark-HC | The new W moved up to be next, above set 2: 55 kg × 3 (Q22's next rung, 70 % of 80), plates "10 + 5 + 2.5"; the focus card shows it with WARM-UP. | ✓ W → WO-02-s2 |
| WO-13-logged | WO-13 · on a logged set · dark-HC | Menu on set 1 (logged): Working set checked; caption "The set stays where it is." | outside → WO-02-s2 |
| WO-13-warmup | WO-13 · warm-up selected · dark-HC | The set type menu from the W badge of Bench Press BB: Warm-up ✓ (amber), Working set, Drop set, To failure. | outside → WO-02-s2 |
| WO-14-revealed | WO-14 · swipe, Delete revealed · dark-HC | Set 3's row moved left 88 pt, red **Delete** showing. | Delete → WO-14-full; elsewhere → WO-02-s2 |
| WO-14-full | WO-14 · full swipe deleting · dark-HC | Set 3 swiped past half: the red fills the row, Delete hugging the left; the rows below about to close up. | anywhere → WO-02-s2 |
| WO-15-one | WO-15 · one set not logged · dark-HC | Action sheet "1 set not logged": Log it as it is and save · Save without it · Discard workout (red) · Continue workout. | Log/Save → WO-21-records; Discard → Main; Continue → WO-02-s9d |
| WO-15-several | WO-15 · several sets not logged · dark-HC | Over WO-02-s9d-like state: "2 sets not logged": Log them as they are and save · Save without them · Discard workout · Continue workout. | Log/Save → WO-21-records; Discard → Main; Continue → WO-02-s2 |
| WO-16-discard | WO-16 · discard alert · dark-HC | "Discard the workout?" "Every set in it is deleted." Cancel · Discard (red). | Discard → Main; Cancel → WO-02-s2 |
| WO-16-empty | WO-16 · finish with no sets · dark-HC | Over WO-01-none: same alert. | Discard → Main; Cancel → WO-01-none |
| WO-17-plan | WO-17 · minimised, no rest · dark-HC | PL-01-week with the bar "Push A · 24:13 · 1/14 sets" above the tab bar. | bar → WO-02-s2; TAB(Plan) except Home → SH-02-inprog |
| WO-17-rest | WO-17 · minimised, resting · dark-HC | Main with the bar showing the amber ring and "1:43". | bar → WO-02-s3; TAB(Home) except Plan → WO-17-plan |
| WO-17-killed | WO-17 · after the app was killed · dark-HC | Main after relaunch: bar "Push A · left open · 24:13", subline "Tap to continue". | bar → WO-02-s16 |
| WO-18-lock | WO-18 · rest notification, locked · dark-HC | Lock screen 17:28, Monday 9 November: notification "Strive · now", "Rest is over", "Bench Press BB · set 3 of 3 next". | notification → WO-02-s2 |
| WO-18-banner | WO-18 · banner over another app · dark-HC | The home screen (plain icon grid, no logos) with the banner at the top. | banner → WO-02-s2 |
| WO-19-counting | WO-19 · Live Activity, counting · dark-HC | Lock screen with the Live Activity: Push A · 24:13 elapsed; ring + "1:43"; "Next: Bench Press BB · 82.5 × 8". | activity → WO-02-s3 |
| WO-19-ended | WO-19 · Live Activity, ended · dark-HC | "Rest over" in teal, "Next: Bench Press BB · set 2", the notification below it. | activity → WO-02-s2 |
| WO-19-open | WO-19 · Live Activity, no rest · dark-HC | Workout open without rest: "Push A · 24:13 · 1 of 14 sets", "Next: Bench Press BB · 82.5 × 8". Caption on the board's side note: if the spike allows. | activity → WO-02-s2 |
| WO-20-compact | WO-20 · Dynamic Island, compact · dark-HC | Main in another tab: the island with an amber ring left and "1:43" right. | island → WO-02-s3 |
| WO-20-minimal | WO-20 · Dynamic Island, minimal · dark-HC | Two activities: the island plus a small detached circle with the ring. | circle → WO-02-s3 |
| WO-20-expanded | WO-20 · Dynamic Island, expanded · dark-HC | Long-press: the big island: Push A, ring + 1:43, "Next: Bench Press BB · 82.5 × 8", − 15 s · + 15 s · Skip. | anywhere → WO-02-s3 |
| WO-20-ended | WO-20 · Dynamic Island, ended · dark-HC | The compact island with a teal check and "Rest over". | island → WO-02-s2 |

## Workout · after · page `workout-after`

| file | title | shows | links |
|---|---|---|---|
| WO-21-records | WO-21 · summary with records · dark-HC | "Monday 9 November", "17:02–18:06 · week 6 · Hypertrofi, block 1"; tiles Time 64 min · Avg RIR 1.5 "goal 2–3", "13 of 14 sets" · Sets 15 · Records 3; the three records starred; "How hard was it? RPE" 1–10 with 8 chosen; workout note "Sov 7 h. Axeln okej."; exercises folded with best set vs last ("Bench Press BB · 82.5 × 8 · ↑ +2.5 kg vs last"); Started 17:02, Ended 18:06 (editable), Week 6; **Done**. | Done → WO-22-one; Bench row → WO-21-expanded |
| WO-21-expanded | WO-21 · an exercise expanded · dark-HC | Bench Press BB open: W 40 × 10, 80 × 8 @2, 82.5 × 8 @2 ★, 82.5 × 7 @1. | Done → WO-22-one |
| WO-21-plain | WO-21 · no records, no RIR, no template · dark-HC | An empty workout "Workout" (Bench Press BB only, 4 sets, no RIR logged): Records "–", Avg RIR "–" with no goal line. | Done → Main |
| WO-21-empty | WO-21 · empty workout, Save as template · dark-HC | The same with **Save as template** above Done. | Save as template → WO-23-summary; Done → Main |
| WO-22-one | WO-22 · update template, one change · dark-HC | Alert "Update the template Push A?" "Rest 1:30 → 2:00 on Incline Press DB". "Weights are not changed." Update · Keep as it is. | Update → SH-02-done; Keep → SH-02-done |
| WO-22-several | WO-22 · update template, several · dark-HC | "Added Tricep Dips", "Removed Tricep Pushdown", "Rest 1:30 → 2:00 on Incline Press DB". | Update → SH-02-done |
| WO-23-summary | WO-23 · save as template · dark-HC | Prompt "Save as template", name "Workout 9 Nov" selected; Cancel, Save. | Save → PL-01-saved; Cancel → WO-21-empty |
| WO-23-edit | WO-23 · save from editing · dark-HC | Over WO-25-edit: name "Push A (copy)". | Save → PL-01-week; Cancel → WO-25-edit |
| WO-24 | WO-24 · session detail · dark-HC | Push A, Mon 2 Nov, 17:05–18:03: back "All activities", **Edit**; tiles 58 min · Avg RIR 1.8 "13 of 14 sets" · Sets 14 · Records 1; Lateral Raise DB 10 × 13 ★; RPE 7; note "Kändes stark idag."; exercises folded. | back → WO-26-all; Edit → WO-25-edit; Lateral row → WO-24-expanded |
| WO-24-expanded | WO-24 · sets shown · dark-HC | Every set as logged, badges, @RIR, the star on Lateral Raise DB 10 × 13, that day's exercise note. | back → WO-26-all |
| WO-25-edit | WO-25 · editing · dark-HC | Cancel · Done; the workout's rows with fields; Started 17:05 / Ended 18:03 editable; at the bottom **Do it again**, **Save as template**, **Delete workout** (red). | a cell → WO-25-dock; Done → WO-24; Cancel → WO-25-unsaved; Do it again → WO-02-s1; Save as template → WO-23-edit; Delete → WO-25-delete |
| WO-25-delete | WO-25 · delete alert · dark-HC | "Delete this workout?" "Push A, Monday 2 November. It leaves History and Progress." Cancel · Delete. | Delete → WO-26-all; Cancel → WO-25-edit |
| WO-25-unsaved | WO-25 · unsaved changes · dark-HC | Action sheet "Discard your changes?": Discard changes (red) · Keep editing. | Discard → WO-24; Keep → WO-25-edit |
| WO-25-dock | WO-25 · a past set in the dock · dark-HC | The dock over WO-25-edit (Mon 2 Nov) for Bench set 1, 77.5 × 8 RIR 3: weight and reps fields with − +, RIR row, **Save change**. | Save change, grabber → WO-25-edit |
| WO-26-all | WO-26 · History, All · dark-HC | "All activities" with back; segments All · Strength · Running; Week 45 · 4 workouts · 22.3 km (6 rows, newest first); Week 44 · 4 workouts · 21.3 km with Wed 28 Armar "Extra". | back → EL-01; Push A Mon 2 → WO-24; Long run → RU-05-history; Morning Run → RU-05-history; Strength → WO-26-strength; Running → WO-26-running |
| WO-26-strength | WO-26 · History, Strength · dark-HC | Strength rows only, week headers sum the strength. | All → WO-26-all; Running → WO-26-running; Push A → WO-24 |
| WO-26-running | WO-26 · History, Running · dark-HC | Run rows with a small map thumbnail, km and pace. | All → WO-26-all; a run → RU-05-history |
| WO-26-empty | WO-26 · History, first use · dark-HC | "No workouts yet" with Start a workout. | Start a workout → SH-03-today; back → EL-01-first |

## Plan · page `plan`

| file | title | shows | links |
|---|---|---|---|
| PL-01-week | PL-01 · this week · dark-HC | Large title Plan with ⋯ and +; week card (W2): "Hypertrofi, block 1 ›", "Week 6 of 8" with ‹ ›, "Week 46 · 9–15 Nov", the 8-week bar (1 K, 2 K, 3–5 past, 6 now), the 7 days Mon Push A (today ring, selected) · Tue Ben A · Wed Rest · Thu Pull A · Fri Easy run 8 km · Sat Push B · Sun Long run 14 km; "6 weeks without a deload"; the day card: Push A open with its exercises, **Start workout** (filled) and Edit; Templates heading with ⋯, the swiped cards Push A, Pull A, Ben A, Push B, + New; "Outside the programme" Armar; rows Exercise library (553 exercises) and "Import from Strong — comes with import" disabled. | TAB(Plan); Start workout → WO-02-s1; Edit → PL-05-edit; › → PL-01-next; programme name → PL-08-week6; ⋯ → PL-02-plan; + → PL-05-new; Sat → PL-02-day; Fri → PL-01-run; Push A card → PL-04-pusha; Pull A card → PL-04-pulla; card ⋯ → PL-03-menu; Templates ⋯ → PL-02-templates; Library → EX-01 |
| PL-01-next | PL-01 · another week · dark-HC | Week 7 of 8 · Week 47 · 16–22 Nov, **Today** beside ⋯, Monday's day card shows its plan (Push A) without Start. | Today → PL-01-week; ‹ → PL-01-week |
| PL-01-deloadrec | PL-01 · deload recommended · dark-HC | "Hypertrofi, 12 veckor", Week 9 of 12; the deload line "Deload recommended · 8 weeks since the last deload" with **Deload now**. | Deload now → PL-01-deload |
| PL-01-deload | PL-01 · a deload week · dark-HC | Week 9 marked D in the bar and the title "Week 9 of 12 · Deload"; day card "Push A · deload: half the working sets". | TAB(Plan) |
| PL-01-calib | PL-01 · a calibration week · dark-HC | Week 1 of 8 (K): "Week 41 · 5–11 Oct · Calibration", all done ✓; day card note "Top sets aim for RIR 1". | Today → PL-01-week |
| PL-01-short | PL-01 · Short week on · dark-HC | Sat Push B and Fri Easy run greyed "Dropped"; nothing moved. | ⋯ → PL-02-short |
| PL-01-missed | PL-01 · a missed day and an extra · dark-HC | Week 44 · 26 Oct–1 Nov: Thu Pull A struck through, Wed "Armar" with "Extra"; others ✓. | Today → PL-01-week |
| PL-01-done | PL-01 · today done · dark-HC | Mon ✓; day card Push A done 17:02–18:06 with **Show workout**. | Show workout → WO-24 |
| PL-01-rest | PL-01 · today a rest day · dark-HC | Today Wed 11 Nov: day card "Rest day", "Next: Pull A tomorrow". | Thu → PL-01-week |
| PL-01-run | PL-01 · today a run day · dark-HC | Fri selected: day card "Easy run · 8 km", "Target: distance", shoes Novablast 4, **Start run**. | Start run → RU-01-ready |
| PL-01-noprog | PL-01 · no programme · dark-HC | "No programme" card with **Create programme**; the week on calendar weeks (Week 46 · 9–15 Nov) with done days only; day card offers **Start empty workout**; templates row still shows. | Create programme → PL-12-empty; Start empty workout → WO-01-marks |
| PL-01-notmpl | PL-01 · no templates · dark-HC | "No templates yet" with Create template and Start empty workout; no programme either. | Create template → PL-05-new; Start empty → WO-01-none |
| PL-01-saved | PL-01 · a template just saved · dark-HC | PL-01-week after WO-23's Save (F-28): the Templates section shows **Workout 9 Nov** ("1 exercise · 4 sets · never done") under "Not in a programme", a toast "Saved as template". | TAB(Plan); the new card → PL-04-never |
| PL-01-axlar | PL-01 · a template created · dark-HC | PL-01-week after F-38's Done: **Axlar** ("2 exercises · 6 sets · never done") in the Templates section, a toast "Axlar created". | TAB(Plan) |
| PL-02-plan | PL-02 · Plan menu · dark-HC | Pull-down from ⋯: Short week, Deload now, Week type ›. | Short week → PL-01-short; Deload now → PL-01-deload; Week type → PL-02-type |
| PL-02-short | PL-02 · Short week on · dark-HC | Plan's ⋯ menu over PL-01-short: **Short week ✓** checked; behind it Push B and the easy run greyed. | Short week → PL-01-week; outside → PL-01-short |
| PL-02-type | PL-02 · week type · dark-HC | ‹ Week type: Normal ✓, Calibration, Deload. | Calibration → PL-01-calib |
| PL-02-day | PL-02 · day card menu · dark-HC | Sat selected (Push B in the day card); its ⋯ open: Move to…, Edit template. | Move to → PL-02-move |
| PL-02-move | PL-02 · move to · dark-HC | "Move Push B to": Wed 11 (rest), Sun 15 (Long run 14 km; Push B joins it), each a row; Cancel. | Sun 15 → PL-02-moved |
| PL-02-moved | PL-02 · a moved session · dark-HC | Week card: Sat empty, Sun shows Push B + Long run; toast "Push B moved to Sunday". | TAB(Plan) |
| PL-02-templates | PL-02 · templates menu · dark-HC | Templates ⋯ open: Show archived. | Show archived → PL-07-list |
| PL-03-menu | PL-03 · template card menu · dark-HC | Push A card's ⋯: Edit, Duplicate, Share, Archive, Delete (red). | Edit → PL-05-edit; Share → PL-03-share; Delete → PL-03-delete; Archive → PL-03-archived |
| PL-03-delete | PL-03 · delete alert · dark-HC | "Delete Push A?" "Your history keeps every workout done from it." Cancel · Delete. | Cancel → PL-01-week |
| PL-03-share | PL-03 · share sheet · dark-HC | The system share sheet with the text preview: "Push A — Bench Press BB: 1 warm-up + 3 × 6–8 @ 2–3 …" (O-23 text). | close → PL-01-week |
| PL-03-archived | PL-03 · archived, the card leaves the row · dark-HC | PL-01-week after Archive on the card PL-03-menu opened from: that card gone from the Templates row, the others closed up, a toast "<name> archived" with **Undo**. | Undo → PL-01-week; TAB(Plan) |
| PL-04-pusha | PL-04 · template preview · dark-HC | Zoomed open: Push A, chest · shoulders · triceps; tiles 5 exercises · 15 sets · 30 min; last done Mon 2 Nov; note "Kontrollerad nedfirning, två sekunder."; numbered exercises with targets ("1 warm-up + 3 × 6–8 @ 2–3"), the superset with edge line and A/B; info buttons; ⋯; **Start workout**; close ×. | Start → WO-02-s1; ⋯ → PL-03-menu; info on Bench → EX-05; × → PL-01-week |
| PL-04-never | PL-04 · never done · dark-HC | "Push A (copy)": "Never done". | × → PL-01-week |
| PL-04-pulla | PL-04 · Pull A preview · dark-HC | PL-04-pusha's layout for **Pull A**: back · biceps, 4 exercises · 13 sets · about 35 min, last done Thu 5 Nov; Deadlift BB 1 warm-up + 3 × 5 @ 2, Lat Pulldown Cable 3 × 8–10 @ 2, Seated Row Cable 3 × 10–12 @ 2, Curl DB 3 × 10–12 @ 1–2. | Start workout → WO-02-pulla; × → PL-01-week |
| PL-05-new | PL-05 · new template · dark-HC | Cancel · New template · Done (dim); name field "Template name"; empty state "Add the exercises you train"; **Add exercises**. | Add exercises → EX-03-tmpl; Cancel → PL-01-week |
| PL-05-edit | PL-05 · editing Push A · dark-HC | Push A with ⋯; per exercise: name, the Set · Previous · Reps · RIR grid (W, 1, 2, 3), target weight row, rest lines, + Add set (2:00), Progression switch on, cue "Skulderbladen ihop."; the superset; "about 30 min"; Delete template (red). | a reps cell → PL-06-reps; Cancel → PL-05-discard; Done → PL-04-pusha; Delete → PL-05-delete |
| PL-05-discard | PL-05 · discard changes · dark-HC | Action sheet "Discard changes?" Discard changes · Keep editing. | Discard → PL-04-pusha; Keep → PL-05-edit |
| PL-05-delete | PL-05 · delete alert · dark-HC | "Delete Push A?" Cancel · Delete. | Cancel → PL-05-edit |
| PL-05-added | PL-05 · new template, exercises added · dark-HC | The new template **Axlar** after EX-03's Add (F-38): Incline Press DB and Lateral Raise DB, three sets each with empty Reps and RIR cells, rest 2:00; Done on. | a reps cell → PL-06-reps; Cancel → PL-05-discard |
| PL-06-reps | PL-06 · range keypad, reps · dark-HC | the range keypad over **PL-05-added (Axlar)**, Incline Press DB set 1 Reps typed "8 – 10" | Same for the rest → PL-06-same; RIR cell, Next → PL-06-rir |
| PL-06-rir | PL-06 · range keypad, RIR · dark-HC | the RIR field over Axlar, "2" | Same for the rest → PL-06-same |
| PL-06-same | PL-06 · after Same for the rest · dark-HC | Axlar after Same for the rest: every set 8–10 @ 2, keypad closed | Done → PL-01-axlar; a reps cell → PL-06-reps; Cancel → PL-05-discard |
| PL-07-list | PL-07 · archived templates · dark-HC | Back "Plan"; Helkropp · last done 14 Sep, its ⋯ open: Restore, Delete. | Restore → PL-01-week; back → PL-01-week |
| PL-07-empty | PL-07 · no archived templates · dark-HC | "No archived templates". | back → PL-01-week |
| PL-08-week6 | PL-08 · programme, this week open · dark-HC | Bar ‹ with ⋯; "Hypertrofi, block 1", Start 5 Oct 2026 · 8 weeks · now week 6; tabs Weeks · Weights · Log · Settings; rows Std, 1 K, 2 K, 3, 4, 5, 6 "now" (open: segmented Normal ✓ / Calibration / Deload and the 7 days as rows with menus), 7, 8. | back → PL-01-week; Weights → PL-09; Log → PL-10; Settings → PL-11; week 8 → PL-08-week8; Std → PL-08-std; Thu menu → PL-08-daymenu |
| PL-08-std | PL-08 · standard week open · dark-HC | Std open: Mon Push A, Tue Ben A, Wed Rest, Thu Pull A, Fri Easy run 8 km, Sat Push B, Sun Long run 14 km; caption "Edit once, every week follows". | back → PL-01-week |
| PL-08-week8 | PL-08 · week 8 made a deload · dark-HC | Week 8 open with Deload chosen; its row shows D. | back → PL-01-week |
| PL-08-daymenu | PL-08 · a day's menu · dark-HC | Week 6, Thursday's menu open: Push A, Pull A ✓, Ben A, Push B, Armar, Run…, Rest. | Run… → PL-08-runmenu; Rest → PL-08-changed |
| PL-08-changed | PL-08 · a day changed · dark-HC | Week 6's Thu now Rest; the day outlined in the miniature (differs from Std). | back → PL-01-week |
| PL-08-runmenu | PL-08 · Run… submenu open · dark-HC | O-12: PL-08-daymenu's Thursday menu with **Run…** open: ‹ Run, then Easy · Long · Tempo · Intervals. | Tempo → PL-08-runtarget; outside → PL-08-week6 |
| PL-08-runtarget | PL-08 · run target · dark-HC | O-12: a prompt "Tempo run · Thursday": Target segmented **Distance** · Time, a field "6 km"; Cancel · Save. | Save → PL-08-tempo; Cancel → PL-08-week6 |
| PL-08-tempo | PL-08 · a run day set · dark-HC | Week 6 open with Thursday "Tempo · 6 km" (run icon) in place of Pull A. | back as PL-08-week6 |
| PL-09 | PL-09 · programme weights · dark-HC | Squat BB 100 → 107.5 kg, Bench Press BB 77.5 → 82.5, Deadlift BB 120 → 127.5, Overhead Press BB 50 → 52.5; "top set, from the log". | Weeks → PL-08-week6 |
| PL-09-new | PL-09 · new programme · dark-HC | "Log a workout to see your weights". | Weeks → PL-08-week6 |
| PL-10 | PL-10 · programme log · dark-HC | New note; 2 Nov "Axeln kändes bra på militärpressen." · 19 Oct "Kalibreringen klar. RIR stämde på bänken." · 5 Oct "Nytt block efter deloadveckan." | New note → PL-10-write |
| PL-10-empty | PL-10 · no notes · dark-HC | "Notes about this block, dated" with New note. | New note → PL-10-write |
| PL-10-write | PL-10 · writing a note · dark-HC | A field "9 Nov" with text being written: "Bänken går bra, 82,5 för 8." Cancel · Save. | Save → PL-10 |
| PL-11 | PL-11 · programme settings · dark-HC | Start date 5 Oct 2026 ›, Length 8 weeks ›, Deload "recommended after 4–6 weeks, earliest week 8", Short week order (Push B, Easy run, Ben A …), Archive programme (red). | Length → PL-11-nofixed; Start date → PL-11-date; Archive → PL-11-archive |
| PL-11-nofixed | PL-11 · no fixed length · dark-HC | Length menu: 4, 6, 8 ✓ … 12 weeks, No fixed length (chosen in this board). | back → PL-11 |
| PL-11-date | PL-11 · date picker · dark-HC | An inline calendar for October 2026 with 5 Oct chosen. | done → PL-11 |
| PL-11-archive | PL-11 · archive alert · dark-HC | "Archive Hypertrofi, block 1?" "It keeps its weeks and log. Plan goes back to calendar weeks." Cancel · Archive. | Archive → PL-01-noprog; Cancel → PL-11 |
| PL-12-empty | PL-12 · create programme, empty · dark-HC | O-11: a full-height form sheet (E1 style): Cancel · New programme · Save (dim); Name, Start date, Length, Standard week (7 rows: Rest), Calibration weeks 0. | Cancel → PL-01-noprog; Mon row → PL-12-filled |
| PL-12-filled | PL-12 · create programme, filled · dark-HC | "Hypertrofi, block 1", Mon 5 Oct, 8 weeks, the standard week filled (runs with type and target), 2 calibration weeks; Save on. | Save → PL-01-week |
| PL-12-notmpl | PL-12 · no templates yet · dark-HC | A weekday's menu open offering only Run… and Rest, with a line "Create a template to plan strength days." | Cancel → PL-01-notmpl |

## Exercises · page `exercises`

| file | title | shows | links |
|---|---|---|---|
| EX-01 | EX-01 · library · dark-HC | Back "Plan", title Library, ⋯ and +; search "Search 553 exercises" + filter button; pattern chips (Horizontal press … Explosive and carries, Mobility, Conditioning); sticky "Chest" heading; rows with the crop (Bench Press BB has the real image; others the drawn muscle glyph, O-19), name, "Horizontal press · Barbell", last "82.5 × 8", the Yes badge. Then "Back". | back → PL-01-week; Bench row → EX-05; search → EX-01-search; Vertical pull chip → EX-01-chip; filter → EX-02-none; ⋯ → EX-01-sort; + → EX-09-empty; Mobility → EX-01-mobility |
| EX-01-search | EX-01 · searching · dark-HC | "row": Seated Row Cable (Swap), Single Row DB, Bent Over Row BB, Upright Row BB…; matches on aliases ("row" in Cable Row). | a row → EX-05-noillo; clear → EX-01 |
| EX-01-chip | EX-01 · a chip on · dark-HC | Vertical pull chip filled: Pull Up (+5 × 6, Yes), Lat Pulldown Cable, Chin Up, Assisted Pull Up Machine… | filter → EX-02-db |
| EX-01-filter | EX-01 · a filter on · dark-HC | Filter button filled; "Dumbbells" shown as a removable chip; rows Incline Press DB (26 × 10, Yes), Lateral Raise DB, Curl DB… | filter → EX-02-db; Incline Press DB → EX-05-incline |
| EX-01-nomatch | EX-01 · no match · dark-HC | "zercher": "No exercises match" with **Create "zercher"**. | Create → EX-09-empty |
| EX-01-mobility | EX-01 · Mobility · dark-HC | Mobility chip on: stretches (Couch Stretch, Hip Flexor Stretch, Thoracic Rotation…), no last performance. | back → PL-01-week |
| EX-01-sort | EX-01 · sort menu · dark-HC | ⋯ open: Sort by A–Z ✓, Most used, Last used. | Most used → EX-01 |
| EX-05-incline | EX-05 · About, Incline Press DB · dark-HC | EX-05's layout for **Incline Press DB** (the row tapped in the filtered library, F-44): drawn muscle illustration (O-19), Yes mark, About text; tabs About · History · Chart · Records. | back → EX-01-filter; History → EX-06-incline; Chart → EX-07-incline; Records → EX-08-incline |
| EX-06-incline | EX-06 · History, Incline Press DB · dark-HC | Mon 9 Nov 26 × 10 @2, 26 × 10 @2, 26 × 9 @1 · Mon 2 Nov 26 × 9 @2, 26 × 9 @2, 26 × 8 @1 · Mon 26 Oct … | tabs as EX-05-incline |
| EX-07-incline | EX-07 · Chart, Incline Press DB · dark-HC | Estimated 1RM per workout over 8 weeks, ending 9 Nov. | a point → EX-07-inclinept; tabs as EX-05-incline |
| EX-07-inclinept | EX-07 · a point selected, Incline Press DB · dark-HC | Mon 2 Nov selected: "26 × 9 · e1RM 35.4 kg", "Tap again to open the workout". | the point again → WO-24; tabs as EX-05-incline |
| EX-08-incline | EX-08 · Records, Incline Press DB · dark-HC | Heaviest weight 26 kg · Best e1RM 36.4 kg (26 × 10 @2) · Most volume in a set 260 kg (26 × 10), with dates. | tabs as EX-05-incline |
| EX-02-none | EX-02 · filter, nothing ticked · dark-HC | Sheet: Clear · Filter · Done; Equipment chips; In my programmes chips Yes, Swap, Test, Not set, No (No unticked; caption "No is hidden until you tick it"). | Dumbbells → EX-02-db; Done → EX-01 |
| EX-02-db | EX-02 · Dumbbells ticked · dark-HC | Dumbbells filled; the list behind updates. | Done → EX-01-filter |
| EX-02-no | EX-02 · No ticked · dark-HC | No ticked: Leg Extension Machine appears behind with its No badge. | Done → EX-01 |
| EX-03-none | EX-03 · picker, none ticked · dark-HC | Cancel · Add exercises · New; search; rows with empty checks; the glass bar Superset (dim) · Add (dim). | Cancel → WO-02-s2; New → EX-09-picker; Incline row → EX-03-one |
| EX-03-one | EX-03 · one ticked · dark-HC | Incline Press DB ticked; Add (1) on, Superset dim ("needs two"). | Lateral row → EX-03-two; New → EX-09-picker |
| EX-03-two | EX-03 · two ticked · dark-HC | Incline Press DB, Lateral Raise DB ticked; **Superset (2)** · **Add (2)**. | Superset (2) → WO-02-s5; Add (2) → WO-02-s2; New → EX-09-picker |
| EX-03-search | EX-03 · searching while picking · dark-HC | searching "plank": Plank, Side Plank, **Copenhagen Plank ✓**; Add (1) → WO-02-s15. | Add (1) → WO-02-s15; New → EX-09-picker |
| EX-03-tmpl | EX-03 · from the template editor · dark-HC | EX-03-two opened from PL-05's Add exercises: the same picker, returning to the template. | Add (2) → PL-05-added; Superset (2) not wired; Cancel → PL-05-new; New → EX-09-empty |
| EX-03-created | EX-03 · a new exercise ticked · dark-HC | Back in the picker after EX-09-picker's Save (F-46): **Copenhagen Plank** at the top, ticked, "New"; Add (1). | Add (1) → WO-02-s15; Cancel → WO-02-s2 |
| EX-04 | EX-04 · swap, with alternatives · dark-HC | "Swap Bench Press BB"; Alternatives: Chest Press Machine, Bench Press DB, Smith Machine Bench Press, Push Up; then the library list. | Chest Press Machine → WO-02-swap; Cancel → WO-02-s2 |
| EX-04-none | EX-04 · swap, none · dark-HC | Swapping Lateral Raise DB: "No alternatives set" caption then the list. | Cancel → WO-02-s5 |
| EX-05 | EX-05 · About · dark-HC | Back "Library", "Bench Press BB" with ⋯; segments About · History · Chart · Records; the illustration (real image); In my programmes: Yes ▾ "Shown first in quick picks and in the picker"; numbered instructions and 2 tips; Primary Chest; Secondary Triceps, Front delts; Equipment Barbell; Sticky note "Arch, feet planted."; Settings: Weight × reps · both sides · step 2.5 kg · Olympic bar 20 kg · kg. | back → EX-01; History → EX-06; Chart → EX-07; Records → EX-08; ⋯ → EX-10-used; mark → EX-05-mark |
| EX-05-sheet | EX-05 · as a sheet from the workout · dark-HC | The same in a sheet over WO-02-s2, grabber, Done. | Done → WO-02-s2 |
| EX-05-noillo | EX-05 · no illustration, Swap mark · dark-HC | Seated Row Cable: the drawn muscle glyph (back highlighted), text first; In my programmes: Swap "the alternative, Single Row DB, is suggested first." | back → EX-01 |
| EX-05-mark | EX-05 · mark menu open · dark-HC | The mark pull-down: Yes ✓, Swap, Test, No, Not set, each with its one-line meaning. | Swap → EX-05-noillo |
| EX-06 | EX-06 · History · dark-HC | Cards per workout: Mon 9 Nov · Push A · e1RM 110.0 kg: W 40 × 10, 1 80 × 8 @2, 2 82.5 × 8 @2 ★, 3 82.5 × 7 @1; Mon 2 Nov · Push A · e1RM 109.3…; Mon 26 Oct. | a card → WO-24; About → EX-05 |
| EX-06-empty | EX-06 · History, empty · dark-HC | "Log a workout to see your history". | About → EX-05 |
| EX-07 | EX-07 · Chart · dark-HC | Estimated 1RM per workout, 3 mo · 1 yr · All (3 mo on), metric menu (e1RM ▾); grey points, the line; under it Heaviest set and Volume per workout. | a point → EX-07-point |
| EX-07-point | EX-07 · a point selected · dark-HC | The tapped point teal; the title reads "110.0 kg · Mon 9 Nov"; "Tap again to open the workout". | the point → WO-24 |
| EX-07-empty | EX-07 · Chart, empty · dark-HC | Dashed empty chart "Log a workout to see your chart". | About → EX-05 |
| EX-08 | EX-08 · Records · dark-HC | Heaviest weight 82.5 kg · Best e1RM 110.0 kg · Most volume in a set 660 kg (82.5 × 8); best weight for 1–12 reps grid; records history list with dates. | About → EX-05 |
| EX-08-empty | EX-08 · Records, empty · dark-HC | "Log a workout to see records". | About → EX-05 |
| EX-08-bw | EX-08 · bodyweight records · dark-HC | Pull Up: Most reps 12, Most added weight +10 kg × 5, Best e1RM (with body weight 82.4). | back → EX-01-chip |
| EX-09-empty | EX-09 · create exercise, empty · dark-HC | Full-height opaque sheet: Cancel · New exercise · Save (dim). Rows: Name, Primary muscle, Secondary muscles, Pattern, Equipment, Type, Per side, Step, Bar, Unit, Instructions, In my programmes, Sticky note. | Cancel → EX-01; Type → EX-09-menu |
| EX-09-menu | EX-09 · a menu open · dark-HC | Type's pull-down: Weight × reps, Bodyweight × reps, Reps only, Time ✓, Distance. | Time → EX-09-filled |
| EX-09-muscles | EX-09 · secondary muscles · dark-HC | The checklist inside the sheet: Glutes ✓, Hip adductors ✓, Obliques… | Done → EX-09-filled |
| EX-09-filled | EX-09 · filled · dark-HC | Copenhagen Plank · Core · Hip adductors, Obliques · Core · Bodyweight · Time · Per side on; Save on. | Save → EX-01 |
| EX-09-picker | EX-09 · from the picker's New, filled · dark-HC | Create exercise as a sheet over EX-03: Name "Copenhagen Plank", Primary muscle Adductors, Pattern Core, Equipment Bodyweight, Type Time, Per side on; Save on. | Save → EX-03-created; Cancel → EX-03-none |
| EX-10-used | EX-10 · edit a used exercise · dark-HC | "Bench Press BB": the form; Type locked "Weight × reps · locked: 46 sets are logged with it"; **Merge data**; **Archive**; no Delete. | Merge → EX-11-list; Cancel → EX-05 |
| EX-10-unused | EX-10 · edit an unused exercise · dark-HC | "Copenhagen Plank": Type editable; **Delete exercise** (red) shown. | Cancel → EX-01 |
| EX-10-archived | EX-10 · archived · dark-HC | Exercise detail of an archived exercise with a banner "Archived · hidden from the library" and Restore. | back → EX-01 |
| EX-11-list | EX-11 · merge, choose · dark-HC | "Merge Bench Press into…": a searchable list, Bench Press BB highlighted. | Bench Press BB → EX-11-alert |
| EX-11-alert | EX-11 · merge alert · dark-HC | "Move every set from Bench Press to Bench Press BB?" "Bench Press is archived afterwards." Cancel · Merge. | Merge → EX-11-done |
| EX-11-done | EX-11 · done · dark-HC | EX-05 (Bench Press BB) after Merge: a toast "12 sets moved from Bench Press. Bench Press is archived." with **Show**. | Show → EX-10-archived; back → EX-01 |

## Running · page `running`

| file | title | shows | links |
|---|---|---|---|
| RU-01-search | RU-01 · looking for GPS · dark-HC | Full screen without tabs: the map (`map-std-dark`/light by mode) at the start point, close ×; a bottom card: "Looking for GPS…" spinner; "Easy run · 8 km" planned; Shoes "ASICS Novablast 4 ›"; Start run (dim). | × → Main; anywhere → RU-01-ready |
| RU-01-ready | RU-01 · GPS ready · dark-HC | "GPS ready" (teal dot); **Start run** filled. | Start run → RU-02; Shoes → EL-12; × → Main |
| RU-01-weak | RU-01 · weak signal · dark-HC | "Weak GPS signal" (amber dot), "Start anyway, or wait under open sky."; Start run on. | Start → RU-02 |
| RU-02 | RU-02 · running, distance · dark-HC | 3.42 km at 96 pt (O-05 default), Time 19:41 · Pace 5:38 /km · Avg 5:45 /km; the live map as a panel (route-live) with a handle; **Pause** (filled round) and **Lap**. | big number → RU-02-time; Pause → RU-03; Lap → RU-02-lap; map handle → RU-02-nomap |
| RU-02-time | RU-02 · running, time · dark-HC | 19:41 large, the others below. | big number → RU-02-pace |
| RU-02-pace | RU-02 · running, current pace · dark-HC | 5:38 /km large. | big number → RU-02-avg |
| RU-02-avg | RU-02 · running, average pace · dark-HC | 5:45 /km avg large. | big number → RU-02 |
| RU-02-lap | RU-02 · a lap just taken · dark-HC | Banner "Lap 2 · 1.00 km · 5:41"; lap count "Lap 3" under the stats. | anywhere → RU-02 |
| RU-02-gps | RU-02 · GPS lost · dark-HC | "GPS lost · still timing" (red dot) and the route's dashed tail (route-livegap). | anywhere → RU-02 |
| RU-02-nomap | RU-02 · map swiped away · dark-HC | The map panel gone; the numbers fill the screen larger; a small map button to bring it back. | map button → RU-02 |
| RU-02-locked | RU-02 · screen locked · dark-HC | Lock screen with the run's Live Activity: 3.42 km · 19:41 · 5:38 /km, Pause. (O-07) | activity → RU-02 |
| RU-02-mini | RU-02 · run minimised · dark-HC | Main with a bar "Easy run · 3.42 km · 19:41" above the tabs (O-07). | bar → RU-02 |
| RU-02-island | RU-02 · Dynamic Island, a run · dark-HC | O-07: another app open, the island expanded (long press) for the run: run icon, "3.42 km", "19:36", "5:44 /km", Pause and Lap buttons; drawn like WO-20-expanded. | the island → RU-02 |
| RU-03 | RU-03 · paused · dark-HC | "Paused" over the stopped clock 19:41, numbers dimmed; **Resume** (filled) and **Finish**. | Resume → RU-02; Finish → RU-04 |
| RU-04 | RU-04 · finish run · dark-HC | Action sheet "Finish the run?": Save run · Discard run (red) · Resume. | Save → RU-05-done; Discard → Main; Resume → RU-02 |
| RU-04-short | RU-04 · a very short run · dark-HC | Header "This run is 0.08 km. Save it anyway?" | Save → RU-05-done; Discard → Main |
| RU-05-done | RU-05 · just finished · dark-HC | Morning Run: the map on top (route-full); the sheet: "Morning Run", "Friday 6 November · 07:15 · Djurgården, Stockholm"; Distance 8.04 km · Avg pace 5:46 /km · Moving time 46:20 · kcal 540 · Avg HR 146 · Max HR 163; Device iPhone; Shoes ASICS Novablast 4; note field; **Done**. | map → RU-07-std; play → RU-08-play; Done → Main; scroll → RU-06-splits |
| RU-05-history | RU-05 · from History · dark-HC | Long run, Sunday 8 Nov 09:30, 14.21 km · 1:18:23 · 5:31 /km, gain 96 m, Nike Pegasus 41; Records ★ Longest run, Fastest 10 km 54:58; back and **Edit**. | back → WO-26-all; Edit → RU-09; map → RU-07-std |
| RU-05-nohr | RU-05 · no heart rate · dark-HC | Morning Run without HR: the HR tiles and section are gone. | back → WO-26-running |
| RU-05-gap | RU-05 · a GPS gap · dark-HC | The route's dashed gap (route-gap); a line "0.4 km without GPS. Distance is estimated across the gap." | back → Main |
| RU-05-watch | RU-05 · synced from a watch (later) · dark-HC | Device "Apple Watch Series 9 · synced 07:02", HR from the watch; marked later. | back → WO-26-running |
| RU-06-splits | RU-06 · splits · dark-HC | Scrolled: Splits: 1 5:58 … 8 5:39 (fastest, teal) · 0.04 km 0:13, elevation per km, a bar per split. | a split → RU-06-sel |
| RU-06-charts | RU-06 · elevation, pace, heart rate · dark-HC | Elevation (gain 38 m, max 41 m), Pace (avg 5:46, moving 46:20, 5:51 over total, total 47:05, fastest split 5:39), Heart rate (avg 146, max 163), each against distance. | chart → RU-06-sel |
| RU-06-sel | RU-06 · a split selected · dark-HC | Split 5 selected (5:41, +3 m); the chart cursor and value. | back → RU-05-done |
| RU-07-std | RU-07 · map, standard · dark-HC | The route full screen; glass controls: close, map type, 3D, Save route, sources (i). | type → RU-07-sat; 3D → RU-07-3d; i → RU-07-sources; close → RU-05-done |
| RU-07-sat | RU-07 · satellite · dark-HC | map-sat with the route; the type menu showing Standard, Satellite ✓, Hybrid. | 3D → RU-07-3d; close → RU-05-done |
| RU-07-3d | RU-07 · 3D, hybrid · dark-HC | The map tilted (`.tilt`), hybrid labels on; 3D on. | close → RU-05-done |
| RU-07-sources | RU-07 · map sources · dark-HC | A sheet: "Map data", sources and licences as text (the provider is a build choice). | close → RU-07-std |
| RU-08-play | RU-08 · flyover playing · dark-HC | The tilted map with the "you" marker part way, live speed 10.6 km/h, elevation 22 m, distance 4.10 km; scrubber; 1x; pause. | pause → RU-08-paused; close → RU-05-done |
| RU-08-paused | RU-08 · paused, scrubbing · dark-HC | The scrubber dragged to 6.2 km; play. | play → RU-08-play |
| RU-09 | RU-09 · edit run · dark-HC | Cancel · Edit run · Save: Title "Long run", Shoes "Nike Pegasus 41 ›", Note, Delete run (red). | Shoes → EL-12; Save → RU-05-history; Delete → RU-09-delete |
| RU-09-delete | RU-09 · delete alert · dark-HC | "Delete this run?" Cancel · Delete. | Delete → WO-26-running |

## You · page `you`

| file | title | shows | links |
|---|---|---|---|
| EL-01 | EL-01 · You · dark-HC | Large title You, "Carl Lundholm" with initials CL; **Progress** row "998.4 t lifted · 9 records in 8 weeks"; Activities: Long run Sun 8, Push B Sat 7, **All activities**; Gear "2 shoes · 1 bike"; Account: Profile, Settings, Export; Import and Account and sync as two "Later" rows. | TAB(You); Progress → EL-02-8wk; Long run → RU-05-history; Push B → WO-24; All activities → WO-26-all; Gear → EL-10; Profile → EL-05; Settings → EL-06; Export → EL-13; Import → EL-15-source; Account and sync → EL-16-out |
| EL-01-first | EL-01 · first use · dark-HC | No activities: "No workouts yet" with Start a workout; Progress row "Starts with your first workout". | TAB(You); Start a workout → SH-03-today |
| EL-02-8wk | EL-02 · Progress, 8 weeks · dark-HC | Back "You"; Strength · Running; total 998.4 t with a rising curve and "Lika tungt som ungefär 166 afrikanska elefanter." (tap for the next); 8 wk · 6 mo · 1 yr; summary 28 vs 26 workouts, 346 vs 320 sets, 128 t vs 119 t, 9 vs 6 records; main lifts tiles (Squat 136.2 +9.5, Bench 110.0 +3.3, Deadlift 157.2 +9.2, OHP 66.5 +3.2) with Edit (O-14); volume per week bars w39–w46 vs before, dashed avg 17.7, D under w40, K w41–42; sets per muscle (week 45) vs a 10–20 band; records from the last 30 days. | back → EL-01; comparison → EL-02-next; 6 mo → EL-02-6mo; a bar → EL-02-bar; Bench tile → EX-05; Edit lifts → EL-04-three; Running → EL-03; a record → EX-08 |
| EL-02-6mo | EL-02 · 6 months · dark-HC | 6 mo on: volume per month-week bars May–Nov; summary against the 6 months before. | 1 yr → EL-02-1yr; a bar → EL-02-6mobar |
| EL-02-1yr | EL-02 · 1 year · dark-HC | 1 yr on; the comparison line the same as EL-02-8wk (elefanter), so a period tap changes only the period. | 8 wk → EL-02-8wk |
| EL-02-next | EL-02 · 8 weeks, next comparison · dark-HC | EL-02-8wk with only the comparison line changed to the sample's next one (F-53). | comparison → EL-02-8wk; 6 mo → EL-02-6mo; 1 yr → EL-02-1yr; as EL-02-8wk otherwise |
| EL-02-bar | EL-02 · a bar selected · dark-HC | w45 selected teal: "Week 45 · 22.9 t · +4.8 t vs w37". | back → EL-02-8wk |
| EL-02-6mobar | EL-02 · 6 months, a bar selected · dark-HC | EL-02-6mo with one bar selected teal and its value line. | a lift tile → EX-05; 8 wk → EL-02-8wk |
| EL-02-first | EL-02 · first use · dark-HC | "No workouts yet" with Start a workout; empty dashed charts. | Start a workout → SH-03-today |
| EL-02-fewer | EL-02 · fewer than four lifts · dark-HC | Three lift tiles and a dashed fourth "Add a lift". | Add a lift → EL-04-three |
| EL-02-cmp | EL-02 · the 25 comparisons · dark-HC | Strive 1+2: all 25 comparisons in section 5's order, each with its implied weight, no icons, and the one rule (a tap shows the next, looping, never on a timer). 1280 × 800. | none |
| EL-03 | EL-03 · Progress, Running · dark-HC | Running on: km per week w39–w45 (18.4 … 22.3), long run per week (12.0 … 14.2), run records: Longest distance 14.21 km, Longest time 1:18:23, Fastest 1 km 5:12, 5 km 26:41, 10 km 54:58, half marathon –, Most climb 96 m. | Strength → EL-02-8wk; a bar → EL-03-bar |
| EL-03-empty | EL-03 · no runs yet · dark-HC | "No runs yet" with Start a run. | Start a run → RU-01-ready |
| EL-03-bar | EL-03 · a bar selected · dark-HC | w45 22.3 km teal. | back → EL-03 |
| EL-04-three | EL-04 · main lifts, three · dark-HC | "Main lifts" Cancel · Done: Squat BB ✓, Bench Press BB ✓, Deadlift BB ✓, Overhead Press BB, Romanian Deadlift DB, Pull Up…; "Pick 3 to 5". | Romanian Deadlift DB → EL-04-five; Done → EL-02-8wk |
| EL-04-five | EL-04 · main lifts, five · dark-HC | Five ticked; the rest disabled, "Five is the most". | Done → EL-02-8wk |
| EL-05 | EL-05 · Profile · dark-HC | Name Carl Lundholm, Birth year "Not set" (the sample has none), Sex "Not set"; Height 183 cm; Body weight 82.4 kg "used for bodyweight exercises"; Goal "Strength while I recompose" and "10 km under 50 min". | back → EL-01 |
| EL-05-empty | EL-05 · Profile, empty · dark-HC | Fields empty with placeholders; "Only for you. Bodyweight exercises use your weight." | back → EL-01-first |
| EL-06 | EL-06 · Settings · dark-HC | Language "Follow the phone ›", Units kg, Default rest 2:00, Plates and bars ›, Keep the screen awake (on), GPS accuracy "Best", Devices and apps ›, About Strive ›. | back → EL-01; Language → EL-06-menu; Units → EL-06-lb; Plates → EL-07-kg; Devices → EL-08; About → EL-14 |
| EL-06-menu | EL-06 · a menu open · dark-HC | Language menu: Follow the phone ✓, English, Svenska. | English → EL-06 |
| EL-06-lb | EL-06 · lb chosen · dark-HC | Units lb; Default rest 2:30. | Plates → EL-07-lb |
| EL-07-kg | EL-07 · plates and bars, kg · dark-HC | Plates: 25 ✓ 20 ✓ 15 (unticked) 10 ✓ 5 ✓ 2.5 ✓ 1.25 ✓; Bars: Olympic 20, Short 15, EZ 7.5, Hex 25, None 0. | EZ → EL-07-edit; back → EL-06 |
| EL-07-lb | EL-07 · plates in lb · dark-HC | 45, 35, 25, 10, 5, 2.5 lb; bars in lb. | back → EL-06-lb |
| EL-07-edit | EL-07 · editing a bar · dark-HC | EZ bar weight field focused "7.5" with the keypad. | Done → EL-07-kg |
| EL-08-none | EL-08 · nothing linked · dark-HC | Health: Apple Health (Health Connect on Android noted) · Not linked · Link; Devices: Apple Watch, Suunto, Garmin, COROS, Polar, Amazfit, Huawei, Samsung, Wahoo, Whoop, Oura; Apps: Strava, Hevy, Fitbod, Runna; names as text. | Apple Health → EL-09-before |
| EL-08 | EL-08 · Health linked, an error · dark-HC | Apple Health "Linked · last sync 07:02" Unlink; Hevy "Could not reach Hevy" (red) Retry; the rest Not linked. | Apple Health → EL-09-linked |
| EL-08-linking | EL-08 · linking · dark-HC | Garmin "Linking…" spinner. | back → EL-06 |
| EL-09-before | EL-09 · Apple Health, before · dark-HC | What Strive reads (workouts, heart rate) and writes (Strive's workouts, so the Activity rings count them); **Link**. | Link → SH-08-health-sys |
| EL-09-linked | EL-09 · Apple Health, linked · dark-HC | "Linked · last sync today 07:02"; Unlink (red). | back → EL-08 |
| EL-09-na | EL-09 · not available · dark-HC | "Apple Health needs a paid developer account in this build." (a spike decides) | back → EL-08-none |
| EL-10 | EL-10 · gear · dark-HC | Shoes: ASICS Novablast 4 · 412 km · Default for runs; Nike Pegasus 41 · 712 km with the warning "Past 700 km"; Bikes: Bianchi Via Nirone 7 · 2 140 km; Retired ›; + Add gear. | + → EL-11-new; Novablast → EL-11; Pegasus → EL-11; Retired → EL-10-retired; back → EL-01 |
| EL-10-none | EL-10 · no gear yet · dark-HC | "No gear yet" with Add shoes. | Add shoes → EL-11-new |
| EL-10-retired | EL-10 · retired · dark-HC | Brooks Ghost 15 · 803 km · retired. | Ghost → EL-11-retired |
| EL-11-new | EL-11 · add gear · dark-HC | Cancel · New gear · Save: Type Shoes, Name "Novablast 4", Brand ASICS, Model Novablast 4, Starting distance 0 km, Warn at 700 km, Default for Run ✓. | Save → EL-10 |
| EL-11 | EL-11 · gear detail · dark-HC | Novablast 4: total 412 km, the latest activities that used it (Morning Run 8.04 km …); Retire. | Retire → EL-10-retired; back → EL-10 |
| EL-11-retired | EL-11 · retired gear · dark-HC | Brooks Ghost 15 · 803 km; Unretire. | back → EL-10-retired |
| EL-12 | EL-12 · choose shoes · dark-HC | sheet over RU-09 (Shoes **Nike Pegasus 41 · 712 km**, as the sample): Nike Pegasus 41 ✓ (this run), ASICS Novablast 4 · 412 km · default for runs, None. | Novablast → EL-12-other |
| EL-12-other | EL-12 · another chosen · dark-HC | Novablast 4 ✓, the edit screen behind shows it. | Done → RU-09 |
| EL-13 | EL-13 · export · dark-HC | What: Workouts ✓ · Runs · All; Period: Since last export (default) · This week · This month · All · Custom; Format: Strive JSON (for the training log) ✓ · CSV · GPX (runs); "18 workouts, 9 runs"; **Share…**. | Share → EL-13-exporting; Custom → EL-13-custom |
| EL-13-custom | EL-13 · custom period · dark-HC | Custom with From 1 Oct · To 9 Nov. | Share → EL-13-exporting |
| EL-13-nothing | EL-13 · nothing in the period · dark-HC | "Nothing to export since 2 Nov"; Share dim. | back → EL-01 |
| EL-13-exporting | EL-13 · exporting · dark-HC | The button "Exporting…" with a spinner. | anywhere → EL-13-share |
| EL-13-share | EL-13 · share sheet · dark-HC | The system share sheet: file "strive-2026-11-09.json · 84 KB", Save to Files, AirDrop, Mail… (generic glyphs). | close → EL-01 |
| EL-14 | EL-14 · About Strive · dark-HC | Strive 0.4 (build 54); "Built by Carl Lundholm"; Credits: "Exercise data by RepDB"; illustrations by Carl; Open-source licences ›. | back → EL-06 |

## Later · page `later`

| file | title | shows | links |
|---|---|---|---|
| EL-15-source | EL-15 · import, source (later) · dark-HC | Import: Strong (CSV export), Strava, A file. | Strong → EL-15-preview |
| EL-15-preview | EL-15 · import, preview (later) · dark-HC | "From Strong: 212 workouts, March 2024 – October 2026, 41 exercises (38 matched, 3 to map)"; Confirm. | Confirm → EL-15-importing |
| EL-15-importing | EL-15 · importing (later) · dark-HC | Progress bar 64 %, "Importing 136 of 212". | anywhere → EL-15-done |
| EL-15-done | EL-15 · import done (later) · dark-HC | "212 workouts imported" ✓, View history. | View history → WO-26-all |
| EL-16-out | EL-16 · sync, signed out (later) · dark-HC | Sign in to back up and sync. | Sign in → EL-16-syncing |
| EL-16-syncing | EL-16 · syncing (later) · dark-HC | Syncing… spinner. | anywhere → EL-16-synced |
| EL-16-synced | EL-16 · synced (later) · dark-HC | Last backup today 07:04, 1 284 sets. | back → EL-01 |
| EL-16-paused | EL-16 · project paused (later) · dark-HC | "The backup server is paused. Your data stays on this phone." Resume. | back → EL-01 |
| EL-17-notarget | EL-17 · food, no target (later) · dark-HC | Food tab filled: today's 1 420 kcal, protein 96 g; Set a target. | Set a target → EL-19-unset; Log food → EL-18-search |
| EL-17-ontrack | EL-17 · food, on track (later) · dark-HC | 2 150 of 2 600 kcal, protein 148 of 165 g, rings for macros; meals. | Log food → EL-18-recent |
| EL-17-over | EL-17 · food, over (later) · dark-HC | 2 840 of 2 600 kcal in amber. | back → EL-17-ontrack |
| EL-18-search | EL-18 · log food, search (later) · dark-HC | Search "kvarg", results; barcode. | a result → EL-18-portion |
| EL-18-recent | EL-18 · log food, recent (later) · dark-HC | Recent foods. | a food → EL-18-portion |
| EL-18-portion | EL-18 · portion (later) · dark-HC | Portion picker 250 g, Add. | Add → EL-17-ontrack |
| EL-19-unset | EL-19 · targets unset (later) · dark-HC | Calories, protein, carbs, fat: empty. | Save → EL-19-set |
| EL-19-set | EL-19 · targets set (later) · dark-HC | 2 600 kcal, 165 g protein, 300 g carbs, 80 g fat. | back → EL-17-ontrack |
| LT-activity | O-22 · Activity bubble (later) · dark-HC | SH-03 with a fifth bubble "Activity". | Activity → LT-activity-list |
| LT-activity-list | O-22 · activity list (later) · dark-HC | Activity types, last used first: Cycling, Walking, Swimming, Yoga, Climbing… | close → Main |
