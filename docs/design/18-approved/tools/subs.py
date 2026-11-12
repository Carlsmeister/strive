"""Exact text substitutions on boards: python3 subs.py EDITS.json
EDITS.json maps a board file (in canvas/project) to [[old, new], ...]; every old must occur, or nothing is written."""
import json, pathlib, sys
P = pathlib.Path(__file__).resolve().parent.parent / "canvas" / "project"
edits = json.loads(pathlib.Path(sys.argv[1]).read_text())
out = {}
for f, subs in edits.items():
    t = (P / f).read_text()
    for a, b in subs:
        assert a in t, (f, a)
        t = t.replace(a, b)
    out[f] = t
for f, t in out.items():
    (P / f).write_text(t)
print(len(out), "boards changed")
