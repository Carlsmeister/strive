"""Coverage of Strive 1 against the inventory: python3 coverage.py [--table]
Prints each inventory screen whose state count differs from its boards, the boards no tap reaches from Main,
and with --table a Markdown coverage table (screen → its boards) for the notes."""
import collections, pathlib, re, sys
D = pathlib.Path(__file__).resolve().parent.parent
INV = (D / "inventory.md").read_text()
REG = (D / "boards.md").read_text()
PROJ = D / "canvas" / "project"

screens = {}  # id -> (name, [states])
for m in re.finditer(r"^\*\*([A-Z]{2}-\d{2}|O-22) · ([^*]+)\*\*(.*?)(?=^\*\*[A-Z]{1,2}-\d{2} · |^## |\Z)", INV, re.S | re.M):
    st = re.search(r"\*\*States:\*\*\s*(.+?)(?:\n- |\n\n|\Z)", m.group(3), re.S)
    states = [s.strip(" .") for s in re.split(r";\s*", st.group(1).replace("\n", " "))] if st else []
    screens[m.group(1)] = (m.group(2).strip(), states)

boards = collections.defaultdict(list)  # id -> [(file, state)]
for line in REG.splitlines():
    m = re.match(r"\| ([A-Za-z0-9-]+) \| ([A-Z]{2}-\d{2}|O-22|LT)[^·|]* · ([^|]+?) · dark-HC \|", line)
    if m and not m.group(1).startswith(("MO-", "MD-")):
        boards[m.group(2)].append((m.group(1), m.group(3).strip()))

for sid, (name, states) in screens.items():
    n = len(boards.get(sid, []))
    if n < len(states):
        print(f"FEWER {sid} {name}: {len(states)} states, {n} boards: {states}")

# Reachability from Main through every <a href> on every board.
links = {f.stem.removesuffix(".dc"): set(t.removesuffix(".dc.html") for t in re.findall(r'href="([^"]+\.dc\.html)"', f.read_text()))
         for f in PROJ.glob("*.dc.html")}
seen, todo = {"Main"}, ["Main"]
while todo:
    for t in links.get(todo.pop(), ()):
        if t not in seen:
            seen.add(t); todo.append(t)
lost = sorted(b for b in links if b not in seen and not b.startswith(("MD-", "MO-")))
print(f"unreachable from Main ({len(lost)}): {lost}")

if "--flows" in sys.argv:
    # Each flow's screen IDs in order; a step is wired when some board of one screen links to some board of the next.
    byid = collections.defaultdict(set)
    for sid, bs in boards.items():
        byid[sid] |= {f for f, _ in bs}
    byid["SH-02"] |= {"Main"}
    for m in re.finditer(r"^\| (F-\d+) \| ([^|]+) \| ([^|]+) \|", INV, re.M):
        ids = re.findall(r"\b([A-Z]{2}-\d{2})\b", m.group(3))
        miss = [f"{a}→{b}" for a, b in zip(ids, ids[1:]) if a != b and byid[a] and byid[b]
                and not any(links.get(x, set()) & byid[b] for x in byid[a])]
        print(f"{m.group(1)} {m.group(2).strip()}: {'wired' if not miss else 'NOT WIRED ' + ', '.join(miss)}")

if "--table" in sys.argv:
    print("\n| Screen | States in the inventory | Boards (each switches dark, dark-HC, light, light-HC; `MD-…` sheet shows three at once) |")
    print("|---|---|---|")
    for sid, (name, states) in screens.items():
        bs = boards.get(sid, [])
        md = next((f.stem.removesuffix('.dc') for f in PROJ.glob(f"MD-{sid}*.dc.html")), "")
        if sid == "SH-02" and not md:
            md = "MD-Main"
        print(f"| {sid} {name} | {'; '.join(states)} | {', '.join(f'`{b}` ({s})' for b, s in bs)}{' · `' + md + '`' if md else ''} |")
