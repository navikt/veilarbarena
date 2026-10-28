FROM busybox:uclibc as busybox
FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-21

COPY --from=busybox /bin/sh /bin/sh
COPY --from=busybox /bin/printenv /bin/printenv

WORKDIR /app
COPY /target/veilarbarena.jar app.jar
ENV TZ="Europe/Oslo"
EXPOSE 8080
ENTRYPOINT ["/bin/sh", "-c", "java $JAVA_PROXY_OPTIONS -jar app.jar"]