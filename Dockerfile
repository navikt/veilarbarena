FROM busybox:uclibc as busybox
FROM gcr.io/distroless/java21-debian12:nonroot

COPY --from=busybox /bin/sh /bin/sh
COPY --from=busybox /bin/printenv /bin/printenv

WORKDIR /app
COPY /target/veilarbarena.jar app.jar
ENV TZ="Europe/Oslo"
EXPOSE 8080
ENTRYPOINT ["/bin/sh", "-c", "java $JAVA_PROXY_OPTIONS -jar app.jar"]