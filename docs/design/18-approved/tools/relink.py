"""The review's flow fixes (2026-11-10) as link rules, safe to run again: python3 relink.py
Each rule: (boards as glob patterns, regex, replacement). A board that no longer matches is left alone, so a second run is a
no-op. Prints each board it changes and each rule that matched nothing anywhere."""
import fnmatch, pathlib, re
P = pathlib.Path(__file__).resolve().parent.parent / "canvas" / "project"
REST = ["WO-02-s3", "WO-06-round", "WO-06-free", "WO-06-last", "WO-03-rest"]
OTHER_TEMPLATES = "Push B|Armar|Pull B|Ben B|Rörlighet|Core|Push C"
RULES = [
    # F-22: minimise while resting shows the resting bar; Plan from Home keeps the bar.
    (REST, r'(<a class="ibtn plain" href=")SH-02-inprog(\.dc\.html" aria-label="Minimise workout">)', r"\1WO-17-rest\2"),
    (["SH-02-inprog", "WO-17-rest"], r'(<a class="(?:tab|teal)" href=")PL-01-week(\.dc\.html")', r"\1WO-17-plan\2"),
    (["WO-17-plan"], r'(<a class="tab" href=")Main(\.dc\.html")', r"\1SH-02-inprog\2"),
    # F-28: the empty workout logs and finishes as itself; WO-23's Save shows the new template.
    (["WO-01-picked"], r'(<a class="(?:fbtn log|ok)" href=")WO-02-s3(\.dc\.html")', r"\1WO-01-logged\2"),
    (["WO-01-picked"], r'(<a class="finish" href=")WO-15-several(\.dc\.html")', r"\1WO-16-empty\2"),
    (["WO-23-summary"], r'(<a class="btn filled" href=")PL-01-week(\.dc\.html")', r"\1PL-01-saved\2"),
    # F-38: the template picker adds to the new template; its Superset has no board.
    (["EX-03-tmpl"], r'(<a class="btn filled" href=")PL-05-edit(\.dc\.html")', r"\1PL-05-added\2"),
    (["EX-03-tmpl"], r'<a class="btn" href="PL-05-edit\.dc\.html">(.*?)</a>', r'<span class="btn">\1</span>'),
    # F-46: New in the picker opens the picker's create sheet.
    (["EX-03-none", "EX-03-one", "EX-03-two", "EX-03-search", "EX-03-created"], r'(<a class="navtext" href=")EX-09-empty(\.dc\.html")', r"\1EX-09-picker\2"),
    # F-47: Merge ends on done.
    (["EX-11-alert"], r'(<a class="btn danger" href=")EX-05(\.dc\.html")', r"\1EX-11-done\2"),
    # F-19, F-15, F-63.
    (["EX-04"], r'(<a class="lrow" href=")WO-02-s2(\.dc\.html">(?:(?!</a>).)*?Chest Press Machine)', r"\1WO-02-swap\2"),
    (["WO-06-round"], r'(<a class="skipk skip" href=")WO-02-s5(\.dc\.html")', r"\1WO-02-s5c\2"),
    (["SH-07-start"], r'(<a class="btn filled" href=")WO-02-s1(\.dc\.html">)', r"\1SH-07-check\2"),
    # F-04, F-36: each template opens its own workout or preview; the ones without boards are not links.
    (["SH-04-four", "SH-04-many"], r'(<a class="bub" href=")WO-02-s1(\.dc\.html" aria-label="Ben A,)', r"\1WO-02-benA\2"),
    (["SH-04-four", "SH-04-many"], r'(<a class="bub" href=")WO-02-s1(\.dc\.html" aria-label="Pull A,)', r"\1WO-02-pulla\2"),
    (["SH-04-four", "SH-04-many"], r'<a class="bub" href="WO-02-s1\.dc\.html" aria-label="((?:%s),[^"]*)">(.*?)</a>' % OTHER_TEMPLATES,
     r'<span class="bub" aria-label="\1">\2</span>'),
    (["PL-01-*", "PL-02-*", "PL-03-*"], r'(<div class="tcard" style="position: relative">)(<div class="th"><span>Pull A</span>)',
     r'\1<a href="PL-04-pulla.dc.html" aria-label="Open Pull A" style="position: absolute; inset: 0; border-radius: inherit"></a>\2'),
    # Copenhagen Plank is timed: the weight keypad is not its keypad, and no time keypad board exists.
    (["WO-02-s15"], r'<a ([^>]*?)href="WO-05-weight\.dc\.html"([^>]*)>(.*?)</a>', r'<span \1\2>\3</span>'),
    # F-64: the rest day offers Pull A, so it opens Pull A.
    (["SH-02-rest", "SH-03-rest"], r'(href=")WO-02-s1(\.dc\.html")', r"\1WO-02-pulla\2"),
    # F-33, F-37, O-12.
    (["PL-01-short"], r'(<a class="ibtn" href=")PL-02-plan(\.dc\.html" aria-label="Week options">)', r"\1PL-02-short\2"),
    (["PL-03-menu"], r'(<a class="mrow lead" href=")PL-07-list(\.dc\.html"><span class="ic ic-archive s18"></span>Archive)', r"\1PL-03-archived\2"),
    (["PL-08-daymenu"], r'<span class="mrow" role="menuitem">Run…(<span class="ic ic-chev-r s16 tail"></span>)</span>',
     r'<a class="mrow" href="PL-08-runmenu.dc.html" role="menuitem">Run…\1</a>'),
    # F-44: the filtered library's Incline row opens its own detail.
    (["EX-01-filter"], r'<span class="lrow">(<span class="thumb"><span class="ic ic-muscle s28"></span></span><span><b>Incline Press DB</b>(?:(?!</span></span></span>).)*?</span></span>)</span>',
     r'<a class="lrow" href="EX-05-incline.dc.html">\1</a>'),
    # F-53: the comparison tap changes only the comparison; 6 mo has its own selected bar.
    (["EL-02-8wk"], r'(<a class="funline" href=")EL-02-1yr(\.dc\.html")', r"\1EL-02-next\2"),
    (["EL-02-6mo"], r'(<section class="chart" aria-label="Volume">\n<div class="head">.*?</div>\n)(<svg .*?</svg>)',
     r'\1<a href="EL-02-6mobar.dc.html" aria-label="Select a week" style="display: block">\2</a>'),
    # F-30: a past set opens the dock over the past session.
    (["WO-25-edit", "WO-25-unsaved", "WO-25-delete", "WO-23-edit"], r'(<a class="cell [wr]" href=")WO-05-weight(\.dc\.html")', r"\1WO-25-dock\2"),
    # F-62: Import and Sync are two later rows; Food (v1) previews the later Food tab.
    (["EL-01"], r'<a style="color: var\(--color-on-surface-variant\)" class="row lead" href="EL-15-source\.dc\.html"><span class="lic"><span class="ic ic-cloud s18"></span></span><span>Import and sync<small>From Strong or Strava, and a backup</small></span>(<span class="val">Later<span class="ic ic-chev-r s16 chev"></span></span>)</a>',
     r'<a style="color: var(--color-on-surface-variant)" class="row lead" href="EL-15-source.dc.html"><span class="lic"><span class="ic ic-download s18"></span></span><span>Import<small>From Strong or Strava</small></span>\1</a>'
     r'<a style="color: var(--color-on-surface-variant)" class="row lead" href="EL-16-out.dc.html"><span class="lic"><span class="ic ic-cloud s18"></span></span><span>Account and sync<small>A backup of everything</small></span>\1</a>'),
    (["SH-05"], r'(<p>Calories and protein against a daily target, logged from what you eat\. It lands here, and nothing else moves\.</p>\n)(</section>)',
     r'\1<a class="btn tinted" href="EL-17-notarget.dc.html">See the food log (later)</a>\n\2'),
]


def boards(globs):
    return [f for f in sorted(P.glob("*.dc.html")) if any(fnmatch.fnmatch(f.name.removesuffix(".dc.html"), g) for g in globs)]


unused = []
for globs, pat, rep in RULES:
    hit = False
    for f in boards(globs):
        s = f.read_text()
        new, k = re.subn(pat, rep, s, flags=re.S)
        if k:
            hit = True
            f.write_text(new)
            print(f"{f.name}: {k}× {rep[:50]!r}")
    if not hit:
        unused.append(pat[:80])
print("rules that matched nothing (already applied, or the markup differs):", *unused, sep="\n  ")
