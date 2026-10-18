# MLHK TV+ Launcher — Systeemarchitectuur

De **MLHK TV+ Launcher** is een native, lichtgewicht Android TV-launcher speciaal ontworpen en geoptimaliseerd voor de **KPN TV+ Box (Sagemcom DIW7022)**.

---

## 1. Doelstelling & Kernprincipes

1. **Visueel identiek aan KPN TV+**: De navigatiestructuur, marges, typografie, focusanimaties en carrousels bootsen nauwkeurig de vertrouwde KPN TV+ gebruikerservaring na.
2. **MLHK Dark Glassmorphism Branding**: Subtiele vervanging van het KPN-logo door het officiële MLHK-logo en styling gebaseerd op het MLHK-kleurenpalet (`#080712`, `#120E20`, `#FF2F7D`, `#00E5FF`). Geen overdreven rode banners of storende elementen.
3. **Plex Content Engine**: Alle media-aanbevelingsrijen worden live opgehaald uit de persoonlijke Plex Media Server (Verder kijken met voortgangsbalk, Recent toegevoegd, Aanbevolen, Films en Series). Klikken op een titel opent direct de officiële Android TV Plex-applicatie.
4. **Subtiele Dienstenstatus**: Een discrete statusindicator rechtsboven (`● Alles online`) toont de gezondheid van servers en containers zonder opdringerige grafieken of dashboards.
5. **Geen DRM-wijzigingen of Risico's**: De officiële KPN TV+ app (`com.kpn.kpnandroidtv`), gids, live TV en opnames blijven 100% behouden en direct bereikbaar.

---

## 2. Componentenoverzicht

```
               [ Sagemcom DIW7022 Afstandsbediening ]
                                 │
                                 ▼
                   ┌───────────────────────────┐
                   │   nl.mlhk.tvlauncher      │
                   │   MainActivity            │
                   └─────────────┬─────────────┘
                                 │
     ┌───────────────────────────┼───────────────────────────┐
     ▼                           ▼                           ▼
┌──────────────┐         ┌──────────────┐            ┌──────────────┐
│ PlexClient   │         │ StatusClient │            │ AppManager   │
└──────┬───────┘         └──────┬───────┘            └──────┬───────┘
       │                        │                           │
       ▼                        ▼                           ▼
[Plex Server API]     [MLHK Monitoring API]       [KPN TV & TV Apps]
(Port 32400)          (Port 8080)                 (com.kpn.kpnandroidtv)
```

---

## 3. UI-indeling (KPN TV+ Replica)

### Bovenste navigatiebalk
- **Links**: MLHK Logo (scherpe vector/PNG weergave op 28dp hoogte).
- **Tabs**:
  - `Nu op TV`: Opent direct de Live TV modus van de officiële KPN TV+ app.
  - `TV Gids`: Opent direct de EPG gids van KPN TV+.
  - `Start`: Actieve startpagina met hero banner en Plex-carrousels.
  - `Films & Series`: Snelle sprong naar films- en seriesrijen.
  - `Apps`: Snelle sprong naar de app-rij.
  - `Opgeslagen`: Opent KPN TV+ opnames.
- **Rechts**:
  - Statusindicator: `● Alles online` (Groen / Oranje / Rood / Grijs).
  - Instellingen-icoon (Tandwiel).
  - Live klok (`HH:mm`).

### Hero / Spotlight Banner
- Groot formaat achtergrond (backdrop art van Plex) met donkere MLHK-gradiënten.
- Titel in krachtige typografie.
- Metadata: Jaartal, Duur/Seizoen, IMDb/Plex-score (`★ 8.4`), genres.
- Korte omschrijving / synopsis.
- `▶ Afspelen` / `▶ Hervatten` knop (MLHK neon pink accent).

### Horizontale Carrousels (D-pad Focus)
1. **Verder kijken**: Liggende covers (16:9) met paarse/roze kijkvoortgangsbalk.
2. **Recent toegevoegd**: Staande film- en serieposters.
3. **Aanbevolen voor jou**: Gepersonaliseerde suggesties vanuit Plex hubs.
4. **Favoriete Apps**: Horizontale rij met geïnstalleerde Android TV apps (Plex, KPN TV+, YouTube, Netflix, Disney+, Spotify, etc.).
5. **Films**: Bibliotheekoverzicht.
6. **Series**: Bibliotheekoverzicht.

---

## 4. Focus & Performance

- **Geen WebView**: 100% native Kotlin met Jetpack Compose.
- **Image Caching**: Asynchrone afbeeldingstransformaties en geheugencaching via **Coil 2.7**.
- **Geen Streaming / Transcoding**: De launcher decodeert geen video zelf en gebruikt geen achtergrondservices. Het afspelen wordt via een intent overgedragen aan de officiële Plex app.
- **Minimale Batterij- & CPU-belasting**: Statuspolling vindt uitsluitend plaats met een 60-seconden interval wanneer het startscherm actief is, en pauzeert wanneer een app of TV wordt gekeken.
