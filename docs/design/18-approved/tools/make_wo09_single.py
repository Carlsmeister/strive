"""One-off: WO-10-one = WO-01-picked (one exercise) with WO-09-menu's menu, minus Create superset (WO-10's 'one exercise left')."""
import pathlib, re
P = pathlib.Path(__file__).resolve().parent.parent / "canvas" / "project"
menu_src = (P / "WO-09-menu.dc.html").read_text()
base = (P / "WO-01-picked.dc.html").read_text()

start = menu_src.index('<a class="scrim clear"')
i = menu_src.index('<div class="menu', start)
depth, pos = 0, i
for m in re.finditer(r"<(/?)div\b[^>]*>", menu_src[i:]):
    depth += -1 if m.group(1) else 1
    if depth == 0:
        pos = i + m.end()
        break
block = menu_src[start:pos]
row = re.search(r'<a class="mrow" href="WO-10\.dc\.html"[^>]*>.*?</a>\n?', block, re.S)
assert row, "Create superset row not found"
block = block.replace(row.group(0), "").replace('href="WO-02-s2.dc.html"', 'href="WO-01-picked.dc.html"')
assert "Create superset" not in block

end = base.rindex("</div>\n</x-dc>")
out = base[:end] + block + "\n" + base[end:]
out = re.sub(r"<title>[^<]*</title>", "<title>Menu, one exercise</title>", out, count=1)
(P / "WO-10-one.dc.html").write_text(out)
print("written", len(out))
