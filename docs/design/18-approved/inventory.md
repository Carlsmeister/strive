# Strive: the inventory of every screen and flow

The completeness checklist for the three designs and their review (2026-11-09). **If a screen is
missing here, it is missing from every design.** It covers the finished product, not version 1.

**Read from:** strive `origin/dev` at `bc762e0` (PR #54): `CLAUDE.md`, `docs/design/` (14, 15, 16,
17, README), `docs/adr/`, `shared/src/commonMain/`; GitHub strive #2, #4, #5, #6, #7, #8, #9,
#14, #17 (all 34 comments), #18, #19, #20, #25, #26, #27, #42–#48, #52, and the commit messages of
PRs #49–#54; the round-two canvas https://claude.ai/artifact/6hPecJvYLJcGA7Wk3197zx.

## Conventions

- **IDs are stable.** Designers name artboards by them (`WO-02 · state 3 · dark-HC`).
- **Section 1 names elements as design 1 draws them** (the decided sketches and the built app). For
  designs 2 and 3 every piece of data, action and state binds; the arrangement does not (section 3).
- **Every screen in every state is drawn in dark, light and Increase Contrast** at 390 × 844
  (brief). Dark with Increase Contrast is the mode Carl judges by.
- **Built** paths are relative to `shared/src/commonMain/kotlin/se/carlsmeister/strive/` on `dev`.
  "Partly" names what is missing.
- **Sketch** means strive `docs/design/17-strength/index.html` and the choice named; the published
  pages are 14 https://claude.ai/artifact/4k4iruXJJDBqG6352uVkZH, 16 (components)
  https://claude.ai/artifact/Q1Cu1mfmb7WKeJhNvZWwVk, 17 https://claude.ai/artifact/WsDF57N3xVnqBJ74wZfjCX.
  **Canvas** means the round-two canvas above, by board name.
- **Tier:** v1 unless marked **later** (after version 1: #7 import, #8 sync, #9 nutrition, #26
  activity types, #27 watch sync, #52 gear and devices). Whether the designs draw the later
  screens in full is open point O-01.
- **Language.** The app follows the phone's language, English or Swedish, with an override in
  Settings (#25, #3). Designs use the English UI (brief); exercise names are English in every
  language (#4); user content (template names, notes) stays as Carl wrote it, often Swedish.
  Decisions quote Swedish labels; the built strings are in `composeResources/values/strings.xml`
  and `values-sv/strings.xml`. The words that recur:

| Swedish (decisions) | English (built) | Swedish | English |
|---|---|---|---|
| Avsluta | Finish | Lägg till set | Add set |
| Logga / Logga › | Log / Log › | Lägg till övningar | Add exercises |
| Nästa · Dölj · Skivor | Next · Hide · Plates | Släng passet | Discard workout |
| Ändra | Edit | Spara ändring | Save change |
| Förra | Previous | Vila 2:00 | Rest 2:00 |
| Hoppa över | Skip | Fortsätt | Continue |
| Uppvärmning · Dropset · Till failure | Warm-up · Drop set · To failure | Arbetsset | Working set |
| Snitt-RIR | Avg RIR | I mina program (Ja/Byt/Testa/Nej) | In my programmes (Yes/Swap/Test/No) |
| Starta passet · Redigera | Start workout · Edit | Mallar · Ny mall | Templates · New template |
| Tomt pass · Från mall · Löpning | Empty workout · From template · Run | Du · Framsteg · Alla aktiviteter | You · Progress · All activities |
| Kort vecka · Deload nu | Short week · Deload now | Flytta till | Move to |
| Byt · Anteckning · Fast anteckning | Swap · Note · Sticky note | Flytta data | Merge data |

---

## 1. Screens

### Shell

**SH-01 · Tab bar and app frame**
- **Purpose:** the five-place navigation every screen sits in.
- **Shows / actions:** Home · Food · Start (round action button in the middle) · Plan · You, on
  glass, floating over content that scrolls under it. Each tab keeps its own back stack. Above it,
  the minimised workout bar (WO-17) when a workout is open. Start opens SH-03.
- **States:** each tab selected; workout bar shown; Start open (+ turned to ×, SH-03 over the
  screen); a pushed screen with the standard back chevron; Reduce Transparency (solid surface).
- **Source:** #14 (chosen navigation, deviation: Start is an action in a tab bar); #31; ADR 0003
  (screens run under the glass bar).
- **Built:** `App.kt` (TabsScreen; tabs as text, Start a plain primary circle).

**SH-02 · Home**
- **Purpose:** today: what is planned, the week so far, food later.
- **Shows / actions:** large title "Home" and the date; card "Planned today" with the template
  name, exercises · "about 30 min" and one filled **Start Push A**; this week's numbers
  (workouts done, km run); a warning card when the last run has a GPS gap ("Last run saved without
  GPS for 0.4 km. The route has a gap."); the latest activity as a row; "Food today — comes with
  nutrition" with a disabled "Set a target".
- **States:** a strength day; a run day (card offers the run); a rest day; today done (card shows
  Show workout); a workout in progress (card says Continue Push A · 24:13); no programme and no
  templates (first use: Create template / Start empty workout); GPS-gap warning.
- **Source:** #14 (Home is today; Home › Start Push day is 1 tap), #25 scope; token preview on
  `docs/design/15-tokens/index.html` (Preview); #14 grey wireframe `e_home`.
- **Built:** placeholder only (`App.kt`, Placeholder).

**SH-03 · Start bubbles**
- **Purpose:** begin any workout or run from any tab.
- **Shows / actions:** + turns to ×; four round bubbles spring up in an arc over Start, one after
  another (40 ms stagger), labels under them, in one nearly clear panel with a soft shadow sized
  to what it holds; the screen stays in view. Bubbles: today's workout (filled, "Push A"),
  Empty workout, From template (→ SH-04), Run (→ RU-01). A tap outside or × closes.
- **States:** today planned; first use (Create template replaces today's bubble); a workout in
  progress (one bubble only: "Continue Push A · 0:42"; two workouts never run at once); Reduce
  Motion (bubbles appear without moving); today is a rest or run day (open point O-08).
- **Source:** #17 Q46, choice 14c (N4), tokens `motion-stagger`, `state-veil-opacity`; sketch
  choice 14c; components catalogue "Start bubbles".
- **Built:** partly: `App.kt` opens a Material bottom sheet with Workout and Run, or Continue.

**SH-04 · Start › From template**
- **Purpose:** start any other template in a third tap.
- **Shows / actions:** the bubbles swap for the templates as round bubbles, three across, last
  done first, Back first; scrolls when there are many. A tap starts that template (WO-02 state 1).
- **States:** 4 templates; many templates (scrolling); none (only Create template).
- **Source:** #17 choice 14c; sketch choice 14c "Från mall".
- **Built:** not built.

**SH-05 · Food tab**
- **Purpose:** hold nutrition's place so filling it later moves nothing.
- **Shows / actions:** title "Food", a card "Nutrition comes later". No actions.
- **States:** v1 placeholder only. The later screens are EL-17–EL-19.
- **Source:** #14 (Food tab, deviation: empty tab), #9.
- **Built:** placeholder (`App.kt`).

**SH-06 · Onboarding**
- **Purpose:** a short welcome journey on first launch that leads straight into the app.
- **Shows / actions:** says Carl built the app; shows how the app is used and a few of its
  functions (log a set in one tap, the week in Plan, runs with GPS); asks only what the first use
  needs (units kg or lb; body data optional); Skip on every page; ends on "Create your first
  template" and "Start an empty workout". Permissions are not asked here (SH-08).
- **States:** each page; skipped (lands on Home, first-use states); finished. Page count and split
  are open (O-04).
- **Source:** #25 (decided: short, skippable, teaches in context, permissions when needed; Carl
  2026-10-22 in #17 Q43); #14 inventory (units, body data).
- **Built:** not built.

**SH-07 · First-use hints in context**
- **Purpose:** teach a function where it is first used, instead of a long tour.
- **Shows / actions:** a dismissible hint at the first use of: the set's ✓ ("logs the hint as it
  stands"), the dock, swipe to delete, ⌄ minimise, Start's bubbles.
- **States:** shown once, dismissed. Which hints exist is open (O-04).
- **Source:** #25 ("teaches in context rather than through a long tour").
- **Built:** not built.

**SH-08 · Permission prompts**
- **Purpose:** ask for a permission at the moment it is needed.
- **Shows / actions:** location before the first run (RU-01), notifications before the first rest
  ends with the phone locked (WO-18), Apple Health when linking (EL-09). Each: Strive's own line
  why, then the system dialog; a denied state with a way to Settings.
- **States:** before asking; system dialog; granted; denied.
- **Source:** #25 ("asks for permissions only when they are needed"), #43, #5, #52.
- **Built:** not built.

### Strength workout

**WO-01 · Active workout, empty**
- **Purpose:** an empty workout asks what you train and adds it in one tap.
- **Shows / actions:** F1 title bar (⌄, ⏱, "Workout" with ⋯, clock "0:12", Finish); a card with a
  round + mark, "What are you training today?", "Pick from the library, or tap an exercise below.",
  filled **Add exercises** (→ EX-03); "Quick picks" with a caption: exercises marked Yes in In my
  programmes, else "Recently done", each a chip "+ Bench Press BB" that adds it in one tap;
  "Discard workout" in red text.
- **States:** quick picks from marks; quick picks from history; no marks and no history (no quick
  picks); after the first quick pick (becomes WO-02 with that exercise in focus).
- **Source:** decision 40 (canvas C-tomt); #17 Q47; #42.
- **Built:** `workout/Focus.kt` (StartPrompt, QuickStart), `workout/WorkoutScreen.kt`.

**WO-02 · Active workout, exercise in focus**
- **Purpose:** log the next set in one tap, one-handed, while the workout stays in view.
- **Shows:**
  - F1 title bar: ⌄ minimise (WO-17), ⏱ free rest (WO-07), **Finish** on the right (filled once
    every set is logged, WO-15).
  - Large header: "Push A" with a ⋯ pill (WO-08); "Mon 9 Nov", the one clock "24:13", "2 of 15 sets".
  - Focus card: "NOW · BENCH PRESS BB · SET 2 OF 3" (a kind word "· WARM-UP", "· DROP SET",
    "· TO FAILURE" in its kind colour; in a superset "NOW · A · INCLINE PRESS DB · SET 1 OF 3");
    the values large, "82,5 kg × 8 reps" (or time, distance, "+10 kg"); "up 2,5 kg" or "down";
    "rest 2:00 after"; "Previous 80 × 8 @3"; RIR segments 0 · 1 · 2 · 3 · 4+ without a label;
    **Edit** (opens WO-04) and filled **✓ Log set 2**.
  - The exercise in focus, open: name in teal (a tap folds it), ⋯ pill (WO-09), fold chevron;
    under it in grey the sticky note (pin), the plan's cue (Plan) and last time's note (Previous);
    today's note as a field when opened. Column labels Set · Previous · kg · Reps · ✓ (per type:
    Time, m, +kg). Set rows: badge (number, W, D, failure number in coral; tap → WO-14),
    Previous "80 × 8 @3", value fields (a tap opens the dock with the keypad on that field), ✓
    (outlined on the next set, filled when logged); logged reps read "8 @2" with RIR small; the
    next row tinted; a plates line under the next barbell set, drawn and as text "25 + 5 + 1,25
    per side"; a star on a logged set that set a record; rest lines "Rest 2:00" between sets (teal
    after the last logged; a tap opens the dock on that rest); "+ Add set (2:00)".
  - Superset: label "Superset · rest 1:30 after each round", letter tags A/B on headers and rows,
    an edge line at the left, one list in lifting order (A 1, B 1, rest, A 2, B 2…).
  - Folded exercises: one shared overview card, a row each with name, a dot per set (ring, filled
    in its kind colour when logged), "0/3" or "4/4 ✓", and an expand chevron; a skipped one says so.
  - End of list: "+ Add exercises" (EX-03) and "Discard workout" in red (WO-16).
- **Actions:** ✓ on a row logs its hint (1 tap); Log set on the card; an RIR tap (2 taps); Edit
  or a tap on a set opens WO-04; a set row swiped left shows Delete, swiped past half deletes;
  badge → WO-14; name or chevron folds and unfolds; scroll → WO-03.
- **States:**
  1. Started from a template, no sets logged ("0:12", "0 of 15 sets", focus on the warm-up).
  2. Sets logged, focus mid-exercise (canvas D-pass).
  3. Resting: WO-06 floats over the list after Log; the rest line after the logged set is teal.
  4. Exercise done, focus jumps on: it folds into the overview with "4/4 ✓", the next opens (D-next).
  5. A superset in focus; its rest starts only once every set of the round is logged, in any order.
  6. Dock open (WO-04); keypad open (WO-05).
  7. Menus open: workout ⋯ (WO-08), exercise ⋯ (WO-09), badge (WO-14).
  8. A row mid-swipe showing Delete.
  9. Warm-up, failure and drop set in focus, each with its kind word and colour.
  10. Bodyweight (Tricep Dips "+10 × 10"), time (Plank "0:45") and distance rows.
  11. A logged set tapped: dock with "Save change".
  12. A record just set: star on the row.
  13. All sets logged: Finish filled.
  14. An exercise skipped.
  15. A new exercise with no history: Previous "–", empty fields, ✓ waits for a value.
  16. Restored after the app was killed: the same screen, every logged set intact.
- **Source:** #17 Q1–Q23, choices 1, 1b, 3, 4, 5, 6 and their corrections; decision 39; Carl
  2026-11-03 (kind colours); #42, #45 (hints), #46 (stars), #48 (plates). Canvas D-pass, D-next.
  Catalogue: logging dock, workout title bar, superset.
- **Built:** partly: `workout/WorkoutScreen.kt`, `workout/Focus.kt` (LargeHeader, FocusCard, SetRow,
  RestLine, ExerciseOverview), `workout/Parts.kt` (SwipeToDelete). Not built: plates line (#48),
  record stars (#46), the plan's cue (#44), the progression rule (#45: "up/down" now compares the
  planned weight with last time), opening exercise detail from the workout (O-02).

**WO-03 · Active workout, scrolled**
- **Purpose:** keep time, progress and the next set reachable deep in the list.
- **Shows / actions:** the large header folds into the bar: "Push A" with ⋯, "24:13 · 2/15 sets"
  under it, ⌄ ⏱ left, Finish right; under the bar a strip "NOW · BENCH PRESS BB · SET 2" with
  "82,5 kg × 8 · RIR 2". The strip is one button back to the focus card (no Log on it, Carl's
  point 7).
- **States:** strip shown; strip during rest (WO-06 still floats); scrolled to the end of the list.
- **Source:** decision 39; canvas D-scroll (draws Log on the strip; superseded by commit 5226124).
- **Built:** `workout/Focus.kt` (FocusStrip), `workout/WorkoutScreen.kt` (StripOverlay).

**WO-04 · Logging dock**
- **Purpose:** change a set before or after logging it, under the thumb.
- **Shows / actions:** a glass dock rising from the bottom with a grabber (no ✕): weight and reps
  fields side by side (per type: time, distance, added weight), each with − and + at the
  exercise's step; for barbells the bar under the weight with "Bar 20" (menu of bars) and its
  plates drawn per side; a row shared by RIR's five segments (left half) and the rest time with
  − and + by 15 s (right half); filled **Log**. Closes by swiping the grabber down or tapping
  outside.
- **States:** a set not logged (Log); a logged set ("Save change"); opened from a rest line (rest
  field focused, "Save" saves without logging); RIR 4+ tapped (a row 4 · 5 · 6 · 7 · 8 · 9 · 10
  opens, the segment then shows the value); keypad open (WO-05: fields drop their − and +, the rest
  card hides); a heavy bar ("4 × 25 + 1,25", wraps, shrinks); lb exercise.
- **Source:** #17 choices 1, 1b (D3), 2 and its correction and additions, 3 (P3), Q13 change;
  catalogue: logging dock (deviation: RIR segments about 29 × 44 pt).
- **Built:** partly: `workout/Dock.kt` (Dock, NumberField, RirRow, RirSegments, RirMore). Not built:
  the bar and plates (#48).

**WO-05 · Keypad**
- **Purpose:** Strive's own number keypad inside the dock.
- **Shows / actions:** a row of small actions on top: Hide, Plates (weight field only), then − and +
  (44 pt, larger); digits on glass keys in three columns, the locale's decimal comma and ⌫; **Next**
  full width in Log's place, walking weight → reps → rest; on the rest field it reads **Log ›**,
  which logs like Log, closes the dock and starts the rest.
- **States:** weight field; reps field; rest field ("Log ›"); Plates: digits swap for plate keys
  25 · 20 · 15 · 10 · 5 · 2,5 · 1,25, Empty and ⌫, plates drawn as keyed, heaviest innermost;
  a field opened fresh is replaced by the first key.
- **Source:** #17 Q16, choice 2 (K3), its correction, choice 3; catalogue: logging dock (keypad, plates).
- **Built:** partly: `workout/Dock.kt` (Keypad, Key). Not built: Plates keys (#48).

**WO-06 · Rest timer card (T5)**
- **Purpose:** count down rest wherever the list is scrolled.
- **Shows / actions:** a glass card floating over the list, inset from the edges: a 64 pt amber
  ring, the time "1:43" in display size, round − and + with "15 s" between them, **Skip** as its
  own capsule far right. Ends with a sound and a buzz.
- **States:** counting after a logged set; a superset round's rest; a free rest (from WO-07); last
  seconds; ended (buzz, card leaves); hidden while the keypad is open; Reduce Motion (only the digits
  change).
- **Source:** #16 Q7 (T5 in glass, K2); #17 Q10, Q17; #43.
- **Built:** `workout/Rest.kt` (RestCard).

**WO-07 · Free rest menu**
- **Purpose:** start a rest that no set started.
- **Shows / actions:** from ⏱: a pull-down menu "Free rest" with 0:30, 1:00, 2:00, 3:00 and
  "Custom time…", which opens a prompt ("Minutes and seconds, such as 1:30").
- **States:** menu open; custom-time prompt; a rest already running (the choice replaces it).
- **Source:** #17 Q17, choice 5.
- **Built:** `workout/Rest.kt` (RestButton), `workout/Parts.kt` (TextPrompt).

**WO-08 · Workout menu**
- **Purpose:** the workout's own settings.
- **Shows / actions:** ⋯ by the workout's name opens a pull-down: Rename, Workout note, Change start
  time. Rename and the note open prompts (the note takes dictation from the keyboard); start time
  opens a time picker.
- **States:** menu open; rename prompt; note prompt (empty, filled); start-time picker.
- **Source:** #17 Q11, Q19, choice 4.
- **Built:** `workout/Focus.kt` (WorkoutMenu), `workout/Parts.kt` (TextPrompt).

**WO-09 · Exercise menu**
- **Purpose:** everything you do to one exercise mid-workout.
- **Shows / actions:** a pull-down from the exercise's ⋯: two tiles on top, **Note** (opens today's
  note field under the header) and **Swap** (→ EX-04); rows: Sticky note (prompt; shown every time,
  in every template), Rest for all sets › (submenu 1:00, 1:30, 2:00, 2:30, 3:00 with a check),
  Create superset (→ WO-10) or, inside one, Remove from superset, Order and skip (→ WO-11),
  Exercise settings (→ WO-12, weight exercises), and **Remove exercise** in red last.
- **States:** menu open; rest submenu open; inside a superset; a non-weight exercise (no settings row).
- **Source:** #17 Q15, choice 4 (M2 without the warm-up tile); commit 5226124.
- **Built:** `workout/WorkoutScreen.kt` (ExerciseHeader, MenuTile).

**WO-10 · Create superset**
- **Purpose:** pair this exercise with another in the workout.
- **Shows / actions:** a dialog listing the workout's other exercises; a tap joins them, the partner
  moves next to it.
- **States:** list; one exercise left (row hidden from WO-09 when only one exercise exists).
- **Source:** #17 Q8, choice 4; commit 4c06f7d.
- **Built:** `workout/WorkoutScreen.kt` (SupersetDialog).

**WO-11 · Order and skip**
- **Purpose:** reorder the workout and skip an exercise.
- **Shows / actions:** the exercises in order, each with move up and move down and Skip / Include.
  A skipped exercise stays recorded, its unlogged sets dropped at finish.
- **States:** default; one skipped; a superset (moves as a group).
- **Source:** #17 Q8, Q15.
- **Built:** `workout/WorkoutScreen.kt` (OrderDialog).

**WO-12 · Exercise settings (in the workout)**
- **Purpose:** change step, bar and unit without leaving the workout.
- **Shows / actions:** Step (2,5 / 2 / 4 / 5 kg…), Bar (Olympic 20, Short 15, EZ 7,5, Hex 25,
  None 0), Unit (kg, lb).
- **States:** barbell exercise; dumbbell (no bar); lb.
- **Source:** #17 Q7, Q15, Q22, Q23.
- **Built:** partly: `workout/WorkoutScreen.kt` (SettingsDialog: step and bar). Unit not built.

**WO-13 · Set type menu**
- **Purpose:** change a set's kind from its badge.
- **Shows / actions:** a pull-down from the badge: Warm-up, Working set, Drop set, To failure, each in
  its kind colour with a check on the current one. An unlogged set made a warm-up moves up to be
  the next set to do.
- **States:** working set selected; warm-up selected; on a logged set (the set stays put).
- **Source:** #17 Q21, choice 4; Carl 2026-11-03; catalogue: pull-down menu (set type).
- **Built:** `workout/WorkoutScreen.kt`, `workout/Focus.kt` (SetRow).

**WO-14 · Swipe to delete a set**
- **Purpose:** remove a set as Mail removes a message.
- **Shows / actions:** a short swipe left reveals a red **Delete**; a tap deletes; past half the row
  deletes in one motion. A swipe never counts as a tap.
- **States:** closed; revealed; full swipe deleting.
- **Source:** #17 choice 4 ("Also chosen"), commit 6843ebb.
- **Built:** `workout/Parts.kt` (SwipeToDelete).

**WO-15 · Finish sheet**
- **Purpose:** end the workout when some sets are not logged.
- **Shows / actions:** an action sheet whose header counts them ("2 sets not logged"): **Log them as
  they are and save**, **Save without them**, **Discard workout** (red, no alert after it),
  **Continue workout**. With every set logged, Finish saves and opens WO-21 directly.
- **States:** one unlogged set (singular); several; all logged (no sheet).
- **Source:** #17 Q12, Q20, choice 5.
- **Built:** `workout/WorkoutScreen.kt` (ActionSheet).

**WO-16 · Discard workout alert**
- **Purpose:** confirm discarding from the list's own Discard workout.
- **Shows / actions:** "Discard the workout?" "Every set in it is deleted." Cancel, Discard (red).
- **States:** from Discard workout; from Finish on a workout with no sets at all.
- **Source:** #16 alerts rule; #17 Q19.
- **Built:** `workout/WorkoutScreen.kt` (Alert).

**WO-17 · Minimised workout bar**
- **Purpose:** keep a workout running while Carl uses the rest of the app.
- **Shows / actions:** a glass bar above the tab bar: workout name, elapsed time, and the rest
  countdown while resting. A tap returns to the workout, which rises from the bottom (Music's
  player). ⌄ in the title bar sends it here.
- **States:** no rest; resting; after the app was killed (a workout left open is offered here);
  Reduce Motion (fades instead of rising).
- **Source:** #17 Q19, choice 5; #42.
- **Built:** `workout/WorkoutScreen.kt` (MiniBar), `App.kt`. Dragging the workout down is not built
  (only ⌄).

**WO-18 · Rest notification (locked phone)**
- **Purpose:** say rest is over when the phone is locked.
- **Shows / actions:** a local notification: "Rest is over · Bench Press BB · set 3 of 3 next".
  A tap opens the workout.
- **States:** lock screen; banner while another app is open.
- **Source:** #17 Q10; #43.
- **Built:** not built.

**WO-19 · Live Activity: lock screen**
- **Purpose:** the rest countdown on the lock screen, like the Clock app's timer.
- **Shows / actions:** workout name, the countdown, the next set ("Next: Bench Press BB · 82,5 × 8"),
  elapsed time.
- **States:** counting; ended; workout open without rest (if the spike allows; else the activity
  ends with the rest); not available with a free Apple ID (the notification stands alone).
- **Source:** #17 Q10, decision 33; #43 (a spike decides).
- **Built:** not built.

**WO-20 · Live Activity: Dynamic Island**
- **Purpose:** the same countdown in the Dynamic Island.
- **Shows / actions:** compact (ring + "1:43"), minimal (ring), expanded (name, countdown, next set).
- **States:** compact; minimal; expanded; ended.
- **Source:** decision 33; #43.
- **Built:** not built.

**WO-21 · Workout summary**
- **Purpose:** what the workout was, and the two questions to answer while it is fresh.
- **Shows / actions, top to bottom:** date heading "Monday 9 November", with "17:02–18:06 · week 6 ·
  Hypertrofi, block 1" under it (the clock time and week · block); four tiles **Time** "64 min", **Avg RIR** "1,5" with "goal 2–3" and "13 of 14
  sets", **Sets** "15", **Records** "3"; the records listed ("Bench Press BB · 82,5 × 8 · +2,5 kg");
  "How hard was it? RPE" as ten segments 1–10 (deviation: more than five segments); the workout
  note (energy, sleep, pain go here); the exercises folded, one row each with its best set against
  last time ("82,5 × 8 · ↑ +2,5 kg vs last"), a tap shows every set with records starred; Started,
  Ended (editable), Week; **Done**.
- **States:** with records; no records ("–"); RIR on some sets ("13 of 14 sets"); no RIR ("–");
  an exercise expanded; not from a template (no goal under Avg RIR); an empty-template workout
  offering **Save as template** (WO-23); Done → WO-22 when the workout differs from its template;
  Done returns to where the workout started.
- **Source:** #17 Q28, Q29, choice 7 (U1 top + U3 exercises), decision 32 (no volume tile); #42, #46.
- **Built:** partly: `workout/SummaryScreen.kt` (tiles, RPE, note, folded exercises, start and end).
  Not built: records (shows "–"), editable times, week · block from the programme, Save as template.

**WO-22 · Update template alert**
- **Purpose:** carry a changed structure back into the template.
- **Shows / actions:** "Update the template Push A?" listing the changes ("Added Tricep Dips",
  "Rest 1:30 → 2:00 on Incline Press DB"); structure and rest count, weights do not. Update, Keep
  as it is.
- **States:** one change; several.
- **Source:** #17 Q29, choice 7; #44.
- **Built:** not built.

**WO-23 · Save as template**
- **Purpose:** turn an empty or past workout into a template.
- **Shows / actions:** a prompt for the name (prefilled with the workout's), Save, Cancel; the template
  appears in Plan's Templates.
- **States:** from the summary of an empty workout; from session detail's edit mode.
- **Source:** #17 Q29, Q30.
- **Built:** not built.

**WO-24 · Session detail**
- **Purpose:** one past workout, as logged; the summary reopened from History.
- **Shows / actions:** the same screen as WO-21, with a back chevron and **Edit** instead of Done;
  every set as logged with badge, weight × reps @RIR, record stars and that day's exercise note.
- **States:** strength workout; a run opens RU-05 instead; with records; with a note.
- **Source:** #17 Q29, Q30; #14 inventory.
- **Built:** partly: `workout/SummaryScreen.kt` (after = false). Edit not built.

**WO-25 · Session detail, editing**
- **Purpose:** correct a past workout.
- **Shows / actions:** Edit turns the screen into the active workout's rows and keypad: change sets,
  add or remove exercises, correct start and end; at the bottom **Do it again** (starts a new
  workout with the same exercises and targets), **Save as template** (WO-23), **Delete workout** in
  red with an alert. Cancel and Done in the bar.
- **States:** editing; delete alert; unsaved changes on Cancel.
- **Source:** #17 Q30.
- **Built:** not built.

**WO-26 · History**
- **Purpose:** what Carl did, by week, like a diary.
- **Shows / actions:** reached from You › All activities; title "All activities" with back;
  segments All · Strength · Running; weeks as groups whose header sums the week ("Week 45 ·
  4 workouts · 22,3 km"); each row led by weekday and date in a column, an icon for workout or run,
  the name and start time, then minutes · sets · volume for a workout ("58 min · 14 sets ·
  3 916 kg") or km and pace for a run, and a star with the record count. A tap opens WO-24 or RU-05.
  No search and no calendar.
- **States:** All; Strength; Running; first use ("No workouts yet" with Start a workout); a week
  with an extra workout; long list.
- **Source:** #17 Q44, Q47, choice 14 (H3, volume stays); #14 inventory; #18 (run rows).
- **Built:** partly: `workout/HistoryScreen.kt` (strength rows, weeks, empty state). Not built:
  segments, runs, record stars.

### Plan

**PL-01 · Plan tab**
- **Purpose:** the week ahead, the programme that frames it, the templates and the library.
- **Shows / actions, top to bottom:**
  - Title "Plan" with ⋯ (PL-02) and + (new template → PL-05); **Today** beside ⋯ once Carl leaves
    this week.
  - Week card (W2): the programme's name (→ PL-08); the week title "Week 6 of 8" with ‹ ›; under
    it "Week 46 · 9–15 Nov" and the type when calibration or deload; the programme's weeks as a bar
    (K calibration, D deload); the seven days Monday to Sunday, each with its plan and ✓ done,
    struck through when missed, "Extra" for a workout outside the plan, a ring for today, greyed
    "Dropped" under Short week; the deload line ("6 weeks without a deload", from week 8 after at
    least 4 such weeks "Deload recommended" with **Deload now**). A swipe on the days changes week.
  - Day card: today's template open, with **Start workout** (one tap) and Edit; a done day shows
    Show workout; a rest day or run day says so; another tapped day shows its plan here. Its ⋯ has
    Move to (PL-02).
  - Templates heading with ⋯ (Show archived → PL-07), then a row of large cards you swipe: name,
    muscles, number of exercises, "about 30 min", last done, ⋯ (PL-03). A card zooms open to PL-04.
    New template at the end of the row. A separate heading for templates outside the programme.
  - Rows: Exercise library (553 exercises) → EX-01; "Import from Strong — comes with import"
    disabled.
- **States:** this week; another week (Today shown); deload recommended (sample: "Week 9 of 12");
  a deload week; a calibration week; Short week on; a missed day and an extra; today done; today a
  rest day; today a run day; first use (no programme: "No programme" with Create programme, the
  week still on calendar weeks, the day card offers an empty workout; no templates: "No templates
  yet" with Create template and Start empty workout).
- **Source:** #17 Q38–Q41, Q47, choices 8 (and correction), 13 (W2); #47; #14 (Plan becomes Coach
  later); sketch "First use on Plan"; catalogue: week card, template cards and today's card.
- **Built:** placeholder with a Library button only (`App.kt`).

**PL-02 · Plan menus**
- **Purpose:** change the week without leaving Plan.
- **Shows / actions:** Plan's ⋯: Short week (greys out Push B, then the easy run, moving nothing),
  Deload now, Week type › Normal / Calibration / Deload. The day card's ⋯: Move to…, listing this
  week's free days from today on. Templates ⋯: Show archived.
- **States:** each menu open; Short week on (its row checked); Move to list; a moved session on its
  new day.
- **Source:** #17 Q39, Q40, choice 13.
- **Built:** not built.

**PL-03 · Template card menu**
- **Purpose:** act on a template without opening it.
- **Shows / actions:** from a card's ⋯ or the preview's ⋯: Edit, Duplicate, Share (system share
  sheet), Archive, Delete in red (alert: "Delete Push A?").
- **States:** menu open; delete alert; share sheet; archived (card leaves the row).
- **Source:** #17 Q27, choice 8 (Dela and Arkivera added by Carl).
- **Built:** not built.

**PL-04 · Template preview**
- **Purpose:** see a template in full and start it.
- **Shows / actions:** the card zoomed open to full screen: name, muscles, tiles for exercises, sets
  and minutes, last done, the template note ("Kontrollerad nedfirning, två sekunder."), the exercises
  in order with their target ("1 warm-up + 3 × 6–8 @ 2–3") and an info button (→ EX-05), the
  superset with its edge line and A/B; ⋯ (PL-03); **Start workout** at the bottom; close.
- **States:** a template with a superset; never done; opened from a day in the week card (PL-01) or
  from a card.
- **Source:** #17 Q25, Q27, choice 8.
- **Built:** not built.

**PL-05 · Template editor**
- **Purpose:** build and change a template with the workout's own rows.
- **Shows / actions:** Cancel and Done in the bar; the name with its own ⋯ (rename, template note);
  per exercise: name, a Set / Previous / Reps / RIR grid (badge = role: W, number, D, F, or top set
  and back-off), optional target weight, a rest line between sets, "+ Add set (2:00)" (a new set
  copies the one above), the progression switch (on by default), the cue; "Add exercises" (EX-03);
  reorder by drag; **Delete template** in red at the bottom; estimated time "about 30 min".
- **States:** a new empty template; editing Push A; keypad open (PL-06); Cancel with changes
  ("Discard changes?"); delete alert; a superset.
- **Source:** #17 Q25–Q27, choice 8 (T4's editor); #44; catalogue: template editor.
- **Built:** not built.

**PL-06 · Template keypad**
- **Purpose:** type rep and RIR ranges.
- **Shows / actions:** K3 with the decimal comma replaced by the range key "–": 8, –, 1, 0 gives
  "8–10"; − and + move both ends; a range typed backwards turns round; **Same for the rest** beside
  Next copies the set's reps and RIR to every set below it; Next walks reps → RIR → next set, Done on
  the last field.
- **States:** reps field; RIR field; after Same for the rest.
- **Source:** #17 Q26, choice 9 (S1); catalogue: template editor (range keypad).
- **Built:** not built.

**PL-07 · Archived templates**
- **Purpose:** find and restore an archived template.
- **Shows / actions:** a list of archived templates with last done; a row's menu: Restore, Delete.
- **States:** list; empty.
- **Source:** #17 choice 8 ("Mallar's own ⋯ holds Visa arkiverade").
- **Built:** not built.

**PL-08 · Programme · Weeks**
- **Purpose:** the block, week by week.
- **Shows / actions:** opened from the programme's name; bar with ‹ and ⋯ (Rename, Duplicate,
  Archive); the name, start "5 Oct 2026", length "8 weeks", the current week; tabs **Weeks · Weights
  · Log · Settings**. Weeks: the standard week ("Std") and weeks 1–8 as thin rows: number, K or D,
  "now" on this week, a miniature of the seven days, a day that differs from the standard week
  outlined. A tap opens one week: its type as a segmented control (Normal, Calibration, Deload) and
  its days full size, each a row with a menu (the templates, Run, Rest). This week starts open.
- **States:** this week open; another week open; the standard week open; a day changed; week 8 made a
  deload; a menu open.
- **Source:** #17 Q38, Q39, Q42, choice 13b (B5); #47; catalogue: weeks that open.
- **Built:** not built.

**PL-09 · Programme · Weights**
- **Purpose:** how the main lifts moved during the block.
- **Shows / actions:** each main lift's top set at the block's start and now, from the log ("Squat BB
  100 → 107,5 kg").
- **States:** filled; a new programme ("Log a workout to see your weights").
- **Source:** #17 Q42, choice 13b.
- **Built:** not built.

**PL-10 · Programme · Log**
- **Purpose:** dated notes about the block.
- **Shows / actions:** New note, then the notes newest first with their dates.
- **States:** notes; empty; writing a note.
- **Source:** #17 Q42, choice 13b.
- **Built:** not built.

**PL-11 · Programme · Settings**
- **Purpose:** the programme's frame.
- **Shows / actions:** Start date, Length (weeks, or No fixed length), Deload ("recommended after 4–6
  weeks, earliest week 8"), Short week order (the days in the order they drop), Archive programme.
- **States:** fixed length; no fixed length; date picker open; archive alert.
- **Source:** #17 Q38, Q39, choice 13b.
- **Built:** not built.

**PL-12 · Create programme**
- **Purpose:** make a programme in the app.
- **Shows / actions:** name, start date, length or no fixed length, the standard week (a row per
  weekday: a template, a run with type and target, or rest), calibration weeks; Save.
- **States:** empty; filled; no templates yet (the weekday menus offer Run and Rest only). Form is
  open (O-11).
- **Source:** #17 Q38 ("created and edited in the app in v1"), Q47 (Create programme).
- **Built:** not built.

### Exercises

**EX-01 · Exercise library**
- **Purpose:** find any of the 553 exercises.
- **Shows / actions:** back, title "Library", ⋯ (sort: A–Z, Most used, Last used), + (EX-09); search
  over names and aliases with a filter button beside it (filled while a filter is on → EX-02);
  Carl's movement patterns as chips (Horizontal press, Vertical press, Horizontal pull, Vertical
  pull, Knee dominant, Hip dominant, Arms, Core, Calves, Kettlebell, Explosive and carries) plus
  Mobility and Conditioning; one list with a heading per primary muscle that sticks while its group
  scrolls; a row: the illustration's crop, name, pattern · equipment, last performance ("82,5 × 8"),
  the In my programmes mark as a small badge (none when unmarked; No hidden). A row opens EX-05.
- **States:** default; searching; a chip on; a filter on; no match ("No exercises match"); Mobility
  chip (stretches); sort menu open; no illustration (placeholder); no history (no last performance).
- **Source:** #17 Q34, choices 11 (L1), 12b (R1); decisions 36, 37; #41; catalogue: library list.
- **Built:** partly: `library/LibraryScreen.kt`, `library/LibraryFilter.kt`. Not built: sort, +,
  last performance, row images.

**EX-02 · Library filter sheet**
- **Purpose:** filter by equipment and mark.
- **Shows / actions:** a sheet with Clear and Done in the head; Equipment chips (Barbell, EZ bar,
  Dumbbells, Kettlebell, Cable, Machine, Bodyweight, Band, Other; several at once); In my programmes
  chips (Yes, Swap, Test, Not set, No; No unticked by default). The list updates behind it.
- **States:** nothing ticked; Dumbbells ticked; No ticked.
- **Source:** #17 Q34, choice 12b.
- **Built:** `library/LibraryScreen.kt` (FilterSheet).

**EX-03 · Exercise picker (Add exercises)**
- **Purpose:** add several exercises to a workout or template at once.
- **Shows / actions:** EX-01's list in picking mode: Cancel and **New** (→ EX-09) in the bar, a check
  per row, and a glass bar with **Superset (n)** and **Add (n)**.
- **States:** none ticked; one ticked (Superset needs two); three ticked; searching while picking.
- **Source:** #17 Q35, choice 11; commit 35c2956.
- **Built:** partly: `library/LibraryScreen.kt` (pick mode). New not built.

**EX-04 · Swap exercise**
- **Purpose:** swap a busy machine for an alternative mid-workout.
- **Shows / actions:** the picker in single-choice mode with an "Alternatives" section first: the
  exercise's Swap alternative and same-pattern exercises; a tap swaps it, keeping the sets.
- **States:** with alternatives; none.
- **Source:** #17 Q8, Q15; commit 5226124.
- **Built:** `library/LibraryScreen.kt` (swapFor).

**EX-05 · Exercise detail · About**
- **Purpose:** how to do it and how the exercise is set up.
- **Shows / actions:** the name with ⋯ (Edit → EX-10); segments **About · History · Chart · Records**;
  About: the illustration (start and end position, worked muscles red, primary strong, secondary
  light), In my programmes with a pull-down and a line saying what it does (Seated Row Cable: "Swap:
  the alternative, Single Row DB, is suggested first."), numbered instructions and 1–2 tips in the app's language,
  primary and secondary muscles, equipment, the sticky note, and the settings (type, per side, step,
  bar, unit).
- **States:** as a screen from the library; as a sheet from the workout (O-02); no illustration yet
  (text first); mark menu open; each mark.
- **Source:** #17 Q32, Q36 (changed), choice 10 (X1) and its settled points; decisions 30, 31; #41.
- **Built:** `library/ExerciseScreen.kt` (About, MarkRow).

**EX-06 · Exercise detail · History**
- **Purpose:** every workout with this exercise.
- **Shows / actions:** a card per workout: date, template, estimated 1RM, sets with badges and record
  stars; a tap opens WO-24.
- **States:** filled; empty ("Log a workout to see your history").
- **Source:** #17 Q32, Q47; #46.
- **Built:** empty state only (`library/ExerciseScreen.kt`).

**EX-07 · Exercise detail · Chart**
- **Purpose:** progress of one exercise.
- **Shows / actions:** per type: estimated 1RM per workout first, then heaviest set and volume per
  workout (weight × reps); most reps and added weight (bodyweight); most reps; best set (time,
  distance). Period 3 mo · 1 yr · All. A tap shows value and date in the title; a second tap opens
  that workout.
- **States:** each metric; a point selected; empty.
- **Source:** #17 Q33; #16 D1 (grey marks, teal selected); #46.
- **Built:** empty state only.

**EX-08 · Exercise detail · Records**
- **Purpose:** the exercise's records and their history.
- **Shows / actions:** the records for its type (weight × reps: Heaviest weight, Best estimated 1RM,
  Most volume in a set, Most reps at each weight); the best weight for 1–12 reps; the records history.
- **States:** filled; empty; a bodyweight or timed exercise's records.
- **Source:** #17 Q31, Q32; #46.
- **Built:** empty state only.

**EX-09 · Create exercise**
- **Purpose:** add Carl's own exercise.
- **Shows / actions:** a full-height opaque sheet: Cancel, "New exercise", Save (waits for a name and
  a primary muscle). Rows, each a value that opens a pull-down: Name, Primary muscle, Secondary
  muscles (a checklist inside the sheet), Pattern, Equipment (fills Step, and Bar for a barbell or EZ
  bar), Type, Per side, Step, Bar, Unit, Instructions, In my programmes with its Alternative, Sticky
  note.
- **States:** empty; a menu open; secondary muscles list; filled (Save on); from the picker's New.
- **Source:** #17 Q35, choice 12 (E1); #48; catalogue: form sheet.
- **Built:** not built.

**EX-10 · Edit exercise**
- **Purpose:** change any exercise, built-in ones included.
- **Shows / actions:** EX-09's form titled with the exercise; Type locked once sets are logged, with a
  line saying why; **Merge data** (→ EX-11); **Archive** (hides it, keeps its history); **Delete**
  only for an exercise never used.
- **States:** a used exercise; an unused one (Delete shown); archived.
- **Source:** #17 Q35, choice 12.
- **Built:** not built.

**EX-11 · Merge data**
- **Purpose:** fold a duplicate into the kept exercise.
- **Shows / actions:** a list of exercises to merge into, then an alert ("Move every set from Bench
  Press to Bench Press BB?").
- **States:** list; alert; done.
- **Source:** #17 Q35, choice 12; #48.
- **Built:** not built.

### Running

**RU-01 · Run start**
- **Purpose:** get a GPS fix and start.
- **Shows / actions:** full screen without the tab bar: a map at the current position; GPS state
  ("Looking for GPS…" with a spinner, then "GPS ready"); today's planned run if any ("Easy run ·
  8 km"); the shoes (default for runs, change → EL-12); **Start run**; close.
- **States:** searching; ready; weak signal; location not allowed (SH-08); from the Start bubble or
  from Plan's run day.
- **Source:** #14 inventory (Run start), #5, #18.
- **Built:** placeholder (`App.kt`, Session).

**RU-02 · Live run**
- **Purpose:** read the key number at a glance, at arm's length, while running.
- **Shows / actions:** full screen: the one number Carl names as most important in the largest type
  (O-05); distance, pace, time; the route drawn on a map; **Pause**, **Lap**. Keeps recording with
  the screen locked.
- **States:** running; a lap just taken; GPS lost (the route gets a gap); screen locked; map
  enlarged or hidden (O-06).
- **Source:** #14 (Live run: map, pace, distance, time, pause, lap), #18 Design and Acceptance, #5.
- **Built:** not built (the spike `spike/` recorded GPS only).

**RU-03 · Live run, paused**
- **Purpose:** pause and decide.
- **Shows / actions:** the clock stopped and marked Paused; **Resume** and **Finish**.
- **States:** paused.
- **Source:** #14, #18 ("pause and stop").
- **Built:** not built.

**RU-04 · Finish run**
- **Purpose:** save or discard the run.
- **Shows / actions:** an action sheet: Save run, Discard run (red), Resume.
- **States:** shown; a very short run (open whether to warn).
- **Source:** #18 scope (stop); the workout's Finish pattern (#17 Q20).
- **Built:** not built.

**RU-05 · Run summary and run detail**
- **Purpose:** the run, like Strava: the route on top, the numbers below.
- **Shows / actions:** the route map fills the top (a tap → RU-07; a play button → RU-08); a sheet
  under it: title ("Morning Run"), date, time and place; a two-column stat grid: Distance, Avg pace,
  Moving time, kcal, Avg heart rate, Max heart rate; the device; the shoes; records set, starred;
  the note; Done after the run, Edit (RU-09) from History. Scrolls into RU-06.
- **States:** just finished (Done); from History (back, Edit); no heart rate (HR tiles and section
  hidden); a run with a GPS gap; a record set; a run synced from a watch (later, #27).
- **Source:** #18 comment (Carl, 2026-10-23: very like Strava), #17 choice 14 (runs set records);
  #14 inventory (Run summary: route, splits, pace chart).
- **Built:** not built.

**RU-06 · Run summary, sections**
- **Purpose:** the run's charts and splits below the stats.
- **Shows / actions:** sections, each a chart against distance with its numbers under it:
  **Splits** (a row per km: km, pace, elevation change, a bar per split; the fastest marked);
  **Elevation** (gain, max); **Pace** (avg pace, moving time, avg pace over total time, total time,
  fastest split); **Heart rate** (avg, max); Segments when they exist. A tap on a chart shows the
  value at that point.
- **States:** each section; no heart rate; a split selected.
- **Source:** #18 comment; #14 inventory; #16 D1 chart style.
- **Built:** not built.

**RU-07 · Map, full screen**
- **Purpose:** look at the route closely.
- **Shows / actions:** the route full screen; map type Standard, Satellite, Hybrid; 3D; Save route;
  a map-data sources sheet; close. Controls on glass over the map (deviation: glass in the content
  layer).
- **States:** each map type; 3D; sources sheet open.
- **Source:** #18 comment; #15 G2 deviation. Scope for v1 open (O-06).
- **Built:** not built.

**RU-08 · Flyover replay**
- **Purpose:** replay the route in 3D.
- **Shows / actions:** the route animated in 3D with live speed, elevation and distance, a scrubber
  and a speed control (1x).
- **States:** playing; paused; scrubbing; Reduce Motion (O-06).
- **Source:** #18 comment ("flyover and 3D cost the most"). Scope for v1 open (O-06).
- **Built:** not built.

**RU-09 · Edit run**
- **Purpose:** correct a run's details.
- **Shows / actions:** title, shoes (→ EL-12), note, Delete run in red with an alert; Cancel, Save.
- **States:** editing; delete alert.
- **Source:** #52 (gear changed per activity afterwards); session detail's Edit pattern (#17 Q30).
- **Built:** not built.

### Elsewhere

**EL-01 · You**
- **Purpose:** what is behind: progress, activities, and Carl's own things.
- **Shows / actions:** large title "You" (Carl's name and initials); **Progress** row with a one-line
  summary (→ EL-02); Activities: the latest two (→ WO-24 / RU-05) and **All activities** (→ WO-26);
  **Gear** (→ EL-10; placement open, O-13); Account: **Profile** (→ EL-05), **Settings** (→ EL-06),
  **Export** (→ EL-13); "Import · Sync — later" disabled.
- **States:** filled; first use (no activities: "No workouts yet" with Start a workout).
- **Source:** #14 (You, like Strava; F's `f_you`), #25 scope, #52.
- **Built:** placeholder with an All activities button (`App.kt`).

**EL-02 · Progress · Strength**
- **Purpose:** is Carl getting stronger, and how much has he lifted.
- **Shows / actions, top to bottom:** segments Strength · Running; the total lifted since the start,
  a number ("998,4 t") and a rising curve, with a comparison line that rotates on a tap through 25
  ("Lika tungt som ungefär 166 afrikanska elefanter."); the period 8 wk · 6 mo · 1 yr with a summary
  against the period before (workouts, sets, volume, records); the main lifts, each a tile with
  estimated 1RM, change and a trend line (→ EX-05); volume per week as bars in front of the same
  weeks of the period before, a dashed average named in the legend, D under deload weeks, a tap
  showing the value; working sets per muscle this week against a grey 10–20 band; records from the
  last 30 days.
- **States:** 8 wk, 6 mo, 1 yr; a bar selected; each comparison; first use ("No workouts yet" with
  Start a workout); fewer than four lifts picked.
- **Source:** #17 Q45, Q47, choice 14b (G6 with G5's total); #46; decision 32; catalogue: Progress
  cards.
- **Built:** not built.

**EL-03 · Progress · Running**
- **Purpose:** the running side of progress.
- **Shows / actions:** km per week as bars, the long run per week, run records (longest distance,
  fastest 1, 5 and 10 km, longest time, most climb), the same period control.
- **States:** filled; no runs yet; a bar selected.
- **Source:** #17 Q45, choice 14 ("#18 checks and completes it").
- **Built:** not built.

**EL-04 · Choose main lifts**
- **Purpose:** pick the 3–5 lifts Progress shows.
- **Shows / actions:** a list of exercises with checks, at most five, at least three; Done.
- **States:** three picked; five picked (others disabled). Where it opens from is open (O-14).
- **Source:** #17 Q45 ("3–5 main lifts Carl picks").
- **Built:** not built.

**EL-05 · Profile**
- **Purpose:** body data and goals.
- **Shows / actions:** name; body weight, height, birth year; goals as text (strength, running).
  Edit in place.
- **States:** filled; empty (first use, onboarding skipped). Fields beyond "body data and goals" are
  open (O-15).
- **Source:** #14 inventory (Profile), #25.
- **Built:** not built.

**EL-06 · Settings**
- **Purpose:** the app's settings. No appearance setting: the app follows the phone.
- **Shows / actions:** Language (Follow the phone · English · Svenska); Units (kg, lb); Default rest
  (2:00); Plates and bars (→ EL-07); Keep the screen awake during a workout (switch); GPS accuracy;
  Devices and apps (→ EL-08); About Strive (→ EL-14).
- **States:** default; a menu open; lb chosen.
- **Source:** #14 (Settings; no appearance setting, #15 Q6), #25 (decided items, language), #17 Q10,
  Q22, Q23; #52.
- **Built:** not built (only the settings table, `data/Settings.kt`).

**EL-07 · Plates and bars**
- **Purpose:** the plates Carl's gym has and the bar weights.
- **Shows / actions:** plates as checks: 25, 20, 15, 10, 5, 2,5, 1,25 kg (in lb: 45, 35, 25, 10, 5,
  2,5); bars with editable weights: Olympic 20, Short 15, EZ 7,5, Hex 25, None 0.
- **States:** kg; lb; editing a bar's weight.
- **Source:** #17 Q22, Q23; #25; #48.
- **Built:** not built.

**EL-08 · Devices and apps**
- **Purpose:** link Apple Health, the Watch and other services, and see their state.
- **Shows / actions:** grouped as Strava does: Health (Apple Health; Health Connect on Android);
  Devices (Apple Watch, Suunto, Garmin, COROS, Polar, Amazfit, Huawei, Samsung, Wahoo, Whoop, Oura…);
  Apps (Strava, Hevy, Fitbod, Runna…). Names as text, no logos. Each row: its state (Linked · last
  sync 07:02, Not linked, Error) with Link or Unlink.
- **States:** nothing linked; Apple Health linked; an error; linking in progress.
- **Source:** #52 (later).
- **Built:** not built.

**EL-09 · Apple Health link**
- **Purpose:** what Strive reads and writes, before the system sheet.
- **Shows / actions:** reads workouts and heart rate; writes Strive's workouts so the Activity rings
  count them; Link (→ the system Health sheet), Unlink.
- **States:** before linking; linked; not available with a free Apple ID (a spike decides).
- **Source:** #52, ADR 0001 (HealthKit is available with a free Apple ID; #7 doubted it).
- **Built:** not built.

**EL-10 · Gear**
- **Purpose:** shoes and bikes, and how far each has gone.
- **Shows / actions:** Shoes and Bikes as groups; a row: name, brand and model, distance ("412 km"),
  the default badge for its activity type, a warning when past the set distance; Retired at the
  bottom; + Add gear.
- **States:** filled; a pair past its warning distance; none yet; retired list.
- **Source:** #52 (later; where it lives is open, O-13).
- **Built:** not built.

**EL-11 · Gear detail and edit**
- **Purpose:** add or change one item.
- **Shows / actions:** name, brand, model, starting distance, warning distance, default for (Run,
  Ride…), total distance and the activities that used it; **Retire**; Save.
- **States:** new; existing; retired (Unretire).
- **Source:** #52.
- **Built:** not built.

**EL-12 · Choose gear**
- **Purpose:** pick the shoes for a run.
- **Shows / actions:** a sheet listing active shoes with distance, the default checked; None.
- **States:** default chosen; another chosen.
- **Source:** #52 (a run gets the default without a tap, changed afterwards).
- **Built:** not built.

**EL-13 · Export**
- **Purpose:** send workouts and runs to the OS training log or another app.
- **Shows / actions:** what (Workouts · Runs · All), the period ("Last 30 days", custom dates), the
  format (O-10), **Share…** which shows "Exporting…" with a spinner in the button and then opens the
  system share sheet.
- **States:** default; custom period; nothing in the period; exporting; share sheet open.
- **Source:** #14 (You › Export, 2 taps), #19 (reuse the system share sheet), #6.
- **Built:** not built.

**EL-14 · About Strive**
- **Purpose:** who built it, and the credits the media licences require.
- **Shows / actions:** version; "Built by Carl"; credits for exercise data and images where a licence
  requires one (RepDB's free tier requires "Exercise data by RepDB"); open-source licences.
- **States:** shown.
- **Source:** #4 comments 6 and 7 (a credits screen; RepDB fallback images), #25 ("says Carl built
  the app").
- **Built:** not built.

**EL-15 · Import (later)**
- **Purpose:** bring in history from Strong, Strava or a file.
- **Shows / actions:** a source (Strong, Strava, a file), a preview of what comes in (counts, dates,
  exercises mapped), Confirm. In v1 only the disabled row "Comes with import" (PL-01, EL-01).
- **States:** v1 disabled row; choose source; preview; importing; done.
- **Source:** #14 inventory, #7, #17 Q47.
- **Built:** not built.

**EL-16 · Account and sync (later)**
- **Purpose:** sign in and see the backup.
- **Shows / actions:** sign in, sync status, last backup, the paused-project state.
- **States:** signed out; synced; syncing; Supabase project paused.
- **Source:** #14 inventory, #8.
- **Built:** not built.

**EL-17 · Food today (later)**
- **Purpose:** calories and macros against the target.
- **Shows / actions:** today's calories and macros against the target, meals logged.
- **States:** no target; on track; over.
- **Source:** #14 inventory, #9.
- **Built:** not built.

**EL-18 · Log food (later)**
- **Purpose:** log what Carl ate.
- **Shows / actions:** search, recent, portion.
- **States:** search; recent; portion picker.
- **Source:** #14 inventory, #9.
- **Built:** not built.

**EL-19 · Nutrition targets (later)**
- **Purpose:** calories and macros per day.
- **Shows / actions:** a target per day for calories, protein, carbohydrate, fat.
- **States:** unset; set.
- **Source:** #14 inventory, #9.
- **Built:** not built.

---

## 2. Flows

Each step is a screen ID; the arrow names the tap. "(n)" counts taps where a decision set a limit.

| ID | Flow | Steps |
|---|---|---|
| F-01 | First launch | SH-06 welcome → Next → how it works → Next → basics (kg) → Next → ready → "Create your first template" → PL-05; or Skip on any page → SH-02 first use |
| F-02 | Start today's workout from Home (1) | SH-02 → "Start Push A" → WO-02 state 1 |
| F-03 | Start today's workout from Start (2) | any tab → Start → SH-03 → today's bubble → WO-02 state 1 |
| F-04 | Start another template (3) | Start → SH-03 → From template → SH-04 → Ben A → WO-02 state 1 |
| F-05 | Empty workout with a quick pick | Start → SH-03 → Empty workout → WO-01 → "+ Bench Press BB" → WO-02 |
| F-06 | Add exercises and a superset | WO-01 or WO-02 → Add exercises → EX-03 → tick Incline Press DB, Lateral Raise DB → Superset (2) → WO-02 state 5 |
| F-07 | Log a set as hinted (1) | WO-02 → ✓ on the next row (or Log set on the focus card) → WO-02 state 3 with WO-06 |
| F-08 | Log with RIR (2) | WO-02 → RIR 2 on the focus card → Log set → WO-02 state 3 |
| F-09 | Change weight one step (3) | WO-02 → Edit → WO-04 → + by weight → Log → WO-02 state 3 |
| F-10 | Type a new weight and walk the fields | WO-02 → tap the kg field → WO-05 (weight) → 8, 5 → Next → reps → Next → rest → Log › → WO-02 state 3 |
| F-11 | Read and build plates | WO-02 (plates line on the next set) → tap the set → WO-04 (bar and plates) → kg field → WO-05 → Plates → 25, 5, 1,25 → Next … Log › |
| F-12 | RIR of 6 | WO-04 → 4+ → 4–10 row → 6 → Log |
| F-13 | Rest, free rest and the locked phone | WO-02 → ✓ → WO-06 counting → + 15 s → lock phone → WO-19 / WO-20 → rest ends → WO-18 → tap → WO-02; or ⏱ → WO-07 → 2:00 → WO-06 |
| F-14 | Change a rest line | WO-02 → tap "Rest 2:00" → WO-04 (rest field) → − → Save |
| F-15 | Log a superset round | WO-02 state 5 → ✓ B 1 → ✓ A 1 → WO-06 starts (round rest) → focus A 2 |
| F-16 | Add a warm-up set (3) | WO-02 → + Add set → badge → WO-13 → Warm-up → the set moves up as next |
| F-17 | Delete a set | WO-02 → swipe a row left → WO-14 → Delete |
| F-18 | Exercise menu actions | WO-02 → ⋯ → WO-09 → Note (field opens) / Sticky note (prompt) / Rest for all sets › 3:00 / Create superset → WO-10 / Order and skip → WO-11 / Exercise settings → WO-12 / Remove exercise |
| F-19 | Swap a busy machine | WO-02 → ⋯ → WO-09 → Swap → EX-04 → Chest Press Machine → WO-02 |
| F-20 | Workout menu | WO-02 → ⋯ by Push A → WO-08 → Rename / Workout note / Change start time |
| F-21 | Scroll and come back | WO-02 → scroll → WO-03 → tap the strip → WO-02 (focus card) |
| F-22 | Minimise and return | WO-02 → ⌄ → SH-01 with WO-17 → browse Plan → tap WO-17 → WO-02 |
| F-23 | Start while a workout runs | Start → SH-03 (only "Continue Push A") → WO-02 |
| F-24 | App killed mid-workout | WO-02 → app killed → relaunch → SH-02 with WO-17 → tap → WO-02 state 16 |
| F-25 | Finish with sets left | WO-02 → Finish → WO-15 → Log them as they are and save → WO-21 → RPE 8 → note → Done → WO-22 → Update → back where it started |
| F-26 | Finish with everything logged | WO-02 state 13 → Finish (filled) → WO-21 → Done |
| F-27 | Discard a workout | WO-02 → Discard workout → WO-16 → Discard → SH-02; or Finish → WO-15 → Discard workout |
| F-28 | Empty workout to template | F-05 … Finish → WO-21 → Save as template → WO-23 → Save → PL-01 shows it |
| F-29 | Look back at a workout | SH-01 You → EL-01 → All activities → WO-26 → a row → WO-24 → back |
| F-30 | Correct a past workout | WO-24 → Edit → WO-25 → change a set (WO-04, WO-05) → Done; or Do it again → WO-02 state 1; or Save as template → WO-23; or Delete workout → alert |
| F-31 | Filter history to runs | WO-26 → Running → a run → RU-05 |
| F-32 | Plan: change week and come back | PL-01 → › → next week → Today → this week; or swipe on the days |
| F-33 | Plan: week controls | PL-01 → ⋯ → PL-02 → Short week (days grey) / Deload now / Week type › Calibration |
| F-34 | Move a session (Flytta till) | PL-01 → tap Saturday → day card ⋯ → PL-02 Move to… → Sunday → week card updates |
| F-35 | Start from Plan (1) | PL-01 → Start workout on today's card → WO-02 state 1 |
| F-36 | Preview and start a template | PL-01 → swipe Templates → Pull A card → PL-04 → Start workout → WO-02 |
| F-37 | Template card actions | PL-01 → card ⋯ → PL-03 → Duplicate / Share (share sheet) / Archive / Delete (alert); Templates ⋯ → Show archived → PL-07 → Restore |
| F-38 | Create a template | PL-01 + (or Create template) → PL-05 → name → Add exercises → EX-03 → Add (4) → Reps field → PL-06 → 8 – 1 0 → Same for the rest → Done → PL-01 |
| F-39 | Edit a template, abandon it | PL-04 → Edit → PL-05 → change → Cancel → "Discard changes?" → Discard → PL-04 |
| F-40 | Deload when recommended | PL-01 (Deload recommended) → Deload now → week card shows D |
| F-41 | Programme: change a week and a day | PL-01 → programme name → PL-08 → week 8 row → Deload; week 6 → Thursday menu → Rest |
| F-42 | Programme tabs | PL-08 → Weights → PL-09 → Log → PL-10 → New note → Settings → PL-11 → Length → No fixed length; Archive programme → alert |
| F-43 | Create a programme | PL-01 first use → Create programme → PL-12 → Save → PL-01 with the week card |
| F-44 | Browse the library | PL-01 → Exercise library → EX-01 → chip Vertical pull → search "row" → filter → EX-02 → Dumbbells → Done → a row → EX-05 → History → EX-06 → Chart → EX-07 → tap a point → tap again → WO-24 → back → Records → EX-08 |
| F-45 | Mark an exercise | EX-05 → In my programmes → Swap → line explains; EX-01 shows the Swap badge |
| F-46 | Create an exercise | EX-01 + (or EX-03 New) → EX-09 → Name "Copenhagen Plank", Primary muscle, Pattern, Equipment, Type Time, Per side → Save → EX-01 (or ticked in EX-03) |
| F-47 | Edit, merge, archive an exercise | EX-05 → ⋯ Edit → EX-10 → Merge data → EX-11 → alert → Merge; or Archive; Type row says why it is locked |
| F-48 | Exercise detail from a workout | WO-02 → (O-02) → EX-05 as a sheet → close → WO-02 |
| F-49 | Record a run | Start → SH-03 → Run → RU-01 (SH-08 location first time) → Start run → RU-02 → Lap → Pause → RU-03 → Resume → RU-02 → Pause → Finish → RU-04 → Save run → RU-05 → scroll → RU-06 → Done → back where it started |
| F-50 | Look at a run's route | RU-05 → map → RU-07 → Satellite → 3D → close; RU-05 → play → RU-08 |
| F-51 | Change a run's shoes | RU-05 (from History) → Edit → RU-09 → Shoes → EL-12 → Pegasus 41 → Save |
| F-52 | Run day from Plan | PL-01 → Friday (Easy run) → day card → Start run → RU-01 |
| F-53 | Progress | SH-01 You → EL-01 → Progress → EL-02 → tap the comparison (next one) → 6 mo → tap a bar → a lift tile → EX-05; Running → EL-03 |
| F-54 | Pick the main lifts | EL-02 → (O-14) → EL-04 → tick Romanian Deadlift DB → Done → EL-02 |
| F-55 | Export | EL-01 → Export → EL-13 → Workouts → Last 30 days → Share… → share sheet → Files |
| F-56 | Settings | EL-01 → Settings → EL-06 → Default rest 2:30; Units lb; Plates and bars → EL-07 → untick 15 kg; Language → English |
| F-57 | Gear | EL-01 → Gear → EL-10 → + → EL-11 → Novablast 4, default for Run, warn at 700 km → Save; a run (F-49) adds its km; past 700 km EL-10 warns → EL-11 → Retire |
| F-58 | Link Apple Health | EL-06 → Devices and apps → EL-08 → Apple Health → EL-09 → Link → SH-08 / system sheet → EL-08 linked; Unlink |
| F-59 | Profile | EL-01 → Profile → EL-05 → body weight → done |
| F-60 | About | EL-06 → About Strive → EL-14 |
| F-61 | Food (v1) | SH-01 Food → SH-05 |
| F-62 | Later: import, sync, food | EL-01 → Import (EL-15) / Sync (EL-16); SH-01 Food → EL-17 → Log food → EL-18; EL-17 → target → EL-19 |
| F-63 | First-use hints | F-02 first time → WO-02 with SH-07 on ✓ → dismiss |
| F-64 | Rest-day and run-day Home | SH-02 on Wednesday (rest) and Friday (run → RU-01) |

---

## 3. Decisions that bind every design

What a screen does, what it shows, and what a gesture does. Numbers are the issue's question (Q),
choice, or Carl's numbered decision; dates are Carl's.

**Function binds; the chosen form does not.** Where #16 or #17 chose a layout among sketched options
(the dock S3, keypad K3, menus M2, frame F1 with D's focus card, supersets G2, summary U1 + U3, Plan
P3 + P1 and W2, editor T4, programme B5, library L1 and R1, form E1, detail X1, history H3, Progress
G6 + G5, Start N4), the content, the actions, the tap budget and the gesture bind every design; the
layout itself is design 1's baseline (section 4). Section 1 names elements the way design 1 draws
them. How far designs 2 and 3 may move the navigation is O-26.

**Navigation and shell**
- Five places: Home (today), Food, Start (begins every workout and run), Plan (ahead), You (behind);
  the same structure on iPhone and Android (#14).
- Templates and the library live in Plan with the week and programme; history and progress live in
  You, as in Strava (#14).
- Every v1 feature within two taps of the start screen; today's workout in one (#14).
- Workouts and runs run full screen without the tab bar until Finish or End (#14).
- Plan becomes Coach once the OS coach's guidance arrives through import (#14, #7).
- Food holds nutrition's place, and filling it later moves nothing (#14, #9).
- The app follows the phone's language, English and Swedish, with an override in Settings; exercise
  names stay English (#25, #3, #4).
- No appearance setting: the app follows the phone's light or dark mode (#15 Q6).
- Carl's own use beats Apple's HIG for now; every deliberate deviation is listed (#2, #14).
- Onboarding: short, skippable, says Carl built the app, teaches in context, asks permissions only
  when needed, leads straight into the app (#25, #17 Q43).

**Start**
- Today's planned workout first and most prominent; then Empty workout, From template (last done
  first), Run. Today's, empty and run cost 2 taps from any tab; another template 3 (Q46, choice 14c).
- The screen underneath stays in view; a tap outside closes; Reduce Motion drops the movement
  (choice 14c, decision 35).
- With a workout in progress Start offers only "Continue … · time"; two workouts never run at once
  (Q46).
- First use: Create template in today's place (choice 14c).

**Logging a set**
- A set holds weight and reps, optional RIR (empty by default, plannable), a kind (warm-up, working,
  drop, failure) and its own rest; notes per exercise and per workout (Q1, Q3).
- Five exercise types: weight × reps; bodyweight × reps with added or assisting weight; reps only;
  distance (optional weight); time (optional weight). Per side: one set covers both sides (Q2).
- Hints: the plan's weight, else the same set last time, else empty; reps from last time, else the
  top of the range; RIR empty with the target beside it; ✓ on an untouched set logs the hints; a
  field without a hint must be filled first (Q4).
- The progression suggestion becomes the weight hint and can always be overwritten; the plan's weight
  wins (Q5; Q6's rules; Q7's steps: barbell 2,5, dumbbells 2, kettlebell 4, machine and cable
  2,5 kg). It shows as ↑ or ↓ by the weight (Q21) and as "up 2,5 kg" by the next set (decision 39).
- **Taps per set:** same as the hint 1, plus RIR 2, one step 3; typing a new number, changing both
  values or changing the kind may cost more (Q13, choice 1b). RIR is 0 · 1 · 2 · 3 · 4+, and 4+
  opens 4–10 (change to Q13.3).
- Every set has its own ✓ that logs it as it stands; a tap on the set opens a place to change it,
  with − and + beside weight and reps, RIR, the rest, and Log; it closes by a swipe down or a tap
  outside, with no ✕; a logged set opens with "Save change" (choices 1, 1b, 2 addition).
- Strive's own keypad: digits, the locale's decimal comma, ⌫, Hide, Plates (weight only), − and + at
  the exercise's step, Next walking weight → reps → rest; on the rest field "Log ›" logs like Log,
  closes and starts the rest. While it is open, the rest timer hides (Q16, choice 2 and correction).
- Plates per side for the next barbell or plate-loaded set, readable without a tap; the bar and its
  plates where the set is changed, with the bar chosen per exercise; plate keys build a weight,
  heaviest innermost as keyed; a typed weight shows fewest plates first (Q22, choice 3).
- Warm-ups counted from the first working set: bar × 10, 50 % × 5, 70 % × 3, 85 % × 1, rounded, sets
  lighter than the bar dropped (Q22). A warm-up is added like any set and changed in its badge; an
  unlogged set made a warm-up moves up to be next (choice 4). Its weight hint is O-03.
- The badge shows the set's number, or W for warm-up, D for drop; a failure set keeps its number
  (Carl 2026-11-03). Tapping the badge changes the kind (Q21).
- Previous shows last time compactly ("80 × 6 @3") (Q21).
- Swipe a set left to delete it, Mail's full swipe; sets are added with Add set (a copy of the last)
  (choice 4).
- A record is marked on the set the moment it is logged (Q14).
- A short haptic confirms ✓; the screen stays awake during a workout, switchable (Q23).
- Weight unit per exercise, kg or lb, defaulting to Settings; totals and records converted (Q23).

**The active workout**
- Always visible: the workout's name with its menu, elapsed time, sets done of total, minimise, free
  rest, Finish (Q9, choice 5). One clock drives every time on screen (commit 8ede5db).
- The next set is always one tap from being logged, wherever Carl has scrolled, and the next set
  follows into the next exercise (decision 39).
- No pause; the time is corrected in the summary (Q9).
- Empty workout: asks what Carl trains today and offers quick picks that add an exercise in one tap:
  those marked Yes, else the most recent (decision 40).
- The list ends with Add exercises (multi-select) and Discard workout in red (Q19).
- Finish becomes the prominent action once the last set is logged; with sets left it offers four
  choices, log them as they stand and save, save without them, discard, continue, and says how many
  are left (Q12, Q20). Unlogged sets are not saved (Q12).
- The workout minimises to a bar above the tab bar showing name, time and rest; a tap returns (Q19).
- Per exercise: the name, its menu, the target, and in grey the sticky note, the plan's cue and last
  time's note; today's note opens from the menu, with dictation (Q11, choice 6). No per-exercise
  focus metric (Q18). The name folds the exercise (Carl's point 3; O-02).
- Exercise menu: Note and Swap first, then Sticky note, Rest for all sets, Create superset (Remove
  from superset inside one), Order and skip, the exercise's settings, Remove in red (Q15, choice 4).
- Workout menu: Rename, Workout note, Change start time (Q19).
- Swap offers the exercise's Swap alternative and the same pattern first (Q8).
- Add, swap, reorder, skip (skipped stays recorded), add and remove sets (Q8).
- Supersets are lifted round by round and the next set follows that order; the round's rest starts
  once every set of the round is logged, in any order (choice 6 and correction).

**Rest**
- Rest starts when a set is logged: the set's rest, else the default from Settings; "Next set" only
  scrolls; the actual rest is stored silently (Q10, #43).
- Each set's rest is editable between sets, − and + by 15 s; the countdown stays in reach wherever
  Carl scrolls, with − and +, and Skip set apart from them; a free rest from presets 0:30, 1:00,
  2:00, 3:00 or a custom time; no pause or reset; it ends with a sound and a buzz (Q17, #16 Q7).
- A notification when rest ends with the phone locked; a Live Activity on the lock screen and in the
  Dynamic Island, after a spike (Q10, decision 33, #43). Reduce Motion: only the digits change (#43).

**Summary, session detail, history**
- Summary and session detail are one screen; from history Edit replaces Done (Q29).
- Content: the date, clock time and week · block; Time, Avg RIR, Sets, Records; the records listed;
  RPE 1–10 and the workout note, asked before the exercises; per exercise the best set against last
  time, every set one tap away with records starred; start, end (editable), week (Q28, choice 7).
- No volume tile (decision 32). Avg RIR against the plan's target, averaging the sets that have RIR,
  "8 of 11 sets" under it, "–" with none (choices 7, 8).
- Done on a workout that differs from its template asks "Update the template …?" listing the changes;
  structure and rest count, weights do not; an empty workout offers Save as template; Done returns to
  where the workout started (Q29).
- Session detail edits with the workout's rows and keypad; Delete with an alert; Do it again; Save as
  template (Q30).
- History: from You › All activities; weeks with a header that sums the week; All · Strength ·
  Running; per row the day and date, workout or run, minutes, sets and volume (strength) or km and
  pace (run), a record count; no search, no calendar (Q44, choice 14).
- Runs set records too, starred (choice 14; which ones: O-16).

**Plan, templates, programme**
- A template: name, a note shown when the workout starts, estimated time sets × (rest + 40 s) as
  "about 55 min", last done; no folders (Q25).
- The editor's rows are the workout's rows with the same keypad; a range key ("8–10"); Same for the
  rest; a new set copies the one above; a progression switch per exercise, on by default (Q26,
  choice 9).
- Done saves; Cancel with changes asks "Discard changes?"; Delete in red with an alert; Duplicate;
  Share; Archive; archived templates reachable from Templates (Q27, choice 8).
- Plan opens on the week with today's template ready to start in one tap, Edit beside it; any day
  shows its plan in the same place; a template opens a preview with Start and Edit (choices 8, 13;
  Q41). + creates a template (Q41).
- Where Carl is in the programme and which week he looks at are one thing: the programme's name, the
  week "Week 3 of 8", its weeks with calibration and deload marked, the days, and the deload line
  (choice 13).
- Week: Monday to Sunday; visible previous and next buttons, swipe as a shortcut, Today once away;
  done ✓ (opens the workout), missed, Extra, today marked; Move to within this week only; missed
  sessions are never moved automatically (Q40, choice 13).
- Programme: name, start, length in weeks or none; a standard week edited once; weeks counted on the
  calendar from the start; past programmes archived with their dates (Q38).
- Week types normal, calibration (overrides top sets' RIR target), deload (halves working sets,
  pauses progression; planned or Deload now); "Deload recommended" from week 8 after 4–6 weeks;
  each weekday has a priority and Short week greys the days dropped first, moving nothing (Q39).
- Programme screen: Weeks, Weights, Log and Settings; week 8 to deload in 2 taps, one day to rest in
  3, every target 44 pt (choice 13b); working weights from the log; a dated notes log (Q42).
- Warm-up and mobility are exercises inside templates, not features (Q42).

**Exercises**
- Library: grouped by primary muscle; search over names and aliases; Carl's patterns as quick
  filters; filters for equipment and In my programmes, with No hidden until ticked; sort A–Z, most
  used, last used; + creates (Q34, choices 11, 12b). Stretches and cardio stay in, under Mobility and
  Conditioning (decision 36).
- A row: image crop, name, pattern · equipment, last performance, mark badge (Q34, choice 11).
- The picker is the library with checks, Add (n), Superset (n) and New (Q35, choice 11).
- Exercise detail has four parts, About first: About · History · Chart · Records (Q32, choice 10).
  About opens with Carl's illustration (start and end, worked muscles red), which replaced video and
  a drawn map; the library row's image is its crop (choice 10 settled, decision 31).
- The mark is called "In my programmes": Yes / Swap / Test / No, with a line saying what the value
  does (choice 10 settled).
- Records per type, Epley 1RM with reps + RIR, warm-up and drop sets excluded, a records history
  (Q31). Charts per type; period 3 mo · 1 yr · All; a tap shows value and date, a second opens the
  workout (Q33).
- Create and edit: every field from Q35; Save waits for a name and a primary muscle; equipment fills
  step and bar; everything editable except the type once sets exist, with the reason shown; Merge
  data behind an alert; Archive keeps history; Delete only if never used (Q35, choice 12).
- English names with equipment suffixes (DB, BB, KB, EZ, Cable, Machine), "Single" first; 553
  exercises ship (#4, decisions 29, 37).

**Progress**
- Strength · Running; the total lifted since the start with a comparison that rotates on a tap (25
  of them); period 8 wk · 6 mo · 1 yr with a summary against the period before; the main lifts with
  estimated 1RM, change and trend, opening exercise detail; volume per week against the period
  before, with an average and deload weeks marked; working sets per muscle against a 10–20 band;
  records from the last 30 days (Q45, choice 14b).
- Volume stays a measure everywhere except the summary's tiles (choice 14).

**Running**
- Runs are very like Strava in layout, statistics and how the route is shown (#18, Carl 2026-10-23);
  strength takes only functions from Strong, never its looks (#17).
- The live screen's largest type is the one number Carl names (#18 Design; O-05).
- Live run: map, pace, distance, time, pause, lap; recording continues locked (#14, #5).
- Run detail: the map on top; title, date, place; distance, avg pace, moving time, kcal, avg HR,
  max HR; the device; sections against distance (splits, elevation, pace, heart rate, segments);
  the map full screen with types, 3D, save route, sources; flyover replay; a list row with a map
  thumbnail (#18 comment; scope O-06).

**You, settings, export, gear, devices**
- You: progress and activities at the top, then profile, settings, export; import and sync later
  (#14, #25).
- Settings: units, default rest, plates your gym has, bar weights, default unit, keep the screen
  awake, GPS accuracy, language (#14, #17, #25).
- Export: what and which period, then the system share sheet; works with whatever format #6 picks
  (#19; #6: files, not a server, in v1).
- Gear: shoes and bikes with name, brand, model, starting distance, Retire; distance from activities;
  a default per activity type; a warning past a set distance (#52, later).
- Devices and apps: a grouped list with Link, Unlink and state (linked, last sync, error); names as
  text, no logos (#52, later).

**Empty states**
- No templates: "No templates yet" with Create template and Start empty workout. No programme: "No
  programme" with Create programme; everything else works without one. No history: History and
  Progress say "No workouts yet" with Start a workout. A new exercise: no hints, Previous "–",
  exercise detail "Log a workout to see your history". Strong import as a disabled "Comes with
  import" row (Q47).

**Motion and accessibility**
- Animations follow the `apple-motion` skill and respect Reduce Motion (decision 35).
- Every tap target 44 pt (listed deviations aside); every text pair 4.5:1, 7:1 under Increase
  Contrast (#15, #2); every screen in light, dark and Increase Contrast (brief).
- A logged set of three taps or fewer is #17's acceptance and #42's evaluation.

---

## 4. Design 1's baseline

**These bind design 1 only.** Designs 2 and 3 take the functions above and none of this.

**The layouts #16 and #17 chose, which design 1 keeps** (each with its sketch in
`docs/design/17-strength/index.html` or `16-components/index.html`):
- Logging: S3, a dock at the bottom; D3, a ✓ per row and the dock opened by a tap, with a grabber;
  K3, glass keys with Next in Log's place; RIR and rest sharing a row; P3 + P4, plates in the dock and
  on the next set's line (choices 1, 1b, 2, 3).
- Menus: iOS pull-downs; M2, the exercise menu with Note and Swap as two palette tiles (choice 4).
- Frame: F1's one bar, plus D's large title and focus card (choice 5, decision 39); the empty workout
  as C-tomt (decision 40).
- Supersets: G2 with G1's edge line (choice 6).
- Summary: U1's top with U3's folded exercises (choice 7).
- Plan: P3's week as the way in, W2's programme card framing the week, P1's swiped template cards
  that zoom to a full-screen preview; T4's editor; S1's "Same for the rest" key (choices 8, 9, 13).
- Programme: B5, tabs with a week per row that opens (choice 13b).
- Exercise detail: X1, a segmented control with four tabs (choice 10).
- Library: L1, one list with sticky muscle headings; R1, a filter sheet behind a button (choices 11,
  12b). Create and edit: E1, menus in a form sheet (choice 12).
- History: H3, days down the side. Progress: G6 with G5's total on top. Start: N4, bubbles in an
  arc (choices 14, 14b, 14c).
- Components (#16): I3 number fields, B3 button corners, P2 press (shrink and dim), rule A for lists
  (blocks with hairlines; joined tiles for short lists), D1 tiles and grey chart marks with the
  selected one teal, T5 rest card in glass with K2's round − and +.

**Tokens.** `docs/design/15-tokens/tokens.py` generates `tokens.css`, `tokens.json` and the app's
`theme/Tokens.kt` (CI fails on a diff). Direction B: calm, clear, native; Material 3 role names;
four schemes (light, dark, light-hc, dark-hc); system face (SF Pro / Roboto), tabular figures.
Page: https://claude.ai/artifact/7A2qZDPnvdxupjX9aXuv3H.

| Role | light | dark | light-hc | dark-hc |
|---|---|---|---|---|
| primary (teal: only what can be tapped or is selected) | #08736c | #40c8bd | #00504b | #8aede3 |
| primaryContainer (⋯ pills, rest line) | #c6efea | #00504b | #00504b | #00504b |
| tertiary (amber: rest ring, warm-up) | #8a5100 | #ffb95f | #5a3300 | #ffd8a8 |
| error | #c4281c | #ffb4ab | #8c1d18 | #ffd1cc |
| background · surface | #f2f2f7 · #ffffff | #000000 · #1c1c1e | #f2f2f7 · #ffffff | #000000 · #1c1c1e |
| accent failure (coral) | #a8391b | #ff6c47 | #7d2a06 | #ffa088 |
| accent drop (violet) | #7a45c4 | #c79cff | #55278f | #d9a3ff |
| accent superset (blue) | #1f63c7 | #5aa9ff | #0f428f | #7cbeff |

- Type (weight/size/line): display-small 600/34/41 for big numbers, headline-large 700/34/41 for
  screen titles, title-medium 600/17/22, body-large 400/17/22, body-small 400/13/18, label-small
  400/11/13. Space steps 4–48. Corners 6 · 10 · 12 · 16 · 28 · full. Glass (dark): regular
  rgb(28 28 30/.58), thick /.8, edge white/.1, shadow 0 8px 24px black/.4; blur 24. Motion: press
  100 ms, release spring 350 ms, fade 250 ms, stagger 40 ms; press scales to .96.
- Rules: glass only on controls and overlays (tab bar, sheets, dock, rest card, map labels); cards and
  lists solid; **no tonal containers in Increase Contrast**: use surface roles (surface,
  surface-container-high), since dark-hc's tonal fills turned near-white (commits 5226124, c28b8c5;
  dark-hc keeps D's deep teal ⋯ pills and rest line, mark at 7.5:1). Ta bort reads deep red in dark
  (commit 8ede5db).

**D's workout screen** (decisions 39, 40; canvas boards D-pass, D-next, D-scroll, C-tomt at
https://claude.ai/artifact/6hPecJvYLJcGA7Wk3197zx):
- B's large title (28 pt bold name, ⋯ pill on primaryContainer, calendar and clock icons, "3 of 9
  sets"), folding into the F1 bar on scroll.
- C's focus card on surface (22 pt corners): teal caps label, 38 pt bold values with grey units,
  "up 2,5 kg" teal, "rest 2:00 after" and "Previous 80 × 8 @3" in a row, the RIR track, **Edit** on
  surface-container-high and the filled teal **Log set 2** (1:2 widths). No tonal roles.
- A's rows: grid Set (36) · Previous · kg (64) · Reps (56) · ✓ (44); badge 28 pt; the next row tinted
  teal 12 % and reaching 6 pt past the columns on an opaque base; next ✓ outlined teal, logged ✓
  filled teal; rest line "Vila 2:00" teal after the last logged, grey ahead; "+ Lägg till set (2:00)"
  on surface.
- Folded exercises share one overview card: name, dots (ring; filled in the kind's colour), "0/3",
  chevron; "5/5 ✓" in teal when done.
- The strip under the bar on surface-container-high: teal caps label, values, RIR; one button back
  to the card.
- Empty workout (C-tomt): card with a 72 pt round + on primaryContainer, "Vad kör du idag?", filled
  Lägg till övningar, Snabbval chips with a teal +, Släng passet in red text.

**Supersets as G2** (choice 6, commit 35c2956, Carl 2026-11-03): one list in lifting order, the A/B
letter tags on surface-container-high in the superset blue, the edge line in blue, and the word
"Superset" in blue in "Superset · vila 1:30 efter varvet".

**A colour per set kind** (Carl 2026-11-03, commits 2829780, f0859ec, 1fe5c24): warm-up W in
tertiary amber with "UPPVÄRMNING"; working sets numbered, teal; failure keeps its number in coral
with "TILL FAILURE" (only the word coloured); drop D unnumbered in violet with "DROPSET"; a logged
dot takes its kind's colour. One mapping (`SetKind.tint`) feeds badge, label, dots and menu.

**The library and the dock as built** (choices 11, 12b, 1b, 2): L1 list with sticky muscle headings,
pattern chips, filter button that fills, R1 filter sheet; the glass dock with grabber, I3 grey fields,
the RIR + rest row, K3 glass keys with Hide, − and + 44 pt, full-width Next / Log ›. Rest card T5 in
glass with the 64 pt amber ring (#16 Q7).

**Paths.**
- Tokens: `docs/design/15-tokens/tokens.py`, `tokens.css`, `tokens.json`; app `theme/Tokens.kt`,
  `theme/Theme.kt`, `theme/Glass.kt`.
- Components and their states: `docs/design/16-components/index.html`, `components.css`,
  `components.js` (catalogue: https://claude.ai/artifact/Q1Cu1mfmb7WKeJhNvZWwVk).
- Sketches and rejected options: `docs/design/17-strength/index.html`; the illustration style:
  `docs/design/17-strength/muscle-prompts.md`, `bench-press.jpg`.
- Built screens: `workout/WorkoutScreen.kt`, `workout/Focus.kt`, `workout/Dock.kt`,
  `workout/Rest.kt`, `workout/Parts.kt`, `workout/SummaryScreen.kt`, `workout/HistoryScreen.kt`,
  `library/LibraryScreen.kt`, `library/LibraryFilter.kt`, `library/ExerciseScreen.kt`, `App.kt`.
- To render a sketch: copy `docs/design/` to a temp directory and serve it with
  `python3 -m http.server` (brief).

---

## 5. Sample data

One set for every design. **Today is Monday 9 November 2026, ISO week 46.** Labels in English; Carl's
own names and notes as he writes them. Numbers in Swedish format (82,5) only where the design shows a
Swedish locale; the English UI writes 82.5.

**Carl.** Carl Lundholm, initials CL. Body weight 82,4 kg, height 183 cm. Goals: "Strength while I
recompose" and "10 km under 50 min". Units kg, default rest 2:00, language follows the phone.
Plates: all seven ticked except 15 kg. Bars as defaults. Keep the screen awake: on.

**Programme.** "Hypertrofi, block 1": started Monday 5 October 2026 (week 41), 8 weeks, weeks 1–2
calibration (K), no deload yet. Today is **week 6 of 8**: "6 weeks without a deload" (not yet
recommended). For the recommended state: "Hypertrofi, 12 veckor", week 9 of 12, "Deload
recommended".
- Standard week: Mon **Push A** · Tue **Ben A** · Wed Rest · Thu **Pull A** · Fri **Easy run 8 km** ·
  Sat **Push B** · Sun **Long run 14 km**. Short week drops Push B, then the easy run.
- Week 46 (9–15 Nov): Mon Push A in progress (today), the rest planned.
- Week 45 (2–8 Nov): all done ✓. Week 44 (26 Oct–1 Nov): Thu Pull A missed (struck through), Wed
  "Armar" extra.
- Weights tab: Squat BB 100 → 107,5 kg · Bench Press BB 77,5 → 82,5 kg · Deadlift BB 120 → 127,5 kg ·
  Overhead Press BB 50 → 52,5 kg.
- Log tab: "2 nov — Axeln kändes bra på militärpressen." · "19 okt — Kalibreringen klar. RIR stämde
  på bänken." · "5 okt — Nytt block efter deloadveckan."

**Templates** (in the programme, cards in this order).
1. **Push A** — chest, shoulders, triceps · 5 exercises · 15 sets · about 30 min · last done Mon 2 Nov.
   Note: "Kontrollerad nedfirning, två sekunder."
   - Bench Press BB — W 10 reps; sets 1–3: 6–8 reps @ RIR 2–3; rest 2:00. Cue: "Skulderbladen ihop."
   - Superset, rest 1:30 after each round: **A** Incline Press DB — 3 × 8–10 @ 2; **B** Lateral Raise
     DB — 3 × 12–15 @ 1–2.
   - Tricep Dips (bodyweight, +10 kg) — 2 × 8–12 @ 2, set 2 to failure; rest 1:30.
   - Tricep Pushdown (cable) — 2 × 10–12 @ 2, then a drop set; rest 1:30.
2. **Pull A** — back, biceps · 4 exercises · 13 sets · about 35 min · last done Thu 5 Nov: Deadlift BB
   (W + 3 × 5 @ 2), Lat Pulldown Cable (3 × 8–10 @ 2), Seated Row Cable (3 × 10–12 @ 2), Curl DB
   (3 × 10–12 @ 1–2).
3. **Ben A** — quads, hamstrings, calves · 4 exercises · 14 sets · about 40 min · last done Tue 3 Nov:
   Squat BB (W + 3 × 6–8 @ 2–3), Romanian Deadlift DB (3 × 8–10 @ 2), Leg Press (3 × 10–12 @ 2),
   Standing Calf Raise Machine (4 × 12–15 @ 1).
- Also in the programme: **Push B** (Overhead Press BB, Incline Press BB, Fly Cable, Tricep
  Pushdown · 12 sets · about 35 min · Sat 7 Nov). Outside the programme: **Armar** (Curl DB, Hammer
  Curl DB, Tricep Pushdown · 9 sets · about 25 min · Wed 28 Oct). Archived: **Helkropp** (last done
  14 Sep).

**Active workout: Push A, today, started 17:02.** Snapshot for WO-02 state 2, at **24:13**, "2 of 15
sets":

| Exercise | Set | Previous (Mon 2 Nov) | Today | Status |
|---|---|---|---|---|
| Bench Press BB | W | 40 × 10 | 40 × 10 | ✓ (plates: 10 a side) |
| | 1 | 77,5 × 8 @3 | 80 × 8 @2 | ✓ |
| | 2 | 80 × 8 @3 | hint 82,5 × 8, "up 2,5 kg", plates "25 + 5 + 1,25 per side" | **focus** |
| | 3 | 80 × 7 @2 | hint 80 × 7 (hold), plates "25 + 5" | |
| A Incline Press DB | 1–3 | 26 × 9 @2, 26 × 9 @2, 26 × 8 @1 | hint 26 × 9 | |
| B Lateral Raise DB | 1–3 | 10 × 13 @2, 10 × 13 @2, 10 × 12 @1 | hint 10 × 13 | |
| Tricep Dips | 1, 2 (F) | +10 × 9 @2, +10 × 8 @0 | hint +10 × 9 | |
| Tricep Pushdown | 1, 2, D | 30 × 11 @2, 30 × 10 @1, 20 × 12 | hint 30 × 11 | |

- Sticky note on Bench Press BB: "Arch, feet planted." Last time's note: "Kändes stark idag."
- Other snapshots: state 1 at "0:12", "0 of 15 sets", focus "NOW · BENCH PRESS BB · WARM-UP"; state 3
  rest card "1:43" of 2:00 after set 1; state 5 at "38:40", "4 of 15 sets", focus "NOW · A · INCLINE
  PRESS DB · SET 1 OF 3", "rest 1:30 after the round"; state 13 at "1:02:10", all 15 logged.
- Finish with 2 unlogged (Pushdown 2 and D): "2 sets not logged".
- Its summary after Finish with every set logged (WO-21, F-26), "Monday 9 November", "17:02–18:06 ·
  week 6 · Hypertrofi, block 1": Time 64 min · Avg RIR 1,5, goal 2–3, "13 of 14
  sets" · Sets 15 · Records 3. Logged: Bench W 40 × 10, 80 × 8 @2, 82,5 × 8 @2, 82,5 × 7 @1 ·
  Incline 26 × 10 @2, 26 × 10 @2, 26 × 9 @1 · Lateral 10 × 14 @2, 10 × 13 @1, 10 × 12 @1 · Dips
  +10 × 10 @2, +10 × 9 @0 (F) · Pushdown 30 × 12 @2, 30 × 11 @1, D 20 × 12. Records: Bench Press BB
  heaviest weight 82,5 kg (+2,5 kg); Bench Press BB estimated 1RM 110,0 kg (+0,7 kg); Lateral Raise
  DB 10 × 14 (+1 rep). Volume (History) 4 142 kg. RPE 8. Note: "Sov 7 h. Axeln okej."
  Update template alert: "Rest 1:30 → 2:00 on Incline Press DB".

**Finished workout: Push A, Mon 2 Nov 2026, 17:05–18:03** (session detail, Previous source): 58 min ·
Avg RIR 1,8 "13 of 14 sets" · 14 sets · 1 record (Lateral Raise DB 10 × 13, +1 rep) · volume
3 916 kg · RPE 7 · note "Kändes stark idag." Bench W 40 × 10, 77,5 × 8 @3, 80 × 8 @3, 80 × 7 @2;
Incline 26 × 9 @2 ×2, 26 × 8 @1; Lateral 10 × 13 @2 ×2, 10 × 12 @1; Dips +10 × 9 @2, +10 × 8 @0;
Pushdown 30 × 11 @2, 30 × 10 @1, D 20 × 12.

**History** (WO-26):
- Week 45 · 4 workouts · 22,3 km: Sun 8 Long run 09:30 · 14,21 km · 5:31 /km ★2 · Sat 7 Push B 11:00 ·
  55 min · 12 sets · 4 610 kg · Fri 6 Morning Run 07:15 · 8,04 km · 5:46 /km · Thu 5 Pull A 18:10 ·
  57 min · 13 sets · 6 420 kg · Tue 3 Ben A 17:30 · 65 min · 14 sets · 7 940 kg · Mon 2 Push A 17:05 ·
  58 min · 14 sets · 3 916 kg ★1.
- Week 44 · 4 workouts · 21,3 km: Sun 1 Long run 13,2 km · Sat 31 Push B · Fri 30 Easy run 8,1 km ·
  Wed 28 Armar (extra) · Tue 27 Ben A · Mon 26 Push A.

**Records** (last 30 days, after today): Bench Press BB 82,5 kg (9 Nov); Bench Press BB e1RM 110,0 kg
(9 Nov); Lateral Raise DB 10 × 14 (9 Nov); Squat BB 107,5 × 6 (3 Nov); Longest run 14,21 km (8 Nov);
Fastest 10 km 54:58 (8 Nov). Bench's Records tab: Heaviest weight 82,5 kg · Best e1RM 110,0 kg ·
Most volume in a set 660 kg (82,5 × 8).

**Progress** (EL-02, 8 wk): total since the first logged workout (March 2024) **998,4 t**. Main
lifts, e1RM (Epley with reps + RIR) and change over 8 weeks: Squat BB 136,2 kg (+9,5) · Bench Press
BB 110,0 kg (+3,3) · Deadlift BB 157,2 kg (+9,2) · Overhead Press BB 66,5 kg (+3,2). Volume per
week, t: w39 19,8 · w40 10,6 (D) · w41 16,2 (K) · w42 17,0 (K) · w43 19,4 · w44 18,1 · w45 22,9 ·
w46 4,1 so far; average 17,7 (full weeks). Working sets per muscle, week 45 (most sit under the
10–20 band): chest 12, back 9, shoulders 6, biceps 3, triceps 8, quads 6, hamstrings 3, glutes 0,
calves 4, core 0; this week so far: chest 6, shoulders 3, triceps 5. Against the period before:
workouts 28 vs 26, working sets 346 vs 320, volume 128 t vs 119 t, records 9 vs 6. Running (EL-03): km per
week w39–w45 18,4 · 9,0 · 19,6 · 20,7 · 21,9 · 21,3 · 22,3; long run 12,0 · 6,0 · 12,5 · 12,8 · 13,5 ·
13,2 · 14,2.

**The comparisons** (#17 choice 14b, Swedish as Carl asked, for 998,4 t; a tap shows the next):
"Lika tungt som ungefär 166 afrikanska elefanter." · "… 7 blåvalar." · "… 499 Volvo XC60:or." ·
"… 2 219 älgar." · "… 666 flodhästar." · "… 832 giraffer." · "… 1 997 isbjörnar." · "… 1 997
flygelpianon." · "… 125 Tyrannosaurus rex." · "… 83 Londonbussar." · "… 24 tomma Boeing 737:or." ·
"… 16 stridsvagnar." · "… 13 rymdfärjor." · "… 4 Frihetsgudinnor." · "… 2 rymdstationer (ISS)." ·
"Ungefär 14 % av Eiffeltornet." · "Ungefär 83 % av regalskeppet Vasa." · "… 25 kaskeloter." ·
"… 434 noshörningar." · "… 998 vithajar." · "… 1 997 hästar." · "… 1 426 mjölkkor." · "Ungefär
34 % av en Saturn V-raket." · "… 25 fullastade långtradare." · "… 73 Big Ben-klockor." The source
list (weights in tonnes) is `FUN` in `docs/design/17-strength/index.html`. English form if needed:
"As heavy as about 166 African elephants."

**GPS run: "Morning Run", Friday 6 November 2026, 07:15, Djurgården, Stockholm.** 8,04 km · moving
46:20 · elapsed 47:05 · avg pace 5:46 /km · avg pace over total time 5:51 /km · 540 kcal · avg HR
146 · max HR 163 · elevation gain 38 m, max 41 m · iPhone · ASICS Novablast 4.
- Splits /km: 1 5:58 · 2 5:49 · 3 5:44 · 4 5:47 · 5 5:41 · 6 5:45 · 7 5:44 · 8 5:39 (fastest) ·
  0,04 km 0:13.
- Route shape: a clockwise loop of about 2,4 × 1,2 km, starting and ending at the same point on the
  west side, a long straight along the water on the south edge, a bulge north-east around a park,
  then back along a road to the start.
- The long run, Sunday 8 Nov 09:30: 14,21 km · 1:18:23 · 5:31 /km · gain 96 m · Nike Pegasus 41
  (records: longest run, fastest 10 km 54:58).
- Live run snapshot (RU-02): 3,42 km · 19:41 · current pace 5:38 /km · avg 5:45 /km.

**Gear.** Shoes: ASICS Novablast 4 — 412 km, default for runs, warn at 700 km · Nike Pegasus 41 —
712 km, past its 700 km warning · retired: Brooks Ghost 15 — 803 km. Bike: Bianchi Via Nirone 7 —
2 140 km (starting distance; rides come with #26).

**Devices and apps.** Apple Health — linked, last sync today 07:02 · Apple Watch — not linked ·
Suunto — not linked · Garmin, COROS, Polar — not linked · Strava — not linked · Hevy — error
"Could not reach Hevy".

**Library extras.** Marks (In my programmes): Yes on Bench Press BB, Squat BB, Deadlift BB, Overhead
Press BB, Pull Up, Incline Press DB, Lateral Raise DB, Romanian Deadlift DB; Swap on Seated Row
Cable (alternative Single Row DB); Test on Bulgarian Split Squat DB; No on Leg Extension Machine.
Last performance in rows: Bench Press BB "82,5 × 8", Incline Press DB "26 × 10", Squat BB "107,5 × 6",
Pull Up "+5 × 6". A timed exercise: Plank (time, "0:45"). A distance exercise: Walking Lunge
("20 m"). Per side: Bulgarian Split Squat DB. Own exercise to create in F-46 (not in the 553): "Copenhagen
Plank" (primary Core, pattern Core, Bodyweight, type Time, per side). The sketches' "Reverse Lunge DB"
would duplicate the library's Rear Lunge DB.

**Export sample.** Workouts, last 30 days: 18 workouts, 9 runs.

---

## 6. Open points

Decisions #14 and #17 leave to the design or to Carl. Options only; none is resolved here.

- **O-01 · The later screens.** Draw import, sync and nutrition (EL-15–EL-19) in full now, or only
  their v1 entry points (the disabled rows, SH-05)? #2 says their parents design them; the brief asks
  for the finished product.
- **O-02 · Exercise detail from the workout.** Q15 opens it from the exercise name; Carl's point 3
  made the name fold the exercise. Options: an info button by the name; a row in the ⋯ menu; a long
  press on the name.
- **O-03 · An added warm-up set's weight** (#48, #4 Open). Copy the last set's weight (today); Q22's
  ladder (bar × 10, 50 % × 5, 70 % × 3, 85 % × 1); empty with a hint.
- **O-04 · Onboarding's pages and in-context hints** (#25). Three to five pages; ask units and body data
  in onboarding or at first need; which first-use hints exist.
- **O-05 · The live run's largest number** (#18: "the one number Carl names"). Distance, time, current
  pace, average pace.
- **O-06 · Running scope for v1** (#18). Flyover replay and 3D (costliest), map types, save route;
  whether the live map is full screen, a panel, or swipeable away; map provider and tile licence (#5).
- **O-07 · Can a run be minimised** like a workout, and does it get a Live Activity? (#14 "#18's call";
  #43 leaves runs to #5.)
- **O-08 · Start and Home on a rest or run day** (Q46 assumes a planned workout). Today's bubble hidden,
  shows the run, or shows the next planned workout.
- **O-09 · Home's content** (#25 not drawn). Today's card plus the week's numbers (the token preview), or
  Plan's week card repeated, plus food later.
- **O-10 · Export format** (#6): JSON, CSV, GPX for runs; whether the format is chosen on the screen or
  fixed; period presets.
- **O-11 · Creating a programme** (Q38, not sketched): an E1-style form sheet, or a short step-by-step.
- **O-12 · A run day in Plan** (Q38: "a run, type and target; #18 draws it"): which run types and
  targets (Easy run 8 km, Intervals, Long run), and whether its day card starts the run.
- **O-13 · Where gear lives** (#52): You, Settings, or both.
- **O-14 · Where the main lifts are picked** (Q45): Progress's ⋯, Settings, or an edit button on the
  lift tiles.
- **O-15 · Profile fields** (#14, #25: "body data and goals"): which fields, and whether body weight
  feeds bodyweight exercises.
- **O-16 · Which run records** (choice 14, #18 decides): longest distance, fastest 1, 5 and 10 km,
  longest time, most climb.
- **O-17 · Running in Progress** (#18 completes Q45's "km per week and the long run").
- **O-18 · Session detail for a run vs a workout** in History: the same screen family or RU-05 alone
  (Q29 makes summary and detail one screen for workouts).
- **O-19 · A missing illustration** (decision 31: Carl's images arrive one by one): RepDB's flat image
  as fallback (#4), a muscle-group glyph, or nothing.
- **O-20 · Dragging the workout down to minimise** (choice 5: only ⌄ drawn) and the swipe-to-delete
  animation's polish (Carl, choice 5).
- **O-21 · A superset of three or more** (giant set): letters A, B, C, or limited to two.
- **O-22 · Activity types** (#26, later): one "Activity" bubble that opens the list, last used first,
  and History's filter growing beyond Strength · Running.
- **O-23 · Template share format** (choice 8's Dela): a file, a link, or text.
- **O-24 · Records from the last 30 days**: tap opens the exercise, the workout, or a full records list.
- **O-25 · How "sets done" counts** in the header: with or without warm-up sets.
- **O-26 · How far designs 2 and 3 may move the navigation.** The brief gives each design its own
  layout and navigation; #14 decided five tabs with Start as a round action in the middle. Options:
  keep #14's five places and Start in the middle, restyled; keep the five places but put Start
  elsewhere (Apple's own fix: a Start button in Home's and Plan's title bars); any navigation that
  keeps every feature within two taps and today's workout within one.

result: inventory/done — 85 screens, 64 flows, 26 open points
