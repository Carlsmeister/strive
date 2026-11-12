"""Screenshot a local HTML file with headless Chrome. Usage: python3 shot.py PAGE.html OUT.png WIDTH HEIGHT"""
import os, subprocess, sys
page, out, w, h = sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4]
subprocess.run(["/Applications/Google Chrome.app/Contents/MacOS/Google Chrome", "--headless=new", "--disable-gpu", "--hide-scrollbars",
                "--allow-file-access-from-files", f"--window-size={w},{h}", f"--screenshot={os.path.abspath(out)}", "file://" + os.path.abspath(page)],
               check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
print(out)
