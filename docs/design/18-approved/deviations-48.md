# Deviations on #48's boards

Every difference between a board and its simulator shot that #48 does not fix: the element, the difference, why, and the issue
that owns it. Shots come from `WorkoutShotTest` into `/tmp/strive-board-shots`; `python3 tools/compare.py <ID>` renders the board
beside them. dark-HC is compared first, then dark. The pull request quotes this list.

**Remaining differences per board**, after Task 7 compared the seven boards of the issue (WO-02-s2, WO-03-strip, WO-03-end,
WO-04-new, WO-04-heavy, WO-05-plates, WO-13-warmup) and the two that draw its plates elsewhere (WO-13-moved, WO-01-picked),
dark-HC then dark. A board counts lines, not elements: one for each line below that names it. Lines owned "Not a difference" and
lines whose board is "None" are not counted. A difference under 1 pt is not listed, except a stroke weight. One line shows in dark alone, the glass's teal
glow; every other line holds in both modes. Positions are in pt on the 390 × 844 board, read off the renders and shots side by
side, so within about 1 pt. 40 in all.

**What this file leaves to deviations-42.** Six of the nine boards (WO-01-picked, WO-02-s2, WO-03-strip, WO-03-end, WO-04-new,
WO-13-moved) are #42's too, and every deviations-42 row on them not marked "Closed by #48" still stands there and is counted there
(WO-04-new's 10, for one). This file repeats a #42 row for them only where it bears on the plates and bar line, or where #48 moved
its numbers, so the counts below are this file's lines alone. The rows not repeated are the text rows (Finish, the workout's name
and its pill, the header's meta row, the focus label, values and Edit and Log labels, the exercise names and notes, the set rows'
text, the dock's field values and button label) and every other open row on those six boards. For the
three new boards (WO-04-heavy, WO-05-plates, WO-13-warmup) this file is the only list: the same text rows hold on them, element
for element, and are counted here in the first line below (text widths and placement); every other difference is a line of its own.

| Boards | Remaining differences |
|---|---|
| WO-01, WO-02, WO-03 | 01-picked 3 · 02-s2 3 · 03-strip 3 · 03-end 2 |
| WO-04, WO-05 | 04-new 4 · 04-heavy 8 · 05-plates 7 |
| WO-13 | 13-moved 4 · 13-warmup 6 |

The plates line, the dock's bar line, the Plates key and its plate mode, and WO-13-moved's 55 × 3 rung match their boards: the
sleeve's plates, sizes and colours (the last keyed plate `primary` on WO-05-plates), "per side", the wrap on WO-04-heavy, the
"Bar 20" pill, and the greyed 15. What is left is listed here.

| Board | Element | Difference | Reason | Owner |
|---|---|---|---|---|
| All 9 | Text widths and placement, the plates' text included | "25 + 5 + 1.25 per side", "4 × 25 + 1.25", "10 per side", "Bar 20" and the plate keys' labels run 1–2 pt wider, and WO-05-plates' "25 + 5 + 1.25" 2.7 pt (inked 93.3 against 90.7), as names and values do (deviations-42's text rows). Up and down, the focus card's values ink 1.0 pt higher at their top (242.0 against 243.0, foot 270.3 against 270.7 on WO-04-heavy, WO-05-plates and WO-13-warmup, dark-HC), as deviations-42 measures on WO-02-s2 | As deviations-42's WO-02-s2: Compose draws SF at its text optical size, the board at the display size; the values' rise is the line placement | #42 |
| WO-01-picked, WO-02-s2, WO-03-strip, WO-04-new, WO-04-heavy, WO-05-plates, WO-13-moved, WO-13-warmup | The sleeve's screen-reader description | The sleeve under a set and in the dock carries a description listing the plates, where the board hides the drawing (`aria-hidden`), so VoiceOver reads the plates twice: once from the sleeve, once from the text beside it | Controller ruling: kept. The plan mandates it and the acceptance test reads it | #48 |
| WO-02-s2, WO-03-strip, WO-13-moved, WO-13-warmup | Bench Press BB's Plan note | "Plan Skulderbladen ihop." is not drawn, so the rows below it sit 20 pt higher (WO-02-s2's set 2 badge 750.0–778.0 against 769.7–797.7, so its centre 764 against 784; the plates line's text 796.7–808.7 against 816.3–828.3) | No template cue exists in the data, and the sample fixture cannot carry one: `ExerciseHeader`'s `plan` has no caller until #44 (P3) | #44 |
| WO-03-end | The list's end | The board shows set 2 with its plates line at the top; the shot shows the overview there, 222 pt higher, so the plates line is off screen. WO-03-strip shows it drawn | As deviations-42's WO-03-end: the list keeps 320 pt under "Discard workout" where the board keeps 48 | Carl to settle |
| WO-04-new, WO-04-heavy, WO-05-plates | The dock's foot | The dock sits 34–35 pt lower (WO-04-heavy's top 497 against 463, WO-05-plates' 370 against 335), so the shot shows more above it: Bench Press BB's or Deadlift BB's header on WO-04-new and heavy, the RIR row on WO-05-plates. Inside the dock every gap and height matches with the shot moved up 34 | The test window has no home-indicator inset; on a phone `navigationBarsPadding` adds it (deviations-42) | Test harness (no home-indicator inset) |
| WO-04-heavy, WO-05-plates | The dock's top edge | Both draw the light hairline along the dock's top on the same path, but the board's fades down the 28 pt corners towards the screen's edge and the shot's holds. In dark-HC, the left corner's stroke stays above luma 45 all the way down in the shot (WO-05-plates 374.7–396.7 pt, WO-04-heavy 502.7–524.7) and drops under it after 8–15 pt on the board (340.3–355.3, 468.3–483.3) | As deviations-42's WO-04-new, which counts it there: the board's `.dock` keeps only `.glass`'s top border, which Chrome tapers round the radius; `glass` strokes its hairline at one width and one colour | #42 |
| WO-04-new, WO-04-heavy, WO-05-plates, WO-13-warmup | The glass's teal glow, dark only | Shows through the dock and the set type menu, narrower and weaker than the board's | As deviations-42: the shared `Modifier.glass` | Carl to settle |
| WO-05-plates | The dock's foot under Next | The list's "Rest 2:00" shows through the dock's glass at the window's foot (about 836–844 pt), where the board's dock is plain glass with nothing legible behind it. Legible in dark; faint in dark-HC (text luma 31 on a 27–28 background, about 1.1:1) but readable in a crop | The shared `Modifier.glass` lets list text show through, as on #42's WO-05-weight (dark) | Carl to settle |
| WO-05-plates | The plate keys' and Empty's outline | ⅓ pt on the board (25 … 1.25, ⌫, the greyed 15, Empty), ⅔ pt in the shot, same colour, both modes | The keys stroke the shared hairline border (0.5 pt, 2 px at 3×) in the keypad's key style, which the 123 keypad shares; the board's 0.5 px renders 1 px. The hairline value is generated | Carl to settle |
| WO-04-heavy | The header's count | "3 of 17 sets" where the board reads "1 of 14 sets" | The board keeps WO-02-s2's header; Deadlift BB adds three sets, two logged | #48 |
| WO-04-heavy | The focus card's meta row | "last set" where the board reads "rest 3:00 after", and its dock reads Rest 2:00 | The board contradicts itself, and set 3 of 3 is Deadlift BB's last set, which reads "last set" (#42, polish-diff's WO-03). The shot keeps the default rest, 2:00, as the dock draws it | #48 |
| WO-04-heavy | The focus card's Log button | "Log set 3" where the board reads "Log set 2" | The board keeps WO-02-s2's label on Deadlift BB's set 3 | #48 |
| WO-13-warmup | The set type menu's place and width | 264 pt wide at x 8–272, opened down from the W badge (612–836), where the board draws it 250 wide at 16–266, opened up over the focus card (400–585) | As deviations-42's WO-13-menu: the shared `GlassMenu` has one width and opens below its button while the room lasts | #86 |
| WO-13-warmup | The menu's caption | "The set stays where it is." heads the menu; the board draws none | **Contradiction between boards, for Carl:** the fixture's warm-up is logged (40 × 10 ✓), and WO-13-logged draws the caption for a logged set, as the inventory's WO-13 and design-1 describe it; WO-13-warmup leaves it off. The app follows WO-13-logged | Carl to settle |
| WO-13-moved | The next warm-up's badge | Filled amber, where the board fills it teal | As deviations-42's WO-13-moved: WO-01-picked fills it amber | Carl to settle |
| WO-01-picked | Sets 1 and 3 | 77.5 × 8 and 80 × 7, last Monday's sets, where the board draws 80 × 8 three times | As deviations-42: equal weights are #45's progression rule | #45 |
| None (no board draws it) | The plates line under logged barbell sets | Drawn only under the next (current) set of an exercise with a bar and a weight; logged sets have none | **Planner ruling 6:** the issue also says "under logged barbell sets"; every board draws the line under the next set alone, and the board wins | #48 |
| None (no board draws it) | The gym's bars | Stored in Settings beside its plates, as weights per unit (kg 20, 15, 7.5, 25; lb 45, 35, 15, 55); None is always offered and never stored. A stored weight takes its name from the named bar that weighs it (Olympic, Short, EZ, Hex), else reads "Bar 10". The dock's bar menu and WO-12's pull-down read the stored list | **Planner ruling 7.** Editing both lists is EL-07's screen | #63 |
| None (no board draws it) | A weight the plates cannot make, and the bar alone | "Not loadable with your plates" / "Går inte att lasta med dina skivor"; the bar alone reads "Bar only" / "Bara stången" | **Planner ruling 9** | #48 |
| None (no board draws lb plate mode) | lb plate mode's keys | Six lb keys (45, 35, 25, 10, 5, 2.5) fill one row and two columns of the next, so ⌫ lands in column 3 and column 4 stays empty | The kg layout puts ⌫ after the plates, as WO-05-plates draws it; lb has one key fewer | #48 |
| None (no board draws it) | The dock's bar menu | The shared `GlassMenu`, anchored to the "Bar 20" pill | No board draws the menu open | #86 |
| WO-05-plates | Plate mode's small keys | Hide and 123 do not apply the board's 12 pt side padding | Every label fits the keys' 88 pt minimum, so nothing renders differently | Not a difference |
