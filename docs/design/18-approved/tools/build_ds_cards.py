"""Write the Strive 1 system's component cards: components/<Comp>/preview.html and README.md for each family the screens use."""
import pathlib
OUT = pathlib.Path(__file__).resolve().parent.parent / "system" / "project" / "components"

def doc(title, body, h=None):
    return (f"<!doctype html>\n<html lang=\"en\">\n<head><meta charset=\"utf-8\"><title>{title} — preview</title>\n"
            "<style>body{margin:0;background:var(--color-background)}</style></head>\n<body>\n"
            f"<div class=\"strive\" style=\"padding:16px;background:var(--color-background);color:var(--color-on-surface);display:grid;gap:16px\">\n{body}\n</div>\n"
            "<script></script>\n</body>\n</html>\n")

PHONE = 'style="position:relative;width:390px;height:{h}px;overflow:hidden;border-radius:16px;background:var(--color-background);border:1px solid var(--color-outline-variant)"'
def phone(h, inner):
    return f'<div {PHONE.format(h=h)}>{inner}</div>'

TAB = ('<nav class="tabbar glass"><span class="tab" aria-current="page"><span class="ic ic-home"></span>Home</span>'
       '<span class="tab"><span class="ic ic-food"></span>Food</span><span class="tab start"><span class="dot"><span class="ic ic-plus s28"></span></span></span>'
       '<span class="tab"><span class="ic ic-plan"></span>Plan</span><span class="tab"><span class="ic ic-you"></span>You</span></nav>')
RING = lambda off, size=64: (f'<div class="ring" style="width:{size}px;height:{size}px"><svg viewBox="0 0 64 64" aria-hidden="true"><circle class="track" cx="32" cy="32" r="23.5" stroke-width="6.4"></circle>'
                             f'<circle class="prog" cx="32" cy="32" r="23.5" stroke-width="6.4" stroke-dasharray="147.65" stroke-dashoffset="{off}"></circle></svg></div>')
SLEEVE = ('<svg class="sleeve" viewBox="0 0 64 40" aria-hidden="true"><rect class="shaft" x="0" y="18" width="64" height="4" rx="1"></rect><rect class="collar" x="4" y="12" width="4" height="16" rx="1"></rect>'
          '<rect class="plate" x="10" y="2" width="7" height="36" rx="1.5"></rect><rect class="plate" x="18" y="9" width="4" height="22" rx="1"></rect><rect class="plate" x="23" y="13" width="3" height="14" rx="1"></rect></svg>')

C = {}
C["TabBar"] = ("Navigation", 230, phone(200,
    '<div class="bottomstack"><a class="minibar glass"><span><b>Push A</b><small>24:13 · 2/15 sets</small></span><span class="mrest">' + RING(20.9, 22) + '1:43</span></a>' + TAB.replace('class="tabbar glass"', 'class="tabbar glass" style="position:static"') + '</div>'),
    "The floating glass tab bar with Start as a round action in the middle, and the minimised workout above it.",
    "Root screens only (Home, Food, Plan, You and what is pushed inside them); workouts and runs run full screen without it. Provide five `.tab` items with an icon and a label, `aria-current=\"page\"` on the current one, and `.tab.start` > `.dot` with `ic-plus`. A minimised workout or run goes in a `.bottomstack` above it as `.minibar.glass`: name, time and sets, and while resting a 22 pt amber ring with the time (design 1's polish; built: the rest only in the subline).")
C["TitleBars"] = ("Navigation", 330,
    '<div style="width:390px;display:grid;gap:12px">'
    '<header class="nav"><div class="l"><span class="back"><span class="ic ic-back s22"></span>Plan</span></div><h1>Library</h1><div class="r"><span class="ibtn plain"><span class="ic ic-more"></span></span><span class="ibtn plain teal"><span class="ic ic-plus"></span></span></div></header>'
    '<header class="lt" style="padding:0 16px"><h1>Plan</h1><span class="ibtn plain"><span class="ic ic-more"></span></span></header>'
    '<header class="wtb"><div class="l"><span class="ibtn plain"><span class="ic ic-down s22"></span></span><span class="ibtn"><span class="ic ic-timer s20"></span></span></div><div class="mid"></div><span class="finish">Finish</span></header>'
    '<header class="wtb glassed hair"><div class="l"><span class="ibtn plain"><span class="ic ic-down s22"></span></span><span class="ibtn"><span class="ic ic-timer s20"></span></span></div><div class="mid"><span class="wn">Push A <span class="pill">⋯</span></span><small>24:13 · 2/15 sets</small></div><span class="finish all">Finish</span></header>'
    '</div>',
    "Bars: the navigation bar of a pushed screen, the large title of a root screen, and F1, the workout's one bar.",
    "`.nav` holds a `.back` (teal chevron and the parent's name) on the left, an `h1`, and `.navtext` or `.ibtn.plain` actions on the right; it turns `.glassed` when content runs under it. `.lt` is the large title inside the scrolling body. In a workout `.wtb` holds ⌄ and ⏱ on the left and Finish on the right; scrolled, the name with a small ⋯ and \"24:13 · 2/15 sets\" fade into `.mid` and the bar turns to glass. `.finish.all` fills teal once every set is logged.")
C["FocusCard"] = ("Workout", 340,
    '<section class="focus" style="width:358px"><p class="now">Now · Bench Press BB · Set 2 of 3</p><div><div class="fvals"><span class="fv">82.5</span><span class="fu">kg</span><span class="fx">×</span><span class="fv">8</span><span class="fu">reps</span></div>'
    '<p class="fchange">up 2.5 kg</p><div class="fmeta"><span>rest 2:00 after</span><span>Previous <b>80 × 8 @3</b></span></div></div>'
    '<div class="seg compact"><span>0</span><span>1</span><span class="on">2</span><span>3</span><span>4+</span></div>'
    '<div class="facts"><span class="fbtn">Edit</span><span class="fbtn log"><span class="ic ic-check s20"></span>Log set 2</span></div></section>',
    "C's focus card as Carl drew it in D: the next set, large, one tap from logged.",
    "It always shows the next set to do, wherever the list is. Provide the NOW-label (exercise, set n of m, the kind word in its colour), the values with grey units, the change against last time (\"up 2.5 kg\"), the rest after it and Previous, the RIR segments (0 · 1 · 2 · 3 · 4+, no label), Edit (opens the dock) and Log set n (logs as shown). One filled button: Log. In a superset the label carries the letter: NOW · A · INCLINE PRESS DB · SET 1 OF 3.")
C["SetRows"] = ("Workout", 470,
    '<div style="width:358px">'
    '<div class="exh"><div class="top"><span class="nm">Bench Press BB</span><span class="pill">⋯</span><span class="fold"><span class="ic ic-up s14"></span></span></div>'
    '<p class="exnote"><span class="ic ic-pin s14"></span>Arch, feet planted.</p><p class="exnote"><b>Plan</b>Skulderbladen ihop.</p></div>'
    '<div class="cols"><span class="c-set">Set</span><span class="c-prev">Previous</span><span class="c-w">kg</span><span class="c-r">Reps</span><span class="c-ok">✓</span></div>'
    '<div class="srow done"><span class="badge w">W</span><span class="sprev">40 × 10</span><span class="cell w">40</span><span class="cell r">10</span><span class="ok"><span class="ic ic-check s18"></span></span></div>'
    '<div class="restl">Rest 2:00</div>'
    '<div class="srow done"><span class="badge">1</span><span class="sprev">77.5 × 8 @3</span><span class="cell w">80</span><span class="cell r">8<small>@2</small></span><span class="ok"><span class="ic ic-check s18"></span></span></div>'
    '<div class="restl cur">Rest 2:00</div>'
    '<div class="srow next"><span class="badge">2</span><span class="sprev">80 × 8 @3</span><span class="cell w">82.5</span><span class="cell r">8</span><span class="ok"><span class="ic ic-check s18"></span></span></div>'
    '<div class="pline">' + SLEEVE + '<span>25 + 5 + 1.25 per side</span></div>'
    '<div class="restl">Rest 2:00</div>'
    '<div class="srow"><span class="badge f">2</span><span class="sprev">+10 × 8 @0</span><span class="cell w">+10</span><span class="cell r">9</span><span class="ok"><span class="ic ic-check s18"></span></span></div>'
    '<div class="srow"><span class="badge d">D</span><span class="sprev">20 × 12</span><span class="cell w">20</span><span class="cell r">12</span><span class="ok"><span class="ic ic-check s18"></span></span></div>'
    '<span class="addset"><span class="plus">+</span>Add set (2:00)</span></div>',
    "A's set rows as D draws them: badge · Previous · values · ✓, with rest lines between and the plates under the next barbell set.",
    "Every set has its own ✓ that logs it as it stands. Provide `.srow` with `.badge` (number; `.w` W amber, `.f` failure number coral, `.d` D violet; a tap changes the kind), `.sprev` (last time, \"80 × 8 @3\"), value `.cell`s (64 and 56 pt; one 88 pt cell for time or distance; a tap opens the dock on that field), and `.ok`. `.srow.done` drops the cell fills and fills the ✓; `.srow.next` is tinted (`state-next-row`) with an outlined ✓. Rest lines `.restl` sit between sets, `.restl.cur` after the last logged one. A superset wraps its rows in `.ssgroup` under `.sslabel` (\"Superset · rest 1:30 after each round\") with `.tagl` letters, in lifting order. Swipe a row left to delete it.")
C["Overview"] = ("Workout", 300,
    '<section class="ov" style="width:358px">'
    '<div class="ovrow"><div class="grow"><div class="nm">Bench Press BB</div><div class="dots"><i class="w"></i><i class="on"></i><i class="on"></i><i class="on"></i></div></div><span class="cnt all">4/4 ✓</span><span class="exp"><span class="ic ic-down s14"></span></span></div>'
    '<div class="ovrow"><div class="grow"><div class="nm"><span class="tagl">A</span>Incline Press DB</div><div class="dots"><i class="on"></i><i></i><i></i></div></div><span class="cnt">1/3</span><span class="exp"><span class="ic ic-down s14"></span></span></div>'
    '<div class="ovrow"><div class="grow"><div class="nm"><span class="tagl">B</span>Lateral Raise DB</div><div class="dots"><i class="on"></i><i></i><i></i></div></div><span class="cnt">1/3</span><span class="exp"><span class="ic ic-down s14"></span></span></div>'
    '<div class="ovrow"><div class="grow"><div class="nm skip">Tricep Dips · Skipped</div></div><span class="exp"><span class="ic ic-down s14"></span></span></div>'
    '<div class="ovrow"><div class="grow"><div class="nm">Tricep Pushdown</div><div class="dots"><i></i><i></i><i></i></div></div><span class="cnt">0/3</span><span class="exp"><span class="ic ic-down s14"></span></span></div></section>',
    "Folded exercises share one overview card: name, a dot per set in its kind's colour once logged, n/m, expand.",
    "Everything not in focus folds here, in workout order. Provide one `.ovrow` per exercise: `.nm` (with `.tagl` in a superset; `.nm.skip` for a skipped one), `.dots` (`i.on` working, `i.w` warm-up, `i.f` failure, `i.d` drop; an empty ring until logged), `.cnt` (`.cnt.all` \"4/4 ✓\" in teal when done) and `.exp`. A tap expands the exercise.")
C["EmptyWorkout"] = ("Workout", 400,
    '<div style="width:358px;display:grid;gap:24px"><section class="qs"><span class="mark"><span class="ic ic-plus s32"></span></span><h2>What are you training today?</h2>'
    '<p>Pick from the library, or tap an exercise below.</p><span class="btn filled">Add exercises</span></section>'
    '<div class="vstack"><div class="between"><span class="title-small">Quick picks</span><span class="cap">Marked Yes in your programmes</span></div>'
    '<div class="chipwrap"><span class="chip qp"><span class="plus">+</span>Bench Press BB</span><span class="chip qp"><span class="plus">+</span>Squat BB</span><span class="chip qp"><span class="plus">+</span>Deadlift BB</span><span class="chip qp"><span class="plus">+</span>Pull Up</span></div></div></div>',
    "C-tomt: an empty workout asks what you train and adds an exercise in one tap.",
    "Provide the card (`.qs`: the 72 pt round + on primary-container, the question, a line, Add exercises) and quick picks as `.chip.qp` with a teal +: exercises marked Yes in In my programmes, else the most recent, at most six. No marks and no history: no quick picks.")
C["Dock"] = ("Logging", 420, phone(400,
    '<div class="dock glass thick"><span class="grab"></span><p class="dk-title">Bench Press BB · Set 2</p>'
    '<div class="dk-fields"><div class="lbl">Weight<div class="nf"><span class="stepk">−</span><span class="v">82.5</span><span class="stepk">+</span></div></div><div class="lbl">Reps<div class="nf"><span class="stepk">−</span><span class="v">8</span><span class="stepk">+</span></div></div></div>'
    '<div class="barline">' + SLEEVE.replace('class="sleeve"', 'class="sleeve" style="height:44px;width:100%"') + '<div class="txt"><b>25 + 5 + 1.25</b><span>per side</span></div><span class="barmenu">Bar 20 <span class="ic ic-down s14"></span></span></div>'
    '<div class="dk-2"><div class="lbl">RIR<div class="seg"><span>0</span><span>1</span><span class="on">2</span><span>3</span><span>4+</span></div></div><div class="lbl">Rest<div class="nf"><span class="stepk">−</span><span class="v">2:00</span><span class="stepk">+</span></div></div></div>'
    '<span class="btn filled">Log</span></div>'),
    "The logging dock (S3, D3): change a set before or after logging it, under the thumb.",
    "A tap on a set opens it; the grabber's swipe down or a tap outside closes it (no ✕). Provide the set's name and number, its value fields as `.nf` with − and + at the exercise's step (weight and reps; time, distance or added weight by type), for a barbell the `.barline` (the drawn plates per side, the text, the bar's menu), the row RIR segments + rest field, and one filled button: Log, \"Save change\" on a logged set, \"Save\" when opened from a rest line. A tapped field opens the keypad.")
C["Keypad"] = ("Logging", 470, phone(450,
    '<div class="dock glass thick" style="gap:10px"><span class="grab"></span><p class="dk-title">Bench Press BB · Set 2</p>'
    '<div class="dk-fields"><div class="lbl">Weight<div class="nf focus"><span class="v">85</span></div></div><div class="lbl">Reps<div class="nf"><span class="v">8</span></div></div><div class="lbl">Rest<div class="nf"><span class="v">2:00</span></div></div></div>'
    '<div class="kp"><div class="top"><span class="kk small">Hide</span><span class="kk small">Plates</span><span class="grow"></span><span class="kk pm">−</span><span class="kk pm">+</span></div>'
    '<div class="digits"><span class="kk">1</span><span class="kk">2</span><span class="kk">3</span><span class="kk">4</span><span class="kk">5</span><span class="kk">6</span><span class="kk">7</span><span class="kk">8</span><span class="kk">9</span><span class="kk">.</span><span class="kk">0</span><span class="kk">⌫</span></div></div>'
    '<span class="btn filled">Next</span></div>'),
    "K3: Strive's own number keypad inside the dock, with Next walking weight → reps → rest.",
    "Small actions on top (Hide; Plates on the weight field; − and + at 44 pt), digits on round glass keys in three columns with the locale's decimal and ⌫, and the full-width Next in Log's place. On the rest field Next reads \"Log ›\" and logs. A field opened fresh is replaced by the first key. While the keypad is open the rest card hides. Plates swaps the digits for plate keys (`.digits.plates`).")
C["RestCard"] = ("Logging", 210,
    '<div class="restcard glass thick" style="position:static;width:358px">'
    '<div class="top">' + RING(20.9) + '<div><div class="k">Rest</div><div class="time">1:43</div></div></div>'
    '<div class="acts"><span class="roundk">−</span><span class="cap">15 s</span><span class="roundk">+</span><span class="skipk skip">Skip</span></div></div>',
    "T5: the rest countdown in glass, floating over the list wherever it is scrolled.",
    "Starts when a set is logged (the set's rest, else Settings' default). Provide the 64 pt amber ring (dashoffset = (1 − left/total) × 147.65), the label (Rest, or Free rest) and the time in display size, round − and + with \"15 s\" between them, and Skip apart on the right. No pause, no reset. It ends with a sound and a buzz; the last 3 s turn teal.")
C["Buttons"] = ("Actions", 280,
    '<div style="width:358px;display:grid;gap:10px"><span class="btn filled">Start workout</span><span class="btn">Edit</span><span class="btn danger">Delete template</span>'
    '<div class="btnrow"><span class="btn small">Show all</span><span class="btn small" style="background:var(--color-primary);color:var(--color-on-primary)">Deload now</span><span class="btn text">Cancel</span></div>'
    '<div class="hstack"><span class="ibtn"><span class="ic ic-timer s20"></span></span><span class="ibtn plain"><span class="ic ic-more"></span></span><span class="pill">⋯</span><span class="ibtn on"><span class="ic ic-sliders s20"></span></span><span class="btn dis" style="width:auto">Save</span></div></div>',
    "Buttons (#16 B3): rounded and wide, capsule when small. One filled button per view.",
    "`.btn.filled` is the one primary action of a view (Start workout, Log, Save). `.btn` (teal text on surface-container-high) is everything else; `.btn.danger` names what goes; `.btn.small` is a capsule; `.btn.text` a bare text button; `.btn.dis` disabled. `.ibtn` is a 44 pt icon disc, `.ibtn.plain` without the disc, `.ibtn.on` a filter in use; `.pill` is the deep teal ⋯. Press: scale .96 and dim, 100 ms; release on the spring.")
C["Controls"] = ("Actions", 330,
    '<div style="width:358px;display:grid;gap:12px"><div class="segc"><span class="on">About</span><span>History</span><span>Chart</span><span>Records</span></div>'
    '<div class="seg"><span>Normal</span><span class="on">Calibration</span><span>Deload</span></div>'
    '<div class="chips" style="margin:0;padding:0"><span class="chip on">Vertical pull</span><span class="chip">Horizontal press</span><span class="chip">Knee dominant</span></div>'
    '<div class="field"><span class="ic ic-search s20"></span><span class="in ph">Search 553 exercises</span></div>'
    '<div class="field focus"><span class="in">Push A<span class="caret"></span></span></div>'
    '<div class="between"><span>Keep the screen awake</span><span class="switch on"></span></div></div>',
    "Controls: segmented for views and for values, chips, fields, the switch.",
    "`.segc` switches a view (the chosen segment on surface, as iOS). `.seg` picks a value and fills the chosen one teal, as built (RIR, week type). Chips filter (`.chip.on` teal). Fields are grey (`.field`, `.field.focus` with a teal ring). The switch's knob stays white in every mode.")
C["Lists"] = ("Content", 330,
    '<div style="width:358px;display:grid;gap:16px"><div class="group"><h4>Account</h4><div class="list">'
    '<div class="row lead"><span class="lic"><span class="ic ic-you s18"></span></span><span>Profile</span><span class="chev"><span class="ic ic-chev-r s16"></span></span></div>'
    '<div class="row lead"><span class="lic"><span class="ic ic-gear s18"></span></span><span>Settings</span><span class="chev"><span class="ic ic-chev-r s16"></span></span></div>'
    '<div class="row lead"><span class="lic"><span class="ic ic-share s18"></span></span><span>Export</span><span class="val">18 workouts<span class="ic ic-chev-r s16"></span></span></div></div></div>'
    '<div class="list tiles"><div class="row"><span>Default rest</span><span class="val">2:00</span></div><div class="row"><span>Units</span><span class="val">kg</span></div><div class="row act center">Add gear</div></div></div>',
    "Lists (rule A): a long list is one block with hairlines; a short list you start or edit is joined tiles.",
    "Provide `.list` > `.row` (title left; `.val` value, `.chev`, or a `.check` right); `.row.lead` with a 30 pt `.lic` icon tile; `small` for a second line. `.list.tiles` joins short lists as tiles. Group lists under `.group` with an `h4` and a `.foot`. A pressed row turns surface-container-highest; no ripple, no scale.")
C["CardsAndStats"] = ("Content", 330,
    '<div style="width:358px;display:grid;gap:12px"><section class="card"><p class="eyebrow">Planned today · Week 6 of 8</p><h2 class="title" style="font-weight:600">Push A</h2><p class="meta">5 exercises · 15 sets · about 30 min</p><span class="btn filled">Start Push A</span></section>'
    '<div class="stats four"><div class="stat"><span class="k">Time</span><span class="v">64<small>min</small></span></div><div class="stat"><span class="k">Avg RIR</span><span class="v">1.5</span><span class="sub">goal 2–3</span></div><div class="stat"><span class="k">Sets</span><span class="v">15</span></div><div class="stat"><span class="k">Records</span><span class="v">3</span></div></div></div>',
    "Cards hold one thing with its own button; stat tiles hold one number each (#16 D1).",
    "A `.card` has an `.eyebrow`, a `.title`, `.meta` lines and at most one filled `.btn`. Stat tiles `.stat` (`.k` key, `.v` value with a grey `small` unit, `.sub`) sit in `.stats` (two), `.stats.three` or `.stats.four`. Solid, no shadow. Numbers are never teal.")
C["Charts"] = ("Content", 330,
    '<section class="chart" style="width:358px"><div class="head"><span>Volume per week</span><b>22.9 t</b></div>'
    '<svg viewBox="0 0 326 150" aria-hidden="true"><line class="grid" x1="0" y1="10" x2="326" y2="10"></line><line class="grid" x1="0" y1="70" x2="326" y2="70"></line><line class="grid" x1="0" y1="130" x2="326" y2="130"></line>'
    '<rect class="bar2 prev" x="6" y="55" width="14" height="75" rx="3"></rect><rect class="bar2" x="22" y="46" width="14" height="84" rx="3"></rect>'
    '<rect class="bar2 prev" x="46" y="70" width="14" height="60" rx="3"></rect><rect class="bar2 dl" x="62" y="85" width="14" height="45" rx="3"></rect>'
    '<rect class="bar2 prev" x="86" y="50" width="14" height="80" rx="3"></rect><rect class="bar2" x="102" y="61" width="14" height="69" rx="3"></rect>'
    '<rect class="bar2 prev" x="126" y="52" width="14" height="78" rx="3"></rect><rect class="bar2" x="142" y="58" width="14" height="72" rx="3"></rect>'
    '<rect class="bar2 prev" x="166" y="48" width="14" height="82" rx="3"></rect><rect class="bar2" x="182" y="47" width="14" height="83" rx="3"></rect>'
    '<rect class="bar2 prev" x="206" y="56" width="14" height="74" rx="3"></rect><rect class="bar2" x="222" y="53" width="14" height="77" rx="3"></rect>'
    '<rect class="bar2 prev" x="246" y="60" width="14" height="70" rx="3"></rect><rect class="bar2 on" x="262" y="32" width="14" height="98" rx="3"></rect>'
    '<rect class="bar2 prev" x="286" y="58" width="14" height="72" rx="3"></rect><rect class="bar2 now" x="302" y="112" width="14" height="18" rx="3"></rect>'
    '<line class="avg" x1="0" y1="54" x2="326" y2="54"></line><text class="tick mid" x="29" y="146">39</text><text class="tick mid" x="109" y="146">41</text><text class="tick mid" x="189" y="146">43</text><text class="tick mid" x="269" y="146">45</text></svg>'
    '<div class="legend"><span><i class="k1"></i>These 8 weeks</span><span><i class="k2"></i>The 8 before</span><span><i class="k3"></i>Average 17.7 t</span></div></section>',
    "Charts (#16 D1): one series in neutral ink, the period before in a lighter mark, the tapped mark teal.",
    "Draw marks to scale in SVG with `.grid`, `.tick`, `.bar2` (this period), `.bar2.prev`, `.bar2.dl` (deload weeks), `.bar2.now` (the week in progress, dashed), `.avg` (dashed, named in the legend), `.line` and `.pt` for lines. A tap turns a mark `.on` and writes its value in the head. Empty: `.nodata` with a dashed edge and the sentence that says what will appear.")
C["Menus"] = ("Overlays", 420, phone(400,
    '<div class="menu glass" style="top:16px;right:16px"><div class="tiles"><span class="tile"><span class="ic ic-note s20"></span>Note</span><span class="tile"><span class="ic ic-swap s20"></span>Swap</span></div>'
    '<span class="mrow">Exercise details<span class="ic ic-info s20"></span></span><span class="mrow">Sticky note<span class="ic ic-pin s20"></span></span><span class="mrow">Rest for all sets<span class="tail">›</span></span>'
    '<span class="mrow">Create superset</span><span class="mrow">Order and skip</span><span class="mrow">Exercise settings</span><span class="mrow danger">Remove exercise<span class="ic ic-trash s20"></span></span></div>'),
    "iOS pull-down menus on glass, anchored to the button that opened them (choice 4).",
    "Provide `.menu` positioned under its button, rows `.mrow` (label left, a glyph or `.tail` right), `.mrow.danger` last, `.mtitle` for a title, `.msep` between sections, and for the exercise menu M2's two palette tiles (`.tiles` > `.tile`: Note, Swap) on top. A submenu replaces the rows with a back row. Design 1 polish: the built app uses Material's DropdownMenu on surface-container-high; this is the decided glass pull-down.")
C["Dialogs"] = ("Overlays", 470, '<div style="display:flex;gap:16px;flex-wrap:wrap">' + phone(420,
    '<div class="scrim"></div><div class="center"><div class="alert"><h2>Discard the workout?</h2><p>Every set in it is deleted.</p><div class="acts"><span class="btn">Cancel</span><span class="btn danger">Discard</span></div></div></div>') + phone(420,
    '<div class="scrim"></div><div class="asheet"><div class="list"><div class="row head">2 sets not logged</div><div class="row">Log them as they are and save</div><div class="row">Save without them</div><div class="row destructive">Discard workout</div></div><div class="list"><div class="row cancel">Continue workout</div></div></div>') + '</div>',
    "Alerts for an uncommon action that cannot be undone; action sheets for choices after an intentional action (#16).",
    "An `.alert` sits in `.center` over a `.scrim`: a title, one line saying what happens, two buttons with the destructive one named (\"Discard\"). An `.asheet` lists the choices as centred rows with a grey `.row.head` that counts what is at stake, `.row.destructive` in red, and `.row.cancel` in its own block. No alert after a destructive choice in an action sheet. Prompts are alerts with a `.field`.")
C["StartBubbles"] = ("Overlays", 340, phone(320,
    '<div class="bpanel" style="left:26px;right:26px;bottom:72px;height:170px"></div>'
    '<span class="bub main" style="left:80px;top:160px"><span class="ic ic-lift"></span><span>Push A<small>today</small></span></span>'
    '<span class="bub" style="left:150px;top:108px"><span class="ic ic-plus"></span><span>Empty</span></span>'
    '<span class="bub" style="left:240px;top:108px"><span class="ic ic-list"></span><span>From template</span></span>'
    '<span class="bub" style="left:310px;top:160px"><span class="ic ic-run"></span><span>Run</span></span>' + TAB.replace('<span class="dot">', '<span class="dot" style="transform:rotate(45deg)">')),
    "Start (choice 14c, N4): four round bubbles spring up in an arc over Start; the screen stays in view.",
    "Today's workout first and filled (\"Push A · today\"; on a run day the run, on a rest day the next session outlined), then Empty workout, From template (third tap: the templates as bubbles three across), Run. With a workout in progress there is one bubble: \"Continue Push A · 24:13\". First use: Create template in today's place. A tap outside or × closes. Bubbles stagger 40 ms on the release spring.")
C["HintsAndToasts"] = ("Overlays", 230,
    '<div style="position:relative;width:358px;height:200px"><div class="hint down" style="left:20px;top:0;--arrow:85%"><b>Log in one tap</b><p>Tap ✓ to log the set as it stands.</p><span class="got">Got it</span></div>'
    '<div class="toast" style="top:150px"><span class="ic ic-star s16" style="color:var(--color-tertiary)"></span>New record · Bench Press BB · 82.5 kg</div></div>',
    "First-use hints teach a function where it is used; toasts confirm what just happened.",
    "A `.hint` (inverse surface, an arrow toward its control with `.up`/`.down` and `--arrow`) shows once, at the first use: the ✓, the dock, swipe to delete, holding Start. A `.toast` drops from under the bar for 2.4 s: a record, a restored workout, a moved session.")
C["WeekCard"] = ("Plan", 360,
    '<section class="card wk" style="width:358px"><div class="pname"><span>Hypertrofi, block 1</span><span class="ic ic-chev-r s16"></span></div>'
    '<div class="wkhead"><span class="ibtn plain teal"><span class="ic ic-chev-l s20"></span></span><span><b>Week 6 of 8</b><small>Week 46 · 9–15 Nov</small></span><span class="ibtn plain teal"><span class="ic ic-chev-r s20"></span></span></div>'
    '<div class="wbars"><i class="past">K</i><i class="past">K</i><i class="past">3</i><i class="past">4</i><i class="past">5</i><i class="now">6</i><i>7</i><i>8</i></div>'
    '<div class="wkdays"><span class="wkday today sel">M<b>9</b><span class="pl">Push A</span></span><span class="wkday">T<b>10</b><span class="pl">Ben A</span></span><span class="wkday">W<b>11</b><span class="pl">Rest</span></span>'
    '<span class="wkday">T<b>12</b><span class="pl">Pull A</span></span><span class="wkday">F<b>13</b><span class="pl">Run 8k</span></span><span class="wkday drop">S<b>14</b><span class="pl">Push B</span></span><span class="wkday">S<b>15</b><span class="pl">Long 14k</span></span></div>'
    '<div class="dlrec"><span><small>6 weeks without a deload</small></span></div></section>',
    "W2: the programme frames the week — where Carl is in the block and which week he looks at are one thing.",
    "Provide the programme's name (opens the programme), the week title with ‹ › (a swipe on the days does the same; Today appears once away), the calendar week and dates, the block's weeks as `.wbars` (K calibration, D deload, the current one inked), the seven days Monday to Sunday (`.wkday` with the plan, ✓ done, `.miss` struck through, `.x` Extra, `.today` ring, `.sel` teal, `.drop` greyed under Short week), and the deload line (`.dlrec`, Deload now when recommended).")
C["TemplateCards"] = ("Plan", 260,
    '<div class="tcar" style="width:390px;margin:0;padding:0 16px"><div class="tcard"><div class="th">Push A<span class="ibtn plain"><span class="ic ic-more"></span></span></div><span class="mus">Chest, shoulders, triceps</span><span class="tstat">5 exercises · about 30 min</span><small><span class="ic ic-clock s12"></span>Mon 2 Nov</small></div>'
    '<div class="tcard"><div class="th">Pull A<span class="ibtn plain"><span class="ic ic-more"></span></span></div><span class="mus">Back, biceps</span><span class="tstat">4 exercises · about 35 min</span><small><span class="ic ic-clock s12"></span>Thu 5 Nov</small></div></div>',
    "P1: templates as large cards you swipe; a card zooms open to its full-screen preview.",
    "Each `.tcard` shows the name with ⋯ (Edit, Duplicate, Share, Archive, Delete), the muscles, exercises and \"about 30 min\", and last done. New template ends the row (`.tcard.new`). A separate heading holds templates outside the programme.")
C["LibraryRows"] = ("Library", 300,
    '<div style="width:390px"><div class="lhead">Chest</div><div class="list" style="border-radius:0">'
    '<div class="lrow"><span class="thumb"><span class="ic ic-muscle s28"></span></span><span><b>Bench Press BB</b><small>Horizontal press · Barbell</small></span><span class="end">82.5 × 8<span class="mark yes">Yes</span></span></div>'
    '<div class="lrow"><span class="thumb"><span class="ic ic-muscle s28"></span></span><span><b>Incline Press DB</b><small>Horizontal press · Dumbbells</small></span><span class="end">26 × 10<span class="mark yes">Yes</span></span></div>'
    '<div class="lrow pick"><span class="thumb"><span class="ic ic-muscle s28"></span></span><span><b>Fly Cable</b><small>Horizontal press · Cable</small></span><span class="end"></span><span class="chk on"><span class="ic ic-check s16"></span></span></div></div></div>',
    "L1: one list with a sticky heading per primary muscle; a row is the illustration's crop, name, pattern · equipment, last performance, the mark.",
    "Provide `.lhead` per muscle group, `.lrow` with `.thumb` (Carl's illustration cropped; until it arrives, the drawn muscle glyph — design 1's O-19), the name, \"pattern · equipment\", last performance (\"82.5 × 8\") and the In my programmes `.mark` (`.yes` teal, `.swap` blue, `.test` amber; none when unmarked, No hidden). In the picker `.lrow.pick` adds a `.chk`.")
C["RunScreen"] = ("Running", 560, phone(540,
    '<div class="map" style="height:190px;background:var(--color-surface-container-high)"><svg viewBox="0 0 390 190" style="position:absolute;inset:0" aria-hidden="true"><path d="M60 140 L140 70 L200 50 L280 60 L320 110 L300 150 L120 155 Z" fill="none" stroke="var(--map-route)" stroke-width="5" stroke-linejoin="round"></path></svg>'
    '<span class="gps glass" style="position:absolute;left:12px;top:12px"><i></i>GPS</span></div>'
    '<div style="padding:16px;display:grid;gap:14px"><div class="bignum"><span class="v">3.42</span><span class="u">Kilometres</span></div>'
    '<div class="runstats"><div><b>19:41</b><small>Time</small></div><div><b>5:38</b><small>Pace /km</small></div><div><b>5:45</b><small>Avg /km</small></div></div>'
    '<div style="display:flex;justify-content:center;gap:40px"><span class="runbtn">Lap</span><span class="runbtn main">Pause</span></div></div>'),
    "The live run: one number at arm's length, the route on a map that swipes away, Pause and Lap.",
    "Runs follow Strava's layout with Strive's own look. The big number (96 pt) is distance by default; a tap cycles time, current pace and average pace (O-05). Three secondary stats in `.runstats`, 84 pt round `.runbtn` controls, the map in `.map` with a mode-matched base image and the route on top, glass `.mapctl` controls. Recording continues locked (Live Activity).")
C["SystemSurfaces"] = ("System", 340,
    '<div class="lock" style="position:relative;width:390px;height:320px;border-radius:16px;overflow:hidden;padding:20px 12px;display:grid;gap:12px;align-content:start">'
    '<span class="island wide" style="top:8px"><span class="ring" style="width:20px;height:20px"><svg viewBox="0 0 64 64" aria-hidden="true"><circle class="track" cx="32" cy="32" r="23.5" stroke-width="10"></circle><circle class="prog" cx="32" cy="32" r="23.5" stroke-width="10" stroke-dasharray="147.65" stroke-dashoffset="20.9"></circle></svg></span><span class="t">1:43</span></span>'
    '<p class="lockdate" style="margin-top:40px">Monday 9 November</p><p class="lockclock" style="font-size:64px;line-height:68px">17:26</p>'
    '<div class="la glass thick"><div class="between"><b>Push A · 24:13</b><span class="cap">Strive</span></div><div class="hstack">' + RING(20.9, 40) + '<span style="font:var(--type-display-small)">1:43</span><span class="cap" style="margin-left:auto">Next: Bench Press BB · 82.5 × 8</span></div></div></div>',
    "The system's own surfaces drawn for the spec: the lock screen, its Live Activity and the Dynamic Island.",
    "The rest countdown leaves the app when the phone locks: a Live Activity (workout name and elapsed time, the ring and countdown, the next set) and the Dynamic Island (compact: ring + time; minimal: ring; expanded: name, countdown, next set). A notification says rest is over. The wallpaper is fixed dark; the activity uses the mode's glass.")
icons = "home food plus minus plan calendar you search back chev-r chev-l down up updown clear x lift note pin flame timer clock swap link reorder skip sliders trash pencil check more info share download gear run flag play pause layers locate cube expand heart bolt lock bell shoe bike watch globe warn sync cloud chart trend grip mountain external list ruler plate gps gauge route arrow-up arrow-down barcode muscle hand eye archive copy merge star star-o play-f pause-f dot".split()
C["Icons"] = ("Foundations", 520,
    '<div style="display:grid;grid-template-columns:repeat(8,minmax(0,1fr));gap:12px 8px;width:720px">' +
    "".join(f'<div style="display:grid;justify-items:center;gap:4px"><span class="ic ic-{n}"></span><span class="cap" style="font-size:10px">{n}</span></div>' for n in icons) + "</div>",
    "Strive's stroke glyphs on a 24 grid, 2 pt stroke, round ends; drawn as masks so they take the text colour.",
    "Use `<span class=\"ic ic-NAME\"></span>`, sized `s12`–`s44`. Colour comes from the text: teal on a tappable control, grey for secondary, amber for records. Icon-only buttons carry an `aria-label`. No emoji, no logos; devices and apps are named in text.")

for name, (group, height, body, summary, guide) in C.items():
    d = OUT / name
    d.mkdir(parents=True, exist_ok=True)
    (d / "preview.html").write_text(f'<!-- @dsCard group="{group}" height={height} -->\n' + doc(name, body))
    (d / "README.md").write_text(f"# {name}\n\n{summary}\n\n{guide}\n")
print(len(C), "cards")
