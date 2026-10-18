# MLHK TV+ Launcher — Configuratiehandleiding

Deze handleiding beschrijft hoe je de Plex Media Server koppeling en de Centrale Monitoring API instelt.

---

## 1. Plex Media Server Koppeling

De launcher haalt de rijen *Verder kijken*, *Recent toegevoegd*, *Aanbevolen*, *Films* en *Series* rechtstreeks op via de Plex Media Server API.

### Benodigde gegevens:
1. **Plex Server URL**:  
   Het lokale IP-adres en poort van je Plex Server (bijv. `http://192.168.2.25:32400` of je reverse proxy domein).
2. **Plex Token (X-Plex-Token)**:
   - Log in op [Plex Web](https://app.plex.tv).
   - Klik op een willekeurig mediabestand op de **drie puntjes (...)** -> **Informatie bekijken** (Get Info).
   - Klik onderaan op **XML weergeven** (View XML).
   - Kijk in de URL-balk van je browser: helemaal aan het einde vind je `X-Plex-Token=xxxxxxxxxxxx`.

### Instellen op de televisie:
1. Open MLHK TV+ op de televisie.
2. Selecteer met de afstandsbediening het **Tandwiel**-icoon rechtsboven.
3. Vul bij **Plex Server URL** en **Plex Token** je gegevens in.
4. Klik op **Opslaan**. De contentrijen worden direct geladen.

---

## 2. Centrale Monitoring API

De centrale monitoring service draait onafhankelijk op je eigen server (Proxmox LXC, 1Panel, Docker of Node/Python).

### Structuur `monitoring-api/config.json`:
```json
{
  "server": {
    "port": 8080,
    "api_key": "mlhk-secure-key",
    "check_interval_seconds": 30
  },
  "services": [
    {
      "id": "proxmox",
      "name": "Proxmox VE",
      "type": "http",
      "url": "https://192.168.2.10:8006/api2/json/version",
      "expected_status": [200, 401],
      "timeout_ms": 3000,
      "reject_unauthorized": false,
      "critical": true
    },
    {
      "id": "onepanel",
      "name": "1Panel",
      "type": "http",
      "url": "http://192.168.2.11:10000",
      "expected_status": [200, 302, 401],
      "timeout_ms": 3000,
      "critical": true
    },
    {
      "id": "plex",
      "name": "Plex Media Server",
      "type": "http",
      "url": "http://192.168.2.25:32400/identity",
      "expected_status": [200],
      "timeout_ms": 3000,
      "critical": true
    },
    {
      "id": "habnight",
      "name": "HabNight",
      "type": "http",
      "url": "https://habnight.nl",
      "expected_status": [200, 301, 302],
      "timeout_ms": 4000,
      "critical": false
    },
    {
      "id": "mlhk",
      "name": "MLHK.nl",
      "type": "http",
      "url": "https://mlhk.nl",
      "expected_status": [200, 301, 302],
      "timeout_ms": 4000,
      "critical": true
    },
    {
      "id": "workflow",
      "name": "Workflow MLHK",
      "type": "http",
      "url": "https://workflow.mlhk.nl",
      "expected_status": [200, 301, 302, 401],
      "timeout_ms": 4000,
      "critical": false
    }
  ]
}
```

### Ondersteunde Controles:
- **HTTP / HTTPS**:
  - `url`: Het volledige endpoint (ondersteunt TLS certificaat bypass via `reject_unauthorized: false` voor interne self-signed Proxmox certificaten).
  - `expected_status`: Lijst van toegestane statuscodes (bijv. `[200, 302, 401]`).
  - `timeout_ms`: Time-out in milliseconden.
  - `critical`: `true` (status wordt rood bij uitval) of `false` (status wordt oranje bij uitval).
- **TCP Port**:
  - `type`: `"tcp"`
  - `host`: IP of domeinnaam
  - `port`: Poortnummer

---

## 3. Uitvoeren van de Monitoring API

### Optie A: Direct via Node.js
```bash
cd monitoring-api
node server.js
```

### Optie B: Direct via Python
```bash
cd monitoring-api
python server.py
```

### Optie C: Via Docker / 1Panel
```bash
cd monitoring-api
docker compose up -d
```

### Testen:
Voer in PowerShell uit:
```powershell
.\scripts\test-api.ps1 -Url "http://localhost:8080/api/status"
```
