# MLHK TV+ Launcher — KPN TV+ Custom Replacement

Volledig functionele, native Android TV-launcher voor de **KPN TV+ Box (Sagemcom DIW7022)**. 

Biedt de vertrouwde KPN TV+ gebruikerservaring met:
1. **MLHK Branding**: Logo en stylesheet gebaseerd op het MLHK Radio 2026 dark glassmorphism design (`#080712`, `#120E20`, `#FF2F7D`, `#00E5FF`).
2. **Plex Integratie**: Alle aanbevelingenrijen (*Verder kijken*, *Recent toegevoegd*, *Aanbevolen*, *Films*, *Series*) komen rechtstreeks uit Plex Media Server en openen via deep-links direct in de officiële Plex Android TV app.
3. **Subtiele Statusindicator**: Uiterst discrete indicator rechtsboven (`● Alles online`) gekoppeld aan een centrale monitoring-API voor Proxmox, 1Panel, Plex, HabNight, MLHK.nl, Workflow MLHK en overige diensten. Geen overbodige dashboards of CPU/RAM-statistieken op de TV.
4. **100% KPN Compatibiliteit**: Live TV, TV Gids, Opgeslagen en remote shortcuts blijven volledig intact. Geen DRM-aanpassingen, geen root, geen risico's.

---

## Direct Aan de Slag

### 1. Gebouwde APK
De kant-en-klare APK staat in:
```
dist/mlhk-tvplus-launcher.apk (55.8 MB)
```

### 2. Automatisch Deployen naar KPN TV+ Box via ADB
Zorg dat ADB netwerkfoutopsporing aanstaat op de DIW7022 (zie `docs/INSTALLATIE-EN-HERSTEL.md`):
```powershell
.\scripts\deploy-adb.ps1 -DeviceIp 192.168.2.58
```

### 3. Centrale Monitoring API Starten
In de map `monitoring-api`:
```bash
# Optie A: Node.js (zonder extra dependencies)
node server.js

# Optie B: Python 3
python server.py

# Optie C: Docker / 1Panel
docker compose up -d
```
Test de API via:
```powershell
.\scripts\test-api.ps1
```

### 4. Veilige Herstelprocedure
Terugkeren naar de standaard KPN TV+ launcher kan op ieder moment via het instellingenmenu op de TV of via:
```powershell
.\scripts\restore-kpn.ps1 -DeviceIp 192.168.2.58
```

---

## Documentatie
- [Systeemarchitectuur](docs/ARCHITECTUUR.md)
- [Installatie & Herstel](docs/INSTALLATIE-EN-HERSTEL.md)
- [Configuratie (Plex & Monitoring)](docs/CONFIGURATIE.md)
- [Afstandsbediening Gedrag](docs/AFSTANDSBEDIENING.md)
