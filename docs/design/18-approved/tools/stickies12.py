"""Strive 1+2's motion notes for the rows where something is won: written to motion.json (the source) and to the row
stickies in canvas.json (m-<page>-<ID>), so both say the same."""
import json, pathlib
D = pathlib.Path(__file__).resolve().parent.parent
WIN = "the win spring (dampingRatio 0.45, stiffness 420)"
NOTES = {
 "SH-02": "Motion · Home · Strive 1+2\nStart Push A: the button presses (scale .96, 100 ms), then the workout rises from the bottom as a full-screen sheet on the calm spring; Home stays behind it. Continue grows the minimised bar into the workout (350 ms). Cards appear without entrance animation: Home must feel instant.\nToday done: the first time Home shows after Finish, the card's lime edge and ✓ pop once on " + WIN + ". A full week plays once on This week (MO-week). Reduce Motion: colour only.",
 "WO-02": "Motion · logging (the signature moment) · Strive 1+2\n✓ or Log set: press 100 ms (scale .96), then the ✓ fills and pops 1 → 1.14 → 1 on " + WIN + ", a light haptic; the logged row's edge flashes lime 2 px and fades in 400 ms; the header's count rolls up. The tint slides to the next row and the rest card rises on the calm spring (design 1's, 350 ms); the focus card's values cross-fade (250 ms). (MO-log)\nA record: six sun rays burst by the ✓ (300 ms), the sun toast drops from under the bar on the win spring and counts the value up in Unbounded (600 ms), a success haptic; it holds 2.5 s and leaves on the calm spring. (MO-record)\nAll logged: \"14 of 14 sets\" and the overview's n/n ✓ turn lime. Reduce Motion: cross-fades and colour only.",
 "WO-06": "Motion · rest timer · Strive 1+2\nThe ring drains in 1 s linear ticks; the digits change without animation (tabular figures). − / + nudge the ring on the calm spring. Last 3 s: the ring and digits stay teal and pulse 1.04 once a second. At 0: the ring closes and turns lime with the card's edge, popping on " + WIN + "; sound, long buzz and a success haptic; \"Rest over. Set 3 is yours.\" It holds about 600 ms, then settles 8 pt and fades (calm). Skip: fades at once. Reduce Motion: only the digits and colours change. (MO-rest)",
 "WO-21": "Motion · summary · Strive 1+2\nPushes in from the right after Finish (calm). \"Nailed it, Carl.\" fades in and rises 8 pt; the four tiles stagger in 50 ms apart on " + WIN + "; the Records tile's sun edge glows once; the record stars pop in sequence. An exercise row expands with a height spring (calm). RPE segments fill on tap (100 ms). Reduce Motion: cross-fades only. (MO-finish)",
 "RU-05": "Motion · run summary · Strive 1+2\nThe route draws itself once (800 ms ease-out), then the stats sheet rises over the map's lower half (calm) while the distance rolls 0 → 8.04 km (700 ms) and a lime check pops by the title on " + WIN + ". A run with records: the records card's sun edge glows once and its stars pop. The sheet scrolls up over the map into the sections. Reduce Motion: cross-fades only. (MO-finish)",
 "EL-02": "Motion · Progress · Strive 1+2\nThe total counts up once (800 ms) in Unbounded and the curve draws left to right. A tap on the comparison line cross-fades to the next line (250 ms), a light haptic. One rule: a tap shows the next, in the list's order, looping; never on a timer, nowhere but Progress (EL-02-cmp). Period changes re-draw the bars from the baseline (calm spring, 20 ms stagger). A tapped bar turns teal and its value appears above it.",
}
m = json.loads((D / "motion.json").read_text())
c = json.loads((D / "canvas" / "project" / "canvas.json").read_text())
for sid, text in NOTES.items():
    assert sid in m, sid
    m[sid] = text
    keys = [k for k in c["notes"] if k.startswith("m-") and k.endswith("-" + sid)]
    assert len(keys) == 1, (sid, keys)
    c["notes"][keys[0]]["text"] = text
(D / "motion.json").write_text(json.dumps(m, ensure_ascii=False, indent=1) + "\n")
(D / "canvas" / "project" / "canvas.json").write_text(json.dumps(c, ensure_ascii=False, indent=1) + "\n")
print("updated", len(NOTES), "rows")
