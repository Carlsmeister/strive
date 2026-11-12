# TabBar

The floating glass tab bar with Start as a round action in the middle, and the minimised workout above it.

Root screens only (Home, Food, Plan, You and what is pushed inside them); workouts and runs run full screen without it. Provide five `.tab` items with an icon and a label, `aria-current="page"` on the current one, and `.tab.start` > `.dot` with `ic-plus`. A minimised workout or run goes in a `.bottomstack` above it as `.minibar.glass`: name, time and sets, and while resting a 22 pt amber ring with the time (design 1's polish; built: the rest only in the subline).
