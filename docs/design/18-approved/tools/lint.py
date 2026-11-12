"""Lint Strive 1 boards: python3 lint.py [BOARD.dc.html ...] (default: every board in canvas/project).
Flags a board that is half-written or would break the canvas: no closing tags, a hole other than {{mode}}, a link to a missing
board, an unknown icon or class-less screen, literal colours, emoji, unbalanced tags, a nested <a>."""
import html.parser, json, pathlib, re, sys
HERE = pathlib.Path(__file__).resolve().parent.parent
PROJ = HERE / "canvas" / "project"
BUNDLE = (HERE / "system" / "project" / "components" / "bundle.css").read_text()
ICONS = set(re.findall(r"\.ic-([a-z0-9-]+)\s*\{", BUNDLE)) | set(re.findall(r"\.ic-([a-z0-9-]+)[,{ ]", BUNDLE))
VOID = {"area", "base", "br", "col", "embed", "hr", "img", "input", "link", "meta", "source", "track", "wbr"}
EMOJI = re.compile("[%s-%s%s-%s%s-%s]" % (chr(0x1F300), chr(0x1FAFF), chr(0x2600), chr(0x26FF), chr(0x2714), chr(0x27BF)))  # ✓ (2713) is a text glyph


class P(html.parser.HTMLParser):
    def __init__(self):
        super().__init__()
        self.stack, self.errs, self.a_depth = [], [], 0

    def handle_starttag(self, tag, attrs):
        if tag == "a":
            if self.a_depth:
                self.errs.append("nested <a>")
            self.a_depth += 1
        if tag not in VOID:
            self.stack.append(tag)

    def handle_startendtag(self, tag, attrs):
        pass

    def handle_endtag(self, tag):
        if tag in VOID:
            return
        if tag == "a":
            self.a_depth -= 1
        if self.stack and self.stack[-1] == tag:
            self.stack.pop()
        elif tag in self.stack:
            while self.stack and self.stack[-1] != tag:
                self.errs.append(f"unclosed <{self.stack.pop()}>")
            self.stack.pop()
        else:
            self.errs.append(f"stray </{tag}>")


def lint(path):
    src = path.read_text(encoding="utf-8")
    errs = []
    if not src.rstrip().endswith("</html>"):
        errs.append("does not end with </html>")
    if '<script src="./support.js"></script>' not in src:
        errs.append("no support.js line")
    if "</x-dc>" not in src:
        errs.append("no </x-dc>")
    holes = set(re.findall(r"\{\{\s*([^}]*?)\s*\}\}", src)) - {"mode"}
    if holes:
        errs.append(f"holes {sorted(holes)}")
    if re.search(r"<sc-(for|if)\b", src):
        errs.append("sc-for/sc-if")
    for t in re.findall(r'href="([^"]+)"', src):
        if t.endswith(".dc.html") and not (PROJ / t).exists():
            errs.append(f"link to missing {t}")
        elif not t.endswith((".dc.html", ".css")) and not t.startswith(("http", "#", "/_blob")):
            errs.append(f"link without .dc.html: {t}")
    body = re.search(r"<x-dc>(.*)</x-dc>", src, re.S)
    body = body.group(1) if body else src
    body_nohelmet = re.sub(r"<helmet>.*?</helmet>", "", body, flags=re.S)
    for ic in set(re.findall(r"\bic-([a-z0-9-]+)", body_nohelmet)):
        if ic not in ICONS:
            errs.append(f"unknown icon ic-{ic}")
    lit = re.findall(r"(?:#[0-9a-fA-F]{3,8}\b|rgba?\()", re.sub(r'href="[^"]*"', "", body_nohelmet))
    if lit and "lock" not in body_nohelmet and "island" not in body_nohelmet:
        errs.append(f"{len(lit)} literal colours")
    if EMOJI.search(body_nohelmet):
        errs.append("emoji")
    if len(re.sub(r"<[^>]+>", "", body_nohelmet).split()) < 8:
        errs.append("almost no text")
    p = P()
    p.feed(body)
    errs += sorted(set(p.errs))[:4]
    if p.stack:
        errs.append(f"unclosed at end {p.stack[-3:]}")
    return errs


files = [pathlib.Path(f) for f in sys.argv[1:]] or sorted(PROJ.glob("*.dc.html"))
bad = 0
for f in files:
    e = lint(f)
    if e:
        bad += 1
        print(f"{f.name}: {'; '.join(e)}")
print(f"{len(files)} boards, {bad} with findings")
