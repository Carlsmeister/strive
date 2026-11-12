"""Bring boards.md's titles and links cells in line with the review fixes (relink.py, the redraws), once."""
import pathlib
R = pathlib.Path(__file__).resolve().parent.parent / "boards.md"
EDITS = [  # (row file, old text in the row, new text)
    ("SH-02-rest", "Start Pull A anyway → WO-02-s1", "Start Pull A anyway → WO-02-pulla"),
    ("SH-02-inprog", "TAB(Home); Continue", "TAB(Home) except Plan → WO-17-plan; Continue"),
    ("SH-03-rest", "Pull A → WO-02-s1", "Pull A → WO-02-pulla"),
    ("SH-04-four", "Ben A → WO-02-s1; others → WO-02-s1", "Ben A → WO-02-benA; Pull A → WO-02-pulla; Push A → WO-02-s1; Push B not wired"),
    ("SH-04-many", "any → WO-02-s1", "Ben A → WO-02-benA; Pull A → WO-02-pulla; Push A → WO-02-s1; the others not wired"),
    ("SH-05", "nothing to tap. | TAB(Food)", "and **See the food log (later)**. | TAB(Food); See the food log → EL-17-notarget"),
    ("SH-07-start", "Got it → Main", "Got it → Main; Start Push A → SH-07-check"),
    ("WO-01-picked", "Log → WO-02-s3", "Log and W ✓ → WO-01-logged; Finish → WO-16-empty"),
    ("WO-06-round", "Skip → WO-02-s5", "Skip → WO-02-s5c; ⌄ → WO-17-rest"),
    ("WO-09-menu", "rows Exercise details (ⓘ, O-02), Sticky note, Rest for all sets ›, Create superset, Order and skip, Exercise settings, Remove exercise (red)",
     "rows Sticky note; Rest for all sets ›, Create superset, Order and skip; Exercise settings, Exercise details (ⓘ, O-02); Remove exercise (red)"),
    ("WO-09-menu", "Note → WO-02-s2;", "Note → WO-02-note; Sticky note → WO-09-sticky; Remove exercise → WO-02-removed;"),
    ("WO-17-plan", "TAB(Plan)", "TAB(Plan) except Home → SH-02-inprog"),
    ("WO-17-rest", "TAB(Home)", "TAB(Home) except Plan → WO-17-plan"),
    ("WO-23-summary", "Save → PL-01-week", "Save → PL-01-saved"),
    ("WO-25-edit", "a cell → WO-05-weight", "a cell → WO-25-dock"),
    ("PL-01-week", "Push A card → PL-04-pusha;", "Push A card → PL-04-pusha; Pull A card → PL-04-pulla;"),
    ("PL-01-short", "⋯ → PL-02-plan", "⋯ → PL-02-short"),
    ("PL-03-menu", "Archive → PL-07-list", "Archive → PL-03-archived"),
    ("PL-06-reps", "Next → PL-06-rir", "RIR cell, Next → PL-06-rir"),
    ("PL-06-rir", "Next → PL-05-edit", "Same for the rest → PL-06-same"),
    ("PL-06-same", "Done → PL-05-edit", "Done → PL-01-axlar; a reps cell → PL-06-reps; Cancel → PL-05-discard"),
    ("PL-08-daymenu", "Rest → PL-08-changed", "Run… → PL-08-runmenu; Rest → PL-08-changed"),
    ("EX-01-filter", "filter → EX-02-db", "filter → EX-02-db; Incline Press DB → EX-05-incline"),
    ("EX-03-none", "New → EX-09-empty", "New → EX-09-picker"),
    ("EX-03-one", "Lateral row → EX-03-two", "Lateral row → EX-03-two; New → EX-09-picker"),
    ("EX-03-two", "Add (2) → WO-02-s2", "Add (2) → WO-02-s2; New → EX-09-picker"),
    ("EX-03-search", "Add → WO-02-s15", "Add (1) → WO-02-s15; New → EX-09-picker"),
    ("EX-03-tmpl", "Add (2), Superset (2) → PL-05-edit", "Add (2) → PL-05-added; Superset (2) not wired"),
    ("EX-04", "Chest Press Machine → WO-02-s2", "Chest Press Machine → WO-02-swap"),
    ("EX-11-alert", "Merge → EX-05", "Merge → EX-11-done"),
    ("EL-01", "\"Import · Sync — later\" disabled", "Import and Account and sync as two \"Later\" rows"),
    ("EL-01", "Import → EL-15-source", "Import → EL-15-source; Account and sync → EL-16-out"),
    ("EL-02-8wk", "comparison → EL-02-1yr", "comparison → EL-02-next"),
    ("EL-02-6mo", "1 yr → EL-02-1yr", "1 yr → EL-02-1yr; a bar → EL-02-6mobar"),
    ("EL-02-1yr", "1 year, next comparison", "1 year"),
    ("EL-12", "Pegasus → EL-12-other", "Novablast → EL-12-other"),
]
lines = R.read_text().split("\n")
for f, a, b in EDITS:
    i = next(k for k, l in enumerate(lines) if l.startswith(f"| {f} |"))
    assert a in lines[i], (f, a)
    lines[i] = lines[i].replace(a, b, 1)
i = next(k for k, l in enumerate(lines) if l.startswith("- **F1**"))
lines[i] = lines[i].replace("⌄ → `SH-02-inprog`", "⌄ → `SH-02-inprog` (`WO-17-rest` while a rest runs)", 1)
R.write_text("\n".join(lines))
print(len(EDITS), "registry edits")
