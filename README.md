VeilarbArena
================

Microservice som fungerer som anti-curruption-layer foran arena for FO.

## Målbilde
Målet er at all integrasjon mot arena på sikt skal gå gjennom denne applikasjonen, slik at evt. endringer fra arena's
side i fremtiden bare påvirker en applikasjon.

Tjenester man kan se for seg:
- Terminering av arena's `personid`
- Løpende-ytelser-fil (NFS i veilarbportefolje per idag)
- Tiltak-fil (SFTP i veilarbportefolje per idag)
- Arena-JSON proxy (Mye fra av veilarboppfolging)
- Bolk uthenting med søk/filtrering


### Finurligheter
- Bruker veilarbportefolje sin database pga db-link til arena (Denne skal permanent flyttes hit etterhvert:tm:)
- Databasen har materaliserte views som synkes mot views i arena via db-link. Dette betyr at det kan forekomme forsinkelser på noen minutter når data fra databasen brukes.


# Komme i gang

```
# bygge
mvn clean install 

# test
mvn test

# starte
# Kjør main-metoden i no.nav.veilarbarena.VeilarbarenaApp.java
# For lokal test kjøring kjør MainTest.java
```

---

# Henvendelser

Spørsmål knyttet til koden eller prosjektet kan stilles via issues her på github.

## For NAV-ansatte

Interne henvendelser kan sendes via Slack i kanalen #poao-tech.

## Kode generert av GitHub Copilot

Dette repoet bruker GitHub Copilot til å generere kode.
