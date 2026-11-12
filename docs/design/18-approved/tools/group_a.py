"""Strive 1+2, group A: design 2's voice on Home, onboarding and empty states, at design 1's sizes (text swaps only, plus the
done-today moment on SH-02-done). Every old string must occur exactly once on its board; run it once on design 1's boards."""
import pathlib
P = pathlib.Path(__file__).resolve().parent.parent / "canvas" / "project"
LINK = '<link rel="stylesheet" href="ds/strive1/components/bundle.css">'
CHANGES = {
    "SH-02-rest": [(">Rest day<", ">Rest day. Legs up.<"), ("Nothing planned today.", "Nothing planned. Recovery counts too.")],
    "SH-02-done": [
        (LINK, LINK + '\n<link rel="stylesheet" href="moments.css">'),
        ('<section class="card" aria-label="Done today">', '<section class="card moment m-lime" aria-label="Done today">'),
        ("Done today · Week 6 of 8", "Done. Big day. · Week 6 of 8"),
        ('<span class="ic ic-check s20 muted"></span>', '<span class="ic ic-check s20 m-ic"></span>'),
        ('<span class="prs"><span class="ic ic-star s12"></span>3 records</span>',
         '<span class="prs m-sun"><span class="ic ic-star s12 m-ic"></span><span class="m-num">3 records</span></span>')],
    "SH-02-first": [(">Welcome, Carl<", ">Welcome, Carl.<"),
                    ("Make a template for the workout you do most, or start empty and add exercises as you go.",
                     "Page one is blank. Make a template, or start empty and add exercises as you go.")],
    "SH-06-welcome": [("Built by Carl for his own training.", "Carl built this to lift with.")],
    "SH-06-units": [("Pick what your gym and your watch use. You can change it later in Settings.",
                     "Kilos or pounds, kilometres or miles? Switch either one later in Settings.")],
    "SH-06-body": [(">About you<", ">About you. Last bit.<"),
                   ("Optional. Bodyweight exercises use your weight.", "Optional. Your weight counts in bodyweight exercises like Tricep Dips."),
                   ("Change it any time in You.", "Skip it now and add it later in Profile.")],
    "WO-26-empty": [("Your workouts and runs land here, week by week.", "Your first one writes page one. Workouts and runs land here, week by week.")],
    "EL-01-first": [("Your workouts and runs show here, newest first.", "Your first one writes page one."),
                    ("Starts with your first workout", "Every kilo lands here, elephants included")],
    "EL-02-first": [("Your total, your main lifts and your weekly volume start with your first workout.",
                     "Your first one writes page one. Lift a little and the elephants start lining up.")],
    "EL-03-empty": [("Km per week, your long runs and your run records show here after your first run.",
                     "Your first run draws the map. Km per week, long runs and records start counting here.")],
    "EX-06-empty": [(">No workouts yet<", ">Page one is blank<")],
    "EX-07-empty": [("Log a workout to see your chart", "No line yet. Log a workout to see your chart")],
    "EX-08-empty": [("Log a workout to see records.", "Your first set of Chest Press Machine sets them all.")],
    "PL-10-empty": [("Notes about this block, dated. What the numbers will not tell you later.",
                     "Write down what the block teaches you. Future you reads it at the next deload.")],
    "EL-10-none": [("Add your shoes to see how far each pair has gone. A run gets the default pair without a tap.",
                    "Add your shoes and every run adds its km to them. Strive tells you when a pair is worn out.")],
}
for board, pairs in CHANGES.items():
    f = P / f"{board}.dc.html"; s = f.read_text()
    for old, new in pairs:
        assert s.count(old) == 1, f"{board}: {old!r} occurs {s.count(old)} times"
        s = s.replace(old, new)
    f.write_text(s)
print(len(CHANGES), "boards,", sum(map(len, CHANGES.values())), "swaps")
