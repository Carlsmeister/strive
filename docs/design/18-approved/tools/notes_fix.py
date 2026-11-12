"""Rewrite design-1.md after the review's fix round (2026-11-10), once: header, review fixes, coverage table, flows,
open points, polishes, missing, progress log, result line. Usage: python3 notes_fix.py COV.md (coverage.py --table's output)."""
import pathlib, re, sys
D = pathlib.Path(__file__).resolve().parent.parent
N = D / "design-1.md"
COV = pathlib.Path(sys.argv[1]).read_text().strip("\n")
s = N.read_text()


def section(name, text):
    """Replace the body of '## name' up to the next '## '."""
    global s
    m = re.search(r"(^## %s[^\n]*\n)(.*?)(?=^## |\Z)" % re.escape(name), s, re.S | re.M)
    assert m, name
    s = s[:m.start(2)] + text.strip("\n") + "\n\n" + s[m.end(2):]


head_end = s.index("## The language in five lines")
s = """# Design 1 — Strive as built, polished and completed

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

""" + s[head_end:]

s = s.replace("""- Sources of truth on disk: `design-1/boards.md` (the registry: every board, what it shows, where each tap leads; the
  canvas index is built from it), `design-1/tools/` (lint, coverage and flow checks, previews).""",
"""- Sources of truth on disk: `design-1/boards.md` (the registry: every board, what it shows, where each tap leads),
  `design-1/flows.json` (every flow as board paths), `design-1/tools/` (lint, coverage, `flows.py`, previews).""")

# The coverage table: from its header to the line before "States drawn elsewhere".
a = s.index("| Screen | States in the inventory")
b = s.index("States drawn elsewhere")
s = s[:a] + COV.lstrip("\n") + "\n\n" + s[b:]

section("Flows wired", """
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
""")

s = s.replace("- O-02: an \"Exercise details\" row (ⓘ) in the exercise ⋯ menu opens EX-05 as a sheet — two taps.",
"""- O-02: an "Exercise details" row (ⓘ) after Exercise settings in the exercise ⋯ menu opens EX-05 as a sheet — two taps.
- O-07: the run's Live Activity in the Dynamic Island, expanded: distance, time, pace, Pause and Lap (RU-02-island).
- O-12: the day's Run… submenu lists Easy, Long, Tempo, Intervals; picking one opens a prompt with Distance | Time and a
  value (PL-08-runmenu, -runtarget); the day then reads "Tempo · 6 km" (PL-08-tempo).
- O-21: a giant set is one superset lettered A, B, C under one "Superset" label (WO-02-abc).""")

s = s.replace("""- **Not recorded:** the subagents who drew the workout, library and history boards (WO-01, WO-03–WO-17, WO-21, WO-24,
  WO-26, EX-01–EX-08) stopped at the usage limit before reporting their polish lists. Their boards follow the built
  structure as far as I checked them, but **diff each against its Kotlin file before treating it as the spec.**""",
"""- **Exercise menu** (WO-09): an "Exercise details" row after Exercise settings (built: no such row).
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
  - Not diffed: Home, Plan and You, and colours, spacing and radii anywhere.""")

s = s.replace("""- **Swipe to delete** (WO-14): `error-container` / `on-error-container` as Parts.kt does; dark-HC error red #ff6961
  (the earlier #ffd1cc turned the revealed action into a near-white slab).""",
"""- **Swipe to delete** (WO-14): `error-container` / `on-error-container` as Parts.kt does; dark-HC `error-container`
  #ff6961 with black (built #ffd1cc turned the revealed action into a near-white slab). Destructive text stays light:
  dark-HC `error` #ffbdb6, 7:1 on every container (built #ffd1cc).""")

section("What is missing or not verified", """
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
""")

s = s.replace("result: design-1/done — https://claude.ai/artifact/D938qJPYBGyUgVKonitcsE", """- Fix round (after the review): O-25 counts, dark-HC `error` #ffbdb6, island tokens, spring values (system v7); exercise
  menu order; contradictions; relinks; four subagents drew 31 boards and redrew 8 (workout, plan, exercises/You) and
  diffed copy and structure against `origin/dev`; 12 rewordings reverted; `flows.json` and `tools/flows.py` check every
  flow tap by tap: 64 of 64 play. Lint: 423 boards, 7 with the known findings (WO-02-s2's scroll holes; six map and island
  boards with little text). Canvas v12.

result: design-1/fixed — https://claude.ai/artifact/D938qJPYBGyUgVKonitcsE — 10 of 10 findings fixed, 64 flows play""")
N.write_text(s)
print("ok", len(s))
