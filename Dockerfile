FROM ghcr.io/navikt/poao-baseimages/java:17

COPY init.sh /init-scripts/init.sh
COPY /target/veilarbarena.jar app.jar