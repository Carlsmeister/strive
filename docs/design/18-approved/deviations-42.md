# Deviations on #42's boards

Every difference between a board and its simulator shot that #42 does not fix: the element, the difference, why, and the issue
that owns it. Shots come from `WorkoutShotTest` into `/tmp/strive-board-shots`; `python3 tools/compare.py <ID>` renders the board
beside them. dark-HC is compared first, then dark. The pull request quotes this list.

| Board | Element | Difference | Reason | Owner |
|---|---|---|---|---|
| WO-02-s2 | Bench Press BB's Plan note | "Plan Skulderbladen ihop." is not drawn | No template cue exists in the data; the row is drawn only with one (P3) | #44 |
| WO-02-s2 | Plates line under set 2 | "25 + 5 + 1.25 per side" with its sleeve is not drawn | Plates are out of scope | #48 |
| WO-02-s2 | Focus card's values "82.5" and "8" | Drawn about 6 pt wider ("82.5" 81.7 pt against 76) at the same 700 38/44 and -1 tracking. Names run wider too: "Push A" (700 28) 94 pt against 88, "Bench Press BB" (600 17) 129 against 124; labels, meta and rows are within 1–2 pt | The optical size, measured for the values: in Chrome, `system-ui` 700 38px draws "82.5" 76 pt wide (SF Pro picks its Display optical size at 38 px), and the same text with `font-variation-settings: "opsz" 17` (Text) draws it 82 pt wide, the shot's width. Compose draws the system font at its Text optical size at every size. That does not explain the 17 pt name, where both are at Text size: a second cause there is not measured. No style value in our code explains either: weight, tracking and figures match the board's | #42 |
| WO-02-rir | Bench Press BB's Plan note | Not drawn | As WO-02-s2 (P3) | #44 |
| WO-02-rir | Focus card's values | About 6 pt wider | As WO-02-s2: the optical size | #42 |
| WO-02-rir | Plates line under set 2 | Not drawn | As WO-02-s2 | #48 |
| WO-02-note | Bench Press BB's Plan note | Not drawn | As WO-02-s2 (P3) | #44 |
| WO-02-note | Plates line under set 2 | Not drawn | As WO-02-s2 | #48 |
| WO-02-note | Keyboard and its Done bar | Not drawn | The system's keyboard; a test window has none | Not ours (iOS) |
| WO-02-s3 | Bench Press BB's Plan note | Not drawn, so the rows below sit 20 pt higher | As WO-02-s2 (P3) | #44 |
| WO-02-s3 | Plates line under set 2 | Not drawn | As WO-02-s2 | #48 |
| WO-02-s3 | Rest card | Sits 34 pt lower at the same 152 pt height (board 641.7–793.7 pt, shot 676.0–828.0 pt, dark-HC); colours match | The rest card's own look and place | #43 |
| WO-02-s3 | Focus card's values | About 6 pt wider | As WO-02-s2: the optical size | #42 |
| WO-02-s4 | Rest card | Sits 34 pt lower, as WO-02-s3 (board 641.7–793.7 pt, shot 676.0–828.0 pt) | The rest card's own look and place | #43 |
| WO-02-s4 | Focus label and values | The label 1–2 pt wider, the values as WO-02-s2 | As WO-02-s2: the optical size | #42 |
| WO-02-s5, s5b, s5c | Focus label and values | As WO-02-s4 | As WO-02-s2: the optical size | #42 |
| WO-02-abc | Focus label and values | As WO-02-s4 | As WO-02-s2: the optical size | #42 |
| WO-02-s5, WO-02-abc | A superset's stacked headers | The headers stand 34 pt apart, so each header's 44 pt targets (the ⋯ and the name that folds it) overlap the next header's by 10 pt. The later header takes the overlap, which leaves the ones above an effective 34 pt where the plan's rule is 44 pt | The board's spacing was kept: the board wins on a value | Carl to settle (board vs 44 pt rule) |
| WO-15-several | Tricep Pushdown's "Set 2 of 2" | The app now reads "set 2 of 3": the "of" counts every set but warm-ups | **Contradiction between boards, for Carl:** WO-02-abc and its registry row draw Pushdown's 1, 2 and D as "set 1 of 3", O-25's count. WO-15-several is #58's sheet and not shot here | #58 |
| WO-02-s9f | Focus card's meta row | The app reads "last set" on Tricep Dips' set 2 of 2; the board reads "rest 1:30 after" | **Contradiction between boards, for Carl:** P6 and the acceptance criterion read "last set" on an exercise's last set, as WO-02-s9d and WO-02-s12 draw it. s9f alone draws the rest | Carl to settle |
| WO-02-s9f, s9d, s10bw, s10td, s15 | Focus label and values | As WO-02-s4: the values about 6 pt wider, the label 1–2 | As WO-02-s2: the optical size | #42 |
| WO-02-s9f, WO-02-s10bw | The dock for a bodyweight set | The rows and the card draw the added weight ("+10", "+kg"), but the dock has no added-weight field, so a row's +kg opens the dock on Reps | **Contradiction for Carl:** the boards and the inventory's WO-04 ("added weight") want the field; the plan's `FlowTest.aBodyweightExerciseLogsRepsOnly` asserts the dock has no weight field, and the dock is Task 8's (WO-04/05), which draws no bodyweight board | Carl to settle (Task 8) |
| WO-02-s9f to s15 | Header's meta row | The count runs wider (s9f: "10 of 14 sets" 67.7 pt against 61), so on s13 the lime check and count start 4 pt further right | The same text-width difference as WO-02-s2's names; weight, size and colours match (lime (214, 255, 138) dark-HC, (196, 245, 92) dark) | #42 |
| WO-02-s13 | "Discard workout" | 6 pt wider at the same 600 17, and its ink 1.7 pt lower (573.0 against 571.3 pt) in the same 44 pt target under the same Add exercises (498–548 in both) | The width as WO-02-s2's names; the 1.7 pt is the text button's own inner layout, not chased | #42 |
| WO-02-s14 | Bench Press BB's Plan note, plates line | Not drawn | As WO-02-s2 | #44, #48 |
| WO-02-s14 | Overview's "Tricep Dips · Skipped" | Not in the shot: the board draws it below its 844 pt window, and the list composes nothing there. The row reads "Tricep Dips · Skipped" in grey 400 with no count, as `.nm.skip` | The board's window | Not a difference |
| WO-02-s15 | RIR on a time exercise's focus card | The board draws the RIR segments for Copenhagen Plank; the app draws none for a timed exercise | **Contradiction between boards, for Carl:** WO-02-s10td draws Plank's card without RIR, and RIR counts reps in reserve, which a time has none of | Carl to settle |
| WO-03-strip, WO-03-rest | Bench Press BB's Plan note, plates line | Not drawn, so the rows below sit 20 pt higher | As WO-02-s2 | #44, #48 |
| WO-03-rest | Rest card | Sits 34 pt lower, as WO-02-s3 | The rest card's own look and place | #43 |
| WO-03-end | Space under the list's end | Scrolled to the end, the list keeps 320 pt under "Discard workout"; the board keeps 48 (`.body`'s inline `padding-bottom`), so the board shows Set 2 to Discard where the shot shows the overview to Discard, 222 pt higher | The 320 pt predates this issue and lets the last rows scroll above the rest card and the dock. Not changed without a ruling: it is the list's behaviour, not WO-03-end's look | Carl to settle |
| WO-03-end | Plates line under set 2 | Not drawn | As WO-02-s2 | #48 |
