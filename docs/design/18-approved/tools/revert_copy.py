"""Put built copy back where a board reworded it without a reason (polish-diff.json, 2026-11-10). Safe to run again.
Every other difference in polish-diff.json is kept and listed as a polish in design-1.md."""
import pathlib
P = pathlib.Path(__file__).resolve().parent.parent / "canvas" / "project"
# (boards prefix or "" for all, board text, built text): texts are matched with their tag edges where a substring could collide.
REVERTS = [
    ("", "Shown first in quick picks and in the picker.", "The coach may programme it."),
    ("", "Its alternative is suggested first.", "The coach prefers its alternative."),
    ("", "Never suggested, and hidden in the library.", "Never programmed, and hidden in the library."),
    ("", "Marked Yes in your programmes", "Yes in My programmes"),
    ("", ">No, both sides<", ">No<"),
    ("", ">Olympic, 20 kg<", ">20 kg<"),
    ("WO-08", ">Rename workout<", ">Rename<"),
    ("WO-08", ">Started at<", ">Change start time<"),
    ("WO-10", ">Superset with…<", ">Create superset<"),
    ("WO-07", ">Custom time<", ">Custom time…<"),
    ("WO-16", ">Discard<", ">Discard workout<"),
    ("", "Workout note: energy, sleep, pain", "Workout note"),
]
n = 0
for f in sorted(P.glob("*.dc.html")):
    s = t = f.read_text()
    for pre, a, b in REVERTS:
        if f.name.startswith(pre):
            t = t.replace(a, b)
    if t != s:
        f.write_text(t)
        n += 1
print(n, "boards changed")
