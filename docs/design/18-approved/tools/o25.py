"""O-25, run once (2026-11-10): the header, the minimised bar and Home's card count working sets only.
Push A's warm-up is its first set, so a count N >= 1 drops by one and every total drops by one.
Armar (WO-12-db) has no warm-up and stays."""
import pathlib, re
D = pathlib.Path(__file__).resolve().parent.parent
SKIP = {"WO-12-db.dc.html"}
pat = re.compile(r"\b(\d+)( of |/)(15|18|19|13|4)( sets\b)")


def fix(m):
    return f"{max(int(m.group(1)) - 1, 0)}{m.group(2)}{int(m.group(3)) - 1}{m.group(4)}"


total = 0
for f in sorted(list((D / "canvas/project").glob("*.dc.html")) + [D / "boards.md", D / "motion.json"]):
    if f.name in SKIP:
        continue
    s = f.read_text()
    new, k = pat.subn(fix, s)
    if k:
        f.write_text(new)
        total += k
print("replaced", total)
