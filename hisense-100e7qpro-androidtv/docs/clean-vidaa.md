# Ripulire VIDAA senza modificare il firmware

Cosa si può fare **oggi**, sul 100E7Q PRO così com'è, per togliere pubblicità e
disordine, e cosa invece non si può fare. Nessuno di questi passi tocca il firmware:
sono impostazioni, rete di casa e un'app web. Fonti in [`../SOURCES.md`](../SOURCES.md) (S-50…S-56).

## Cosa NON è possibile (e perché)

| Desiderio | Possibile? | Motivo |
|-----------|-----------|--------|
| Disinstallare le app preinstallate di sistema | **No** | Servirebbe l'accesso root al sistema, che sul 100E7Q PRO non c'è; il progetto non tocca il firmware (vedi [`recovery-plan.md`](recovery-plan.md)) |
| Sostituire la home di VIDAA con un'altra all'accensione | **No** | VIDAA si avvia sempre nella propria home; un'app non può prenderne il posto |
| Eliminare del tutto le pubblicità | **Non garantito** | Gli utenti riferiscono che l'apposito interruttore non basta sempre (S-50); il blocco DNS lascia un riquadro vuoto "VIDAA" (S-52) |

## 1. Impostazioni del televisore (rischio zero)

Le voci esatte cambiano tra versioni di VIDAA e **non sono verificate sull'U8.5 del
100E7Q PRO**: cerca nel menu Impostazioni (di solito sotto *Sistema* → *Privacy* o
*Informativa/Termini*) le voci con questi significati e disattivale:

- **Pubblicità personalizzata / basata sugli interessi.**
- **Riconoscimento dei contenuti (ACR)**, a volte chiamato "Servizi informativi di
  visione" o simile: è la funzione che analizza cosa guardi, anche dalle sorgenti
  HDMI, per profilarti. Il partner pubblicitario di VIDAA (Nexxen) vi ha accesso (S-50).
- **Contenuti consigliati / raccomandazioni** sulla home.
- **Avvio automatico di app o canali all'accensione**, se presente.

Poi, nella fila delle app della home, usa la funzione di modifica (tasto OK prolungato
o menu dell'app) per **spostare in fondo o rimuovere dai preferiti** le app che non
usi. Le app di sistema non si disinstallano, ma si possono togliere dalla vista.

> Nota: nel marzo 2026 sono stati segnalati spot non saltabili all'accensione e al
> cambio di ingresso HDMI. Hisense ha dichiarato che era un test temporaneo limitato
> alla Spagna (S-53), ma ci sono segnalazioni anche da altri paesi (S-51).

## 2. Blocco delle pubblicità sulla rete di casa (rischio zero, reversibile)

Il televisore scarica pubblicità e banner da server internet. Se gli dai un DNS che
blocca quei domini, la maggior parte non arriva.

**Opzione A, la più semplice: AdGuard DNS pubblico** (S-54).
Impostazioni → Rete → configurazione IP manuale → DNS:
`94.140.14.14` e `94.140.15.15`.
Per tornare indietro rimetti il DNS automatico.

**Opzione B: Pi-hole o AdGuard Home** su un Raspberry Pi o un NAS, per tutta la casa.
Le liste standard di AdGuard bloccano già gli annunci della home VIDAA, lasciando un
riquadro con scritto solo "VIDAA" (S-52). Non esiste ancora una lista pubblica
specifica per VIDAA (S-55): il registro delle richieste di Pi-hole mostra quali domini
contatta il televisore, così puoi bloccarli uno per uno.

Limiti onesti:
- Le pubblicità già scaricate restano in memoria finché VIDAA non le rinnova (S-54).
- Se un dominio serve sia gli annunci sia una funzione utile (aggiornamenti, app
  store, metadati), bloccarlo può rompere quella funzione: aggiungi un dominio alla
  volta e verifica.
- Il blocco DNS non ferma le connessioni dirette a un indirizzo IP.

## 3. Un'interfaccia pulita in stile Apple TV

Vedi [`../vidaa-launcher/`](../vidaa-launcher/): un launcher a schermo intero,
navigabile con le frecce del telecomando, installabile come app web su VIDAA. Non
sostituisce la home all'accensione (vedi sopra), ma può stare **prima nella fila
delle app** e diventare il punto da cui apri tutto il resto.
