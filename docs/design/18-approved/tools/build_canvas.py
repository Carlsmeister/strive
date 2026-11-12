"""Build canvas/project/canvas.json from boards.md (the registry) and motion.json (the motion notes beside each row).
Every board is a 390 × 844 phone frame; a page per area; a row per screen ID with a title note above it and a motion sticky at its end."""
import json, re, datetime, pathlib, sys
HERE = pathlib.Path(__file__).resolve().parent.parent
REG = (HERE / "boards.md").read_text()
MOTION = json.loads((HERE / "motion.json").read_text()) if (HERE / "motion.json").exists() else {}
EXTRA = json.loads((HERE / "extra-boards.json").read_text()) if (HERE / "extra-boards.json").exists() else {}
DS_URL = "https://claude.ai/artifact/3cuFKpGnj48MxnEphtVvav"
DS_VERSION = sys.argv[1] if len(sys.argv) > 1 else None

NAMES = {
 "SH-01": "Tab bar and app frame", "SH-02": "Home", "SH-03": "Start bubbles", "SH-04": "Start › From template", "SH-05": "Food tab",
 "SH-06": "Onboarding", "SH-07": "First-use hints", "SH-08": "Permission prompts",
 "WO-01": "Active workout, empty", "WO-02": "Active workout, exercise in focus", "WO-03": "Active workout, scrolled", "WO-04": "Logging dock",
 "WO-05": "Keypad", "WO-06": "Rest timer card", "WO-07": "Free rest menu", "WO-08": "Workout menu", "WO-09": "Exercise menu",
 "WO-10": "Create superset", "WO-11": "Order and skip", "WO-12": "Exercise settings", "WO-13": "Set type menu", "WO-14": "Swipe to delete a set",
 "WO-15": "Finish sheet", "WO-16": "Discard workout alert", "WO-17": "Minimised workout bar", "WO-18": "Rest notification",
 "WO-19": "Live Activity: lock screen", "WO-20": "Live Activity: Dynamic Island", "WO-21": "Workout summary", "WO-22": "Update template alert",
 "WO-23": "Save as template", "WO-24": "Session detail", "WO-25": "Session detail, editing", "WO-26": "History",
 "PL-01": "Plan tab", "PL-02": "Plan menus", "PL-03": "Template card menu", "PL-04": "Template preview", "PL-05": "Template editor",
 "PL-06": "Template keypad", "PL-07": "Archived templates", "PL-08": "Programme · Weeks", "PL-09": "Programme · Weights", "PL-10": "Programme · Log",
 "PL-11": "Programme · Settings", "PL-12": "Create programme",
 "EX-01": "Exercise library", "EX-02": "Library filter sheet", "EX-03": "Exercise picker", "EX-04": "Swap exercise", "EX-05": "Exercise detail · About",
 "EX-06": "Exercise detail · History", "EX-07": "Exercise detail · Chart", "EX-08": "Exercise detail · Records", "EX-09": "Create exercise",
 "EX-10": "Edit exercise", "EX-11": "Merge data",
 "RU-01": "Run start", "RU-02": "Live run", "RU-03": "Live run, paused", "RU-04": "Finish run", "RU-05": "Run summary and run detail",
 "RU-06": "Run summary, sections", "RU-07": "Map, full screen", "RU-08": "Flyover replay", "RU-09": "Edit run",
 "EL-01": "You", "EL-02": "Progress · Strength", "EL-03": "Progress · Running", "EL-04": "Choose main lifts", "EL-05": "Profile", "EL-06": "Settings",
 "EL-07": "Plates and bars", "EL-08": "Devices and apps", "EL-09": "Apple Health link", "EL-10": "Gear", "EL-11": "Gear detail and edit",
 "EL-12": "Choose gear", "EL-13": "Export", "EL-14": "About Strive", "EL-15": "Import (later)", "EL-16": "Account and sync (later)",
 "EL-17": "Food today (later)", "EL-18": "Log food (later)", "EL-19": "Nutrition targets (later)", "O-22": "Activity types (later)",
 "MO": "Motion", "MD": "All modes",
}
PAGES = [("shell", "Shell"), ("workout", "Workout · logging"), ("workout-more", "Workout · around it"), ("workout-after", "Workout · after"),
         ("plan", "Plan"), ("exercises", "Exercises"), ("running", "Running"), ("you", "You"), ("later", "Later"), ("motion", "Motion"),
         ("modes", "All modes")]

# Parse the registry: each '## … · page `id`' section, then its table rows.
boards, rows = {}, {}
page = None
for line in REG.splitlines():
    m = re.match(r"## .*page `([a-z-]+)`", line)
    if m:
        page = m.group(1); continue
    if page and line.startswith("| ") and not line.startswith("| file") and not line.startswith("|---"):
        cells = [c.strip() for c in line.strip().strip("|").split("|")]
        f, title = cells[0], cells[1]
        sid = title.split(" · ")[0]
        rows.setdefault(page, {}).setdefault(sid, []).append((f, title))
for p, extra_rows in EXTRA.items():
    for sid, items in extra_rows.items():
        rows.setdefault(p, {}).setdefault(sid, []).extend([(f, t, w, h) for f, t, w, h in items])

GAP_X, ROW_H = 80, 844 + 300
canvas_boards, order, notes = {}, [], {}
for pid, _ in PAGES:
    y = 0
    for sid, items in rows.get(pid, {}).items():
        x = 0
        for it in items:
            f, title = it[0], it[1]
            w, h = (it[2], it[3]) if len(it) > 2 else (390, 844)
            canvas_boards[f + ".dc.html"] = {"x": x, "y": y + 260, "w": w, "h": h, "title": title, "page": pid, "is_interactive": True}
            order.append(f + ".dc.html")
            x += w + GAP_X
        nid = re.sub(r"[^A-Za-z0-9_-]", "-", f"t-{pid}-{sid}")[:40]
        if sid.startswith("MD"):
            text = "All modes · " + " · ".join(it[1].split(" · ")[0] for it in items)
        elif sid == "MO":
            text = "Motion · the signature moments, timed"
        else:
            text = f"{sid} · {NAMES.get(sid, '')}"
        notes[nid] = {"x": 0, "y": y, "text": text, "kind": "title1", "maxW": max(x - GAP_X, 900), "page": pid}
        if sid in MOTION:
            mid = re.sub(r"[^A-Za-z0-9_-]", "-", f"m-{pid}-{sid}")[:40]
            notes[mid] = {"x": x, "y": y + 260, "text": MOTION[sid], "w": 420, "size": "m", "fill": "teal", "page": pid}
        tallest = max((it[3] if len(it) > 2 else 844) for it in items)
        y += 260 + tallest + 160

notes["readme"] = {"x": -560, "y": 260, "w": 480, "size": "m", "fill": "gray", "page": "shell", "text":
    "Strive 1 — Strive as built, polished and completed.\n\nEvery board is one screen state at 390 × 844. Each opens in dark with Increase Contrast "
    "(Carl's phone). Switch a board's mode in Tweaks: dark-hc · dark · light · light-hc. The All modes page shows each screen in the other three.\n\n"
    "Prototype: press Play on Main (Home) and tap. Every flow in the inventory is wired; a tap on a set's Previous stands in for the swipe that "
    "deletes it, and a scroll in the workout folds the focus card into the strip.\n\nLabels: screen ID · state · default mode. The decisions, the "
    "polish list and the coverage are in design-1.md."}

now = datetime.datetime.now(datetime.timezone.utc).replace(microsecond=0).isoformat().replace("+00:00", "Z")
out_path = HERE / "canvas" / "project" / "canvas.json"
created = {"v": 1, "at": now}
if out_path.exists():
    created = json.loads(out_path.read_text()).get("createdOnFiles", created)
canvas = {"v": 3, "createdOnFiles": created, "title": "Strive 1", "launch": {"view": "canvas", "page": "shell"},
          "pages": [{"id": i, "name": n} for i, n in PAGES], "boards": canvas_boards, "order": order, "notes": notes,
          "designSystems": [{"title": "Strive 1 system", "namespace": "strive1", "artifact": DS_URL, "version": DS_VERSION, "copiedAt": now}]}
out_path.write_text(json.dumps(canvas, indent=1, ensure_ascii=False))
print(len(canvas_boards), "boards,", len(notes), "notes")
