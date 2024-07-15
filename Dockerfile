FROM ghcr.io/navikt/baseimages/temurin:21

COPY init.sh /init-scripts/init.sh
COPY /target/veilarbarena.jar app.jar