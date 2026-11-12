"""Give every board the moment layer's stylesheet after the system bundle, so a board that imports a changed one (the mode
sheets, the comparison page) renders it. Skips boards that already have it and the boards named on the command line."""
import pathlib, sys
P = pathlib.Path(__file__).resolve().parent.parent / "canvas" / "project"
LINK = '<link rel="stylesheet" href="ds/strive1/components/bundle.css">'
skip = {f"{n}.dc.html" for n in sys.argv[1:]}
n = 0
for f in sorted(P.glob("*.dc.html")):
    s = f.read_text()
    if f.name in skip or "moments.css" in s:
        continue
    assert s.count(LINK) == 1, f.name
    f.write_text(s.replace(LINK, LINK + '\n<link rel="stylesheet" href="moments.css">')); n += 1
print("linked", n, "boards")
