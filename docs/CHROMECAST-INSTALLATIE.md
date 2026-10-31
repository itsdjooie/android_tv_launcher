# Installatie- en Herstelprocedure — Chromecast with Google TV (Woonkamer)

Deze handleiding beschrijft de exacte stappen om de **MLHK TV+ (Woonkamer) Launcher** te installeren op je **Chromecast with Google TV (HD / 4K)** en hoe je op elk moment veilig terugkeert naar de standaard Google TV interface.

---

## 1. Voorbereiding op de Chromecast (ADB Inschakelen)

1. Druk op de **Home**- of **Tandwiel**-knop van je Chromecast afstandsbediening en ga naar **Instellingen** (rechtsboven).
2. Kies **Systeem** -> **Over**.
3. Scroll helemaal naar beneden naar **Build van Android TV OS**.
4. Druk **7 keer achter elkaar op de ronde OK-knop** van je afstandsbediening totdat de melding verschijnt:  
   *"U bent nu een ontwikkelaar!"*
5. Ga één stap terug naar **Systeem** -> scroll naar het nieuwe menu **Ontwikkelaarsopties**.
6. Schakel de optie **USB-foutopsporing** (USB debugging) en/of **Draadloze foutopsporing** in.
7. Noteer het IP-adres van je Chromecast (*Instellingen -> Netwerk en internet -> Verbonden wifi-netwerk*).

---

## 2. Automatische Installatie via PowerShell

Vanaf je computer in hetzelfde thuisnetwerk (bijv. PC):

1. Open PowerShell in de projectmap `d:\Apps\launcher`.
2. Voer het deployscript uit met het IP-adres van je Chromecast in de woonkamer:
   ```powershell
   .\scripts\deploy-chromecast.ps1 -DeviceIp 192.168.2.XX
   ```
3. Kijk naar je televisiescherm in de woonkamer: er verschijnt een pop-up:  
   *"Foutopsporing via USB / Netwerk toestaan?"*  
   Vink aan: *"Altijd toestaan vanaf deze computer"* en druk op **OK**.
4. Het script installeert automatisch `dist\mlhk-tvplus-chromecast-woonkamer.apk` en start de app direct op!

---

## 3. Standaard Launcher Maken op Chromecast with Google TV

Google TV verbergt standaard de optie "Standaard startscherm-app". Er zijn twee eenvoudige manieren om MLHK TV+ als je vaste startscherm in te stellen:

### Optie A: Direct via het script (Aanbevolen)
Voer uit met de `-SetAsDefault` parameter:
```powershell
.\scripts\deploy-chromecast.ps1 -DeviceIp 192.168.2.XX -SetAsDefault
```
Dit schakelt de zware Google TV launcher tijdelijk uit (`pm disable-user --user 0 com.google.android.apps.tv.launcherx`). Android gebruikt hierdoor direct **MLHK TV+ Woonkamer** wanneer je op de Home-knop drukt.

### Optie B: Via Button Mapper / Launcher Manager
Indien je Google TV niet wilt uitschakelen, kun je via de gratis app *Button Mapper* (Google Play Store) de Home-knop koppelen aan het openen van `MLHK TV+ Woonkamer`.

---

## 4. 🛡️ Noodknop & Veilig Herstel naar Google TV

Je kunt op elk gewenst moment zonder gegevensverlies terugkeren naar de fabrieksinterface:

### Methode 0: Direct via Afstandsbediening (Zonder PC)
> [!TIP]
> **Houd de Home-knop (Huisje) 2 seconden ingedrukt.**  
> MLHK TV+ Launcher vangt `onKeyLongPress(KEYCODE_HOME)` af en lanceert direct de officiële **Google TV launcher** (`com.google.android.apps.tv.launcherx`). Je zit dus nooit vast!

### Methode 1: Automatisch herstelscript via PC
Voer in PowerShell uit:
```powershell
.\scripts\restore-chromecast.ps1 -DeviceIp 192.168.2.XX
```
Dit activeert de officiële Google TV launcher weer en start hem direct op.
