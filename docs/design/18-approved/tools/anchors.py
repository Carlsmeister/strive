"""Show the anchors on boards that link to targets, with the text after them: python3 anchors.py BOARD=T1,T2 [BOARD=...]
Names without .dc.html; a target is a prefix; '*' shows every anchor."""
import pathlib, re, sys
P = pathlib.Path(__file__).resolve().parent.parent / "canvas" / "project"
for spec in sys.argv[1:]:
    board, targets = spec.split("=")
    s = (P / f"{board}.dc.html").read_text()
    print(f"== {board}")
    for t in targets.split(","):
        pat = r'<a [^>]*href="%s[^"]*"[^>]*>' % ("" if t == "*" else re.escape(t))
        for m in re.finditer(pat, s):
            tail = re.sub(r"<[^>]+>", " ", s[m.end():m.end() + 300])
            print(f"{m.group(0)[:200]} …{' '.join(tail.split())[:70]}")
