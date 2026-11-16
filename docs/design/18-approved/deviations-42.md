# Deviations on #42's boards

Every difference between a board and its simulator shot that #42 does not fix: the element, the difference, why, and the issue
that owns it. Shots come from `WorkoutShotTest` into `/tmp/strive-board-shots`; `python3 tools/compare.py <ID>` renders the board
beside them. dark-HC is compared first, then dark. The pull request quotes this list.

| Board | Element | Difference | Reason | Owner |
|---|---|---|---|---|
| WO-02-s2 | Bench Press BB's Plan note | "Plan Skulderbladen ihop." is not drawn | No template cue exists in the data; the row is drawn only with one (P3) | #44 |
| WO-02-s2 | Plates line under set 2 | "25 + 5 + 1.25 per side" with its sleeve is not drawn | Plates are out of scope | #48 |
| WO-02-s2 | Focus card's values "82.5" and "8" | Drawn about 6 pt wider in all ("82.5" 81.7 pt against 76) at the same 700 38/44 and -1 tracking; smaller text is within 1–2 pt | Not found in our code: both draw the system font, and tabular figures are off as the board's `font:` sets them. Unverified guess: Chrome picks SF Pro's Display optical size at 38 px and Skia does not | Open: no issue yet |
| WO-02-rir | Bench Press BB's Plan note | Not drawn | As WO-02-s2 (P3) | #44 |
| WO-02-rir | Focus card's values | About 6 pt wider | As WO-02-s2 | Open: no issue yet |
| WO-02-rir | Plates line under set 2 | Not drawn | As WO-02-s2 | #48 |
| WO-02-note | Bench Press BB's Plan note | Not drawn | As WO-02-s2 (P3) | #44 |
| WO-02-note | Plates line under set 2 | Not drawn | As WO-02-s2 | #48 |
| WO-02-note | Strip under the bar | The board draws the strip with the list scrolled 72 pt under the bar; the shot shows the focus card's foot there and no strip | The app folds the card into the strip only once the card has scrolled wholly away (D-scroll), so no scroll position draws both the strip and the header where the board puts it. The shot keeps the header, notes and field where the board draws them | #42 (D-scroll's rule, unchanged) |
| WO-02-note | Keyboard and its Done bar | Not drawn | The system's keyboard; a test window has none | Not ours (iOS) |
