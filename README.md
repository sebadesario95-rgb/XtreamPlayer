# Xtream Player

Starter project Android/Kotlin per un client autorizzato Xtream API.

## Requisiti
- Android Studio recente
- JDK 17
- Android SDK 35

## Funzioni incluse
- Login con Server URL / Username / Password
- Autenticazione `player_api.php`
- Recupero Live TV, VOD e Series
- Costruzione adattabile degli URL di streaming
- Media3/ExoPlayer
- Credenziali protette tramite Android Keystore/EncryptedSharedPreferences
- Architettura separata API / repository-ready / UI / player

## XtreamStreamUrlBuilder
Il builder:
- usa `server_protocol`, `url`, `port` e `https_port` quando forniti dal server;
- usa il server configurato come fallback;
- accetta l'estensione restituita dai dati VOD;
- evita di fissare l'estensione nell'interfaccia;
- mantiene separata la costruzione degli URL dalla UI.

## Nota
I server che dichiarano compatibilità Xtream possono avere variazioni. Prima di una release reale conviene aggiungere adapter/strategie per i diversi formati di risposta, caching/paginazione, dettagli VOD/Series, EPG e gestione completa degli stati del player.

Per generare l'APK:
Build -> Build APK(s)
