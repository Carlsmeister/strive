"""Screenshot a local HTML file with headless Chrome. Usage: python3 shot.py PAGE.html OUT.png WIDTH HEIGHT [--scale N]
--scale N renders at Chrome's device scale N, so the PNG is N x WIDTH by N x HEIGHT px."""
import os, subprocess, sys
args = sys.argv[1:]
scale = []
if "--scale" in args:
    i = args.index("--scale"); scale = [f"--force-device-scale-factor={args[i + 1]}"]; del args[i:i + 2]
page, out, w, h = args
subprocess.run(["/Applications/Google Chrome.app/Contents/MacOS/Google Chrome", "--headless=new", "--disable-gpu", "--hide-scrollbars",
                "--allow-file-access-from-files", *scale, f"--window-size={w},{h}", f"--screenshot={os.path.abspath(out)}", "file://" + os.path.abspath(page)],
               check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
print(out)
