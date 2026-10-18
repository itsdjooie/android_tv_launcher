# Installatie- en Herstelprocedure — KPN TV+ Box (Sagemcom DIW7022)

Deze handleiding beschrijft de exacte stappen om de **MLHK TV+ Launcher** te installeren op je KPN TV+ Box en hoe je op elk moment veilig terugkeert naar de standaard KPN-omgeving.

---

## 1. Voorbereiding op de KPN TV+ Box (ADB Inschakelen)

Om apps te installeren via het netwerk moet eenmalig **Ontwikkelaarsopties** en **Netwerkfoutopsporing (ADB)** worden geactiveerd op de DIW7022:

1. Druk op de **Home**- of **Tandwiel**-knop van je afstandsbediening en ga naar **Instellingen** (rechtsboven).
2. Kies **Apparaatvoorkeuren** (of *Systeem*) -> **Info**.
3. Scroll naar beneden naar **Build van Android TV OS** (of *Build*).
4. Druk **7 keer achter elkaar op de OK-knop** van je afstandsbediening totdat de melding verschijnt:  
   *"U bent nu een ontwikkelaar!"*
5. Ga één stap terug naar **Apparaatvoorkeuren** -> scroll naar het nieuwe menu **Ontwikkelaarsopties**.
6. Schakel de optie **USB-foutopsporing** (USB debugging) en/of **Draadloze foutopsporing** in.
7. Noteer het IP-adres van je TV+ Box (te vinden onder *Instellingen -> Netwerk en internet -> Verbonden netwerk*). Standaard bevindt de DIW7022 zich in het subnet `192.168.2.x`.

---

## 2. Automatische Installatie via ADB Script

Vanaf je computer in hetzelfde thuisnetwerk:

1. Open PowerShell in de projectmap `d:\Apps\launcher`.
2. Voer het deployscript uit met het IP-adres van je TV+ Box:
   ```powershell
   .\scripts\deploy-adb.ps1 -DeviceIp 192.168.2.58
   ```
3. Kijk naar je televisiescherm: er verschijnt een pop-up:  
   *"Foutopsporing via USB / Netwerk toestaan?"*  
   Vink aan: *"Altijd toestaan vanaf deze computer"* en druk op **OK**.
4. Het script installeert automatisch `dist\mlhk-tvplus-launcher.apk` en start de app.

---

## 3. Handmatige Installatie (Alternatief zonder ADB)

Indien ADB op jouw firmwareversie tijdelijk afgeschermd is:

1. Kopieer `dist\mlhk-tvplus-launcher.apk` naar een USB-stick (FAT32 geformatteerd).
2. Steek de USB-stick in de USB-poort aan de zijkant/achterkant van de Sagemcom DIW7022.
3. Open een bestandsbeheerder op de TV (bijv. *AnExplorer* of *FX File Explorer*, gratis te downloaden uit de Google Play Store op de TV).
4. Open het APK-bestand en kies **Installeren**.

---

## 4. De Launcher als Standaard Instellen

> [!IMPORTANT]
> Test eerst of de navigatie, de Plex-koppeling en de terugkeer naar KPN TV goed werken voordat je de launcher als definitieve standaard instelt!

Als je tevreden bent:
1. Druk op de **Home**-knop op je afstandsbediening.
2. Android TV toont de vraag: *"Welke app wilt u gebruiken als startscherm?"*
3. Selecteer **MLHK TV+** en kies **Altijd**.

---

## 5. Veilig Herstel naar Originele KPN TV+ Omgeving

Je kunt op elk gewenst moment zonder gegevensverlies terugkeren:

### Methode A: Via het instellingenmenu op de TV
1. Klik rechtsboven in de MLHK Launcher op het **Tandwiel**-icoon (Instellingen).
2. Druk op de knop **"Terug naar KPN"** (start direct `com.kpn.kpnandroidtv`).
3. Of druk op **"Standaard Launcher Wijzigen"** en herstel de standaard startscherm-app.

### Methode B: Automatisch via ADB herstel-script
Voer in PowerShell uit:
```powershell
.\scripts\restore-kpn.ps1 -DeviceIp 192.168.2.58
```
Dit start direct de officiële KPN TV+ interface en herstelt de standaard home-activiteit.

### Methode C: Via Android Systeeminstellingen
1. Ga naar **Instellingen** -> **Apps** -> **Speciale app-toegang** -> **Standaard startscherm-app**.
2. Selecteer de originele KPN TV+ launcher.
