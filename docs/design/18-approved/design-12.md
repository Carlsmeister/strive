# Design 12 — Strive 1+2 (design 1 with design 2's life)

**Status: done — 2026-11-10.** The canvas holds design 1's 423 boards plus 4 new boards and 32 comparison boards
(459 boards, all published, version 5). **36 boards differ from Strive 1:** 32 changed and 4 new. All 64 flows play,
checked step by step (`tools/flows.py --check`). Lint shows design 1's 7 known findings and nothing new.

**Changed 2026-11-12 (Carl's pick of 1+2):** the drawn comparison icons are gone. Carl keeps 1+2 for its playful,
personal copy, but the sun elephant looked odd on You's Progress row and beside the comparison line. The comparison text
is unchanged. Canvas version 19 (`1794510204-1c25`).

- Canvas: **Strive 1+2** — https://claude.ai/artifact/Vcf9tb4Mafp5r8yqxu92TC. It opens on the page **"1 vs 1+2"**. To use
  the prototype, press Play on Main, which is on the Shell page.
- System: design 1's **Strive 1 system** (https://claude.ai/artifact/3cuFKpGnj48MxnEphtVvav, v `1794298481-934d`),
  installed unchanged at `ds/strive1/`.
- The borrowed layer lives in `moments.css` on the canvas, not in a new system artifact. The task keeps design 1's tokens
  unchanged; the accents sit on top of them. Moving them into the system is a step for the build.
- Design 1's canvas and system are untouched.

## The mix in five lines

- **Kept from design 1:**
  - tokens and the teal palette
  - type scale, density, every layout and the navigation
  - Increase Contrast handling
  - set kinds' colours
  - teal for everything that can be tapped
- **Colour energy in moments only**, in `moments.css`:
  - The accents are sun (a record), flame (a streak) and lime (done: a finished workout or run, the rest ending, a full
    week).
  - An accent marks an edge, an icon or a number. `.moment` draws the edge as an inset, so nothing changes size.
  - Dark and light add a 12–14 % wash under the edge. **Dark-HC and light-HC get the edge only, never a fill.**
  - Contrast is checked by script: edges and icons are at least 3:1 on every surface role, numbers at least 4.5:1, and
    at least 7:1 in the -hc modes. Two flame values were moved to pass.
- **One borrowed face:** Unbounded 700, used only on a record's value and the total lifted. It is set at .88 em so it
  takes no more room. Inside `small` and `.val` it also gets `line-height: 1`, so it does not raise the line.
- **Springs in one convention:**
  - **Win:** `spring(dampingRatio = 0.45f, stiffness = 420f)`, which is design 2's 420/18. That gives about 20 %
    overshoot. SwiftUI: `.spring(response: 0.31, dampingFraction: 0.45)`.
  - **Calm:** design 1's `spring(0.6f, 400f)`.
  - The béziers are web fallbacks only. Reduce Motion keeps cross-fades and colour only.
- **Design 2's voice** appears in headlines, empty states, onboarding, the summary and records:
  - "Nailed it, Carl.", "Rest day. Legs up.", "Your first one writes page one.", "Two records. Fast legs."
  - No exclamation marks and no emoji.
  - Labels and buttons stay exact.

## The three review findings carried from design 2: not repeated

- **Rotating comparison:**
  - One rule: a tap on Progress's comparison line shows the next one, in section 5's order, and loops. Never on a timer.
  - You shows no rotating line. Its Progress row has design 1's chart icon and the total.
  - **No comparison has an icon** (removed 2026-11-12). The line is text only, as in design 1.
  - **EL-02-cmp lists all 25** with line and implied weight.
- **Springs:** every spring is given as dampingRatio + stiffness, with the bézier marked as fallback. This holds in
  `moments.css`, on every motion board and in every row sticky.
- **Near-white fills in dark-HC:** none. In dark-HC, every accent is an edge, an icon stroke or a number on design 1's
  surface roles. Nothing is filled: no card, ring track, slab or button.

## Every board that differs from Strive 1

The same list is on the canvas: one board per row, showing design 1's version next to 1+2's with this caption (`diffs.json`).

**Home, onboarding, empty states** (`tools/group_a.py`: text swaps, plus one moment):
- SH-02-rest: "Rest day" → "Rest day. Legs up."; "Nothing planned today." → "Nothing planned. Recovery counts too."
- SH-02-done:
  - The done card gets a lime edge and a lime ✓.
  - "3 records" is in sun.
  - "Done today · Week 6 of 8" → "Done. Big day. · Week 6 of 8".
- SH-02-first: "Welcome, Carl" → "Welcome, Carl."; the caption becomes "Page one is blank. Make a template, or start empty
  and add exercises as you go."
- SH-06-welcome: "Built by Carl for his own training." → "Carl built this to lift with."
- SH-06-units: the caption becomes "Kilos or pounds, kilometres or miles? Switch either one later in Settings."
- SH-06-body:
  - "About you" → "About you. Last bit."
  - The caption becomes "Optional. Your weight counts in bodyweight exercises like Tricep Dips."
  - "Change it any time in You." → "Skip it now and add it later in Profile."
- WO-26-empty: the caption becomes "Your first one writes page one. Workouts and runs land here, week by week."
- EL-01-first: the caption becomes "Your first one writes page one."; the Progress row reads "Every kilo lands here,
  elephants included".
- EL-02-first: the caption becomes "Your first one writes page one. Lift a little and the elephants start lining up."
- EL-03-empty: the caption becomes "Your first run draws the map. Km per week, long runs and records start counting here."
- EX-06-empty: "No workouts yet" → "Page one is blank".
- EX-07-empty: "Log a workout to see your chart" → "No line yet. Log a workout to see your chart".
- EX-08-empty: "Log a workout to see records." → "Your first set of Chest Press Machine sets them all."
- PL-10-empty: the caption becomes "Write down what the block teaches you. Future you reads it at the next deload."
- EL-10-none: the caption becomes "Add your shoes and every run adds its km to them. Strive tells you when a pair is worn out."

**Logging and rest:**
- WO-02-s12:
  - The record toast is a sun moment: sun edge, sun star, and "82.5 kg" in Unbounded.
  - Set 2's star is sun.
  - Toast text: "New record · Bench Press BB · 82.5 kg" → "New record · 82.5 kg on Bench Press BB".
  - The toast's drop shadow is added back inline next to the edge.
- WO-02-s13: "14 of 14 sets" gets a lime check and lime text, and the overview's n/n ✓ marks turn lime. Finish stays teal.
- WO-06-ended:
  - The ring and 0:00 are lime, and the card has a lime edge. The ring's track and the glass stay design 1's.
  - "Rest over · set 3 next" → "Rest over. Set 3 is yours."

**Summary, records, run summary:**
- WO-21-records and WO-21-expanded:
  - "Monday 9 November" (headline) → "Nailed it, Carl.". The date moves into the meta line.
  - "Week 6 · Hypertrofi, block 1" leaves the header and stays readable in the Week row. With the date added, it would
    wrap and add a line.
  - The Records tile gets a sun edge and star.
  - The record values are in Unbounded sun.
  - "3 records" → "Three records. Big day.".
  - On -expanded, the ★ on 82.5 × 8 and the star counts are sun too.
- WO-21-plain: "Nailed it, Carl."; the date moves into the meta line. No accent, because nothing was won.
- WO-21-empty: "Off-script, Carl." for a workout started from scratch. No accent.
- EX-08: the Records card gets a sun edge; today's stars and "Mon 9 Nov" dates are sun; the three values are in Unbounded.
- EL-03: the two records from the 8 Nov run (Longest distance, Fastest 10 km) are sun with Unbounded values. The other
  records stay plain.
- RU-05-done: a lime check by "Morning Run"; the Splits header becomes "Strong finish. Km 8 was the fastest." (true to the
  sample: 5:39 is the fastest split).
- RU-05-history: "Records" → "Two records. Fast legs."; the records card gets a sun edge; the values are in Unbounded.

**Progress:**
- EL-02-8wk, EL-02-next, EL-02-1yr: 998.4 t is in Unbounded. The comparison line is design 1's: same text, same colour,
  no icon.
- EL-01: 998.4 t is in Unbounded. The Progress row keeps design 1's chart icon.

**Motion boards:**
- MO-log, redrawn:
  - The ✓ pops 1 → 1.14 → 1 on the win spring.
  - The row flashes a lime edge.
  - The count rolls from "1 of 14" to "2 of 14".
  - The tint and the rest card move on the calm spring.
  - Title "Logging a set, and a record" → "Logging a set"; the record moved to MO-record.
- MO-rest, redrawn:
  - The last 3 s pulse 1.04, on the calm spring and still teal.
  - At 0:00 the ring and the edge pop lime and the new label appears.
  - The card holds for 600 ms, then fades and drops 8 pt (calm).
- **New:**
  - MO-record: the ✓ pops, six sun rays burst, and the sun toast drops with 4 px overshoot while it counts 80 → 82.5 kg. It
    holds for 2.5 s, then the summary's records stagger in.
  - MO-finish: the summary pushes in, the tiles stagger on the win spring, the Records tile glows, and the stars pop. On
    the run summary, 0 → 8.04 km rolls up and a lime check appears.
  - MO-week: Sunday's mark pops lime, the seven days pulse in turn, "Full week" pops in, and the flame tag ticks
    "1 → 2 weeks in a row". The streak is an example, and the board says so.
  - EL-02-cmp: the 25 comparisons.
- Each motion board has timed key frames, a live loop, a Springs box and Reduce Motion.

**Not visible on a board, but changed:**
- Every board links `moments.css` in its head, so the mode sheets and comparison boards that import a changed board
  render it.
- Asset blob ids were rewritten in 52 boards, for the new canvas's copied assets.
- The row stickies for SH-02, WO-02, WO-06, WO-21, RU-05 and EL-02 now give 1+2's motion (`tools/stickies12.py`, also in
  `motion.json`).
- The Motion page title is updated.
- The page "1 vs 1+2" was added and the canvas opens on it.

**Copy changes to built screens** (each one is a change in the code): every "old → new" pair above on WO-02, WO-06 and
WO-21. Home, onboarding and You are not built yet.

## Choices to check (said plainly)

- **Home's "Last week: …" line is unchanged.** It is on 18 boards (Home under every overlay), and Monday's Home is not where
  the week is won. The full-week moment is MO-week.
- **No streak on Monday's Home.** The sample gives none, because week 44 missed Pull A.
- Record stars that stay on screen (History, Home's Latest, lists) keep design 1's amber. Only the moment a record is set
  uses sun.
- The comparison line on EL-02-1yr is not linked. In the prototype, only EL-02-8wk ↔ EL-02-next rotate, the same as
  F-53 in design 1.
- In EL-03, Longest time and Most climb also say 8 Nov but stay plain. The sample counts two records from that run.
- **EL-01's Progress row got design 1's chart icon back** rather than an empty slot. Every row in that list leads with an
  icon, so a gap would read as a bug. Carl asked only for the elephant to go.
- In MO-week, the Home frame's "4 of 4 · 22 of 22 km · Long run today" is example data for Sunday 15 Nov.

## What is missing or not verified

- **The canvas was not seen rendered.** As in design 1, no Chrome extension was connected. Every changed board was checked
  locally with headless Chrome against the same stylesheets, in dark-HC first:
  - groups B, C and D by their subagents in all four modes
  - spot checks by me, with Unbounded loaded: WO-21, EL-02-8wk, EX-08, EL-03, WO-02-s12 and WO-06-ended against design 1
    in dark-HC; SH-02-done, WO-06-ended and RU-05-history in dark, light and light-HC
- **Not checked on the canvas itself:**
  - that `<dc-import>` works with hyphenated names on the 32 comparison boards; the right half of each depends on it,
    the same risk as design 1's mode sheets
  - that `moments.css` loads as a canvas support file
  - the Unbounded Google Fonts link
  - the CSS live loops; a static screenshot catches them at their invisible first frame, and only MO-record's loop was
    seen moving
- The light modes of group A were checked only on SH-02-done.

## Progress log

- 2026-11-10 (first session): copied `design-1/` to `design-12/`, then stopped at the usage limit.
- 2026-11-10 (this session):
  1. Read design 2's SPEC and the three carried findings.
  2. Created the canvas and copied design 1's 9 assets into it, rewriting their ids in 52 boards. Wrote `moments.css` and
     checked its contrast.
  3. Froze design 1's version of every board that would change in `v1-snap/`.
  4. Did group A myself (15 boards).
  5. Sent groups B, C and D to three subagents with `agent-brief-12.md`.
  6. Published v2–v3: the index, `ds/strive1/`, `moments.css` and 402 boards.
  7. Checked C and D with the font loaded and fixed `.celebrate`'s line height. Checked B.
  8. Wrote the stickies and built the 32 comparison boards (`tools/vs.py`).
  9. Ran lint (7 known findings) and the flows (64 of 64).
  10. Published v5: index, `moments.css`, 21 redrawn boards, 4 new boards, 4 with only the head link changed, and 32
      comparison boards.

- 2026-11-12 (icons removed, `tools/icons_off.py`):
  1. Found every board with a comparison icon beside its text: EL-01, EL-02-8wk, EL-02-next, EL-02-1yr and EL-02-cmp.
     EL-01-first, EL-02-first, EL-04-three and EL-04-five name elephants in text only, so they did not change.
  2. Removed the 29 icons. The script asserts that each board's visible text is unchanged. On EL-02-cmp, each card's
     grid dropped its icon column, the caption now says "No icons", and a doubled `moments.css` head link went.
  3. The four boards now differ from design 1 only by the Unbounded total, which was checked by diff against `v1-snap/`.
     Rendered in all four modes, dark-HC first, and checked against the version with icons.
  4. Rewrote the four captions in `diffs.json` and rebuilt the comparison boards with `tools/vs.py`. Only vs-EL-01 and
     vs-EL-02-8wk, -next and -1yr changed. The EL-02 motion sticky no longer says the icon pops in (`stickies12.py`,
     `motion.json`, canvas note `m-you-EL-02`).
  5. Built `canvas.json` from the live version, which the page had re-saved since v5. Only that note's text differs.
  6. Lint: the same 7 known findings as before. Flows: 64 of 64.
  7. Published 10 files: 5 boards, 4 comparison boards and the index. The 36 boards that differ from Strive 1 are
     unchanged in number.
  8. `tools/preview_wide.py` wrote its temp page to another job's folder; it now uses `$CLAUDE_JOB_DIR/tmp`.

result: design-12/done — https://claude.ai/artifact/Vcf9tb4Mafp5r8yqxu92TC — 36 boards changed
result: design-12/icons-removed — https://claude.ai/artifact/Vcf9tb4Mafp5r8yqxu92TC — 9 boards changed
