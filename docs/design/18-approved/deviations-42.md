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
| WO-02-note | Strip under the bar | The board draws the strip with the focus card scrolled under the bar; the shot shows the card's foot there and no strip | This issue's own trigger: the strip shows only once `firstVisibleItemIndex > 1` (`WorkoutScreen.kt`), so while the card's foot is still under the bar there is no strip. The shot keeps the header, notes and field where the board draws them | #42, Task 6 |
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
