import zipfile
import re

with zipfile.ZipFile("d:/Apps/launcher/kpn_base.apk") as z:
    names = z.namelist()
    print("Total files in APK:", len(names))
    
    # Check assets
    assets = [n for n in names if n.startswith("assets/")]
    print("\n--- ASSETS (sample 30) ---")
    for a in assets[:30]:
        print(a)
        
    # Check drawables
    drawables = [n for n in names if "drawable" in n]
    print("\n--- DRAWABLES (sample 30) ---")
    for d in drawables[:30]:
        print(d)

    # Check for fonts
    fonts = [n for n in names if "font" in n]
    print("\n--- FONTS ---")
    for f in fonts:
        print(f)
