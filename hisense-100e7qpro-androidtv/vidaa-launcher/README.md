# vidaa-launcher — interfaccia in stile Apple TV per VIDAA

Launcher a schermo intero per il 100E7Q PRO: sfondo che cambia colore con l'app
selezionata, riquadri arrotondati che si ingrandiscono al passaggio, titolo grande,
orologio. Si usa con le frecce, OK e Indietro del telecomando originale.

È un'app web (HTML/CSS/JS in un solo file), cioè lo stesso formato delle app VIDAA
(S-12). **Non modifica il televisore** e si rimuove come qualsiasi altra app.

## Cosa fa e cosa no

- ✅ Apre le **app web** che configuri (RaiPlay, Plex, Jellyfin, …) con un tasto.
- ✅ Ricorda l'ultima app selezionata.
- ✅ Ha una pagina **Info sistema** (solo lettura) che elenca le funzioni proprietarie
  che il browser del tuo televisore espone: serve a capire, sul tuo firmware, se si
  potranno aggiungere riquadri per le app native (Netflix, Prime…) o per gli ingressi
  HDMI.
- ❌ **Non apre ancora le app native VIDAA** né cambia ingresso HDMI. La guida per
  sviluppatori VIDAA cita delle "API di sistema" proprietarie (S-12), ma il loro elenco
  non è pubblico e non sappiamo se comprendono l'avvio di app o il cambio di ingresso.
  La pagina Info sistema serve proprio a scoprire cosa espone il tuo firmware.
- ❌ Non sostituisce la home di VIDAA all'accensione.

## Personalizzare

Modifica [`apps.json`](apps.json). Ogni riquadro:

```json
{ "name": "RaiPlay", "subtitle": "Dirette e on demand Rai",
  "url": "https://www.raiplay.it/", "color": "#0b4ea2", "glyph": "R" }
```

`color` è il colore del riquadro e dello sfondo, `glyph` la lettera o il simbolo al
centro. Le voci predefinite sono esempi: YouTube in particolare potrebbe rifiutare il
browser di VIDAA, e l'indirizzo di Jellyfin va sostituito con quello del tuo server.

## Installarlo sul televisore

1. Su un computer sempre acceso nella stessa rete del televisore (PC, NAS, Raspberry
   Pi) avvia `../tools/serve_launcher.sh`: mostra l'indirizzo, ad es.
   `http://192.168.1.20:8080/`.
2. Sul televisore apri il **Browser**, scrivi `hisense://debug` nella barra degli
   indirizzi. Si apre una finestra di installazione (S-56).
3. Compila **App Name** (es. "Salotto") e **App Url** con l'indirizzo del punto 1;
   facoltativo **Thumbnail** con l'URL di un'icona. Premi **Install**.
4. L'app compare in fondo a **Le mie app**: spostala in prima posizione.

> Questo metodo di installazione è documentato da terzi (S-56) e la sua disponibilità
> dipende dalla versione del firmware, come per lo strumento simile `vidaa-appstore`
> (S-43, S-44). **Non è ancora verificato sull'U8.5 del 100E7Q PRO.**

## Verifiche fatte

`../tools/run_launcher_tests.sh` lo apre in Chromium headless a 1920×1080 e 1280×720
e simula il telecomando: navigazione con le frecce e limiti di riga, cambio di riga,
OK che apre l'URL dell'app, apertura e chiusura di Info sistema con Indietro (codice
tasto 8), ripristino della selezione dopo il ricaricamento, nessun errore JavaScript.
**Tutti i 12 controlli passano.** Non è stato provato sul browser del televisore, che
è diverso da Chromium desktop: per questo il codice evita le funzioni JavaScript più
recenti e accetta più codici per il tasto Indietro.
