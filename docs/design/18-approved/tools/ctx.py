"""Raw markup around a regex on boards: python3 ctx.py 'REGEX' BOARD [BOARD ...]  (before 160, after 120 chars)."""
import pathlib, re, sys
P = pathlib.Path(__file__).resolve().parent.parent / "canvas" / "project"
for b in sys.argv[2:]:
    s = (P / f"{b}.dc.html").read_text()
    s = s[s.find("<x-dc>"):]
    for m in re.finditer(sys.argv[1], s):
        print(f"{b} | {s[max(0, m.start() - 160):m.end() + 120]!r}")
