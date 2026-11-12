"""2026-11-12, Carl: drop the drawn comparison icons. The comparison text stays exactly as it is.
EL-01's Progress row gets design 1's chart icon back; the funline on EL-02-8wk/-next/-1yr loses its icon; EL-02-cmp lists
the 25 lines without icons. Run once: python3 icons_off.py (asserts it finds each icon exactly where expected)."""
import pathlib, re
PROJ = pathlib.Path(__file__).resolve().parent.parent / "canvas" / "project"
SVG = re.compile(r'<svg class="m-sun m-ic".*?</svg>', re.S)


def text(s):  # visible text, to prove it did not change
    return re.sub(r"<[^>]+>", "", s)


def edit(name, fn, n):
    p = PROJ / f"{name}.dc.html"
    s = p.read_text()
    assert len(SVG.findall(s)) == n, (name, len(SVG.findall(s)))
    out = fn(s)
    assert not SVG.search(out), name
    p.write_text(out)
    return s, out


s, out = edit("EL-01", lambda s: SVG.sub('<span class="ic ic-chart s18"></span>', s), 1)
assert text(s) == text(out)
for b in ["EL-02-8wk", "EL-02-next", "EL-02-1yr"]:
    s, out = edit(b, lambda s: SVG.sub("", s), 1)
    assert text(s) == text(out)

OLD_CAP = ("Each line has its own drawn icon: 24 grid, stroke 1.75, currentColor, no fill, round caps and joins, 20 pt before "
           "the line in the record accent.")
NEW_CAP = "No icons: each line is text only, as on Progress."
OLD_LI = "grid-template-columns: 24px 1fr; column-gap: 12px; "


def cmp(s):
    assert s.count(OLD_CAP) == 1 and s.count(OLD_LI) == 25
    s = s.replace('<link rel="stylesheet" href="moments.css">\n<link rel="stylesheet" href="moments.css">',
                  '<link rel="stylesheet" href="moments.css">')  # a doubled head link
    return SVG.sub("", s).replace(OLD_LI, "grid-template-columns: 1fr; ").replace(OLD_CAP, NEW_CAP)


s, out = edit("EL-02-cmp", cmp, 25)
lines = lambda s: re.findall(r'<span class="body-medium">(.*?)</span>', s)
assert lines(s) == lines(out) and len(lines(out)) == 25
print("icons removed: EL-01, EL-02-8wk, EL-02-next, EL-02-1yr, EL-02-cmp")
