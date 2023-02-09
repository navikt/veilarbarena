FROM ghcr.io/navikt/pus-nais-java-app/pus-nais-java-app:17

COPY init.sh /init-scripts/init.sh
COPY /target/veilarbarena.jar app.jar