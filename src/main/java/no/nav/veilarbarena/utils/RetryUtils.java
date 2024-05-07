package no.nav.veilarbarena.utils;

import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.logging.Logger;

public class RetryUtils {

    private static final Logger log = Logger.getLogger(RetryUtils.class.getName());
    private final static int msToWaitBetweenAttempts = 700;

    @SneakyThrows
    public static Response requestWithRetry(OkHttpClient client, Request request) {
        try {
            var response = client.newCall(request).execute();
            if(!response.isSuccessful()) {
                Thread.sleep(msToWaitBetweenAttempts);
                log.info("Kall feilet, forsøker på nytt");
                return client.newCall(request).execute();
            } else {
                log.info("Kall var vellykket første gang");
                return response;
            }
        } catch (IOException e) {
            Thread.sleep(msToWaitBetweenAttempts);
            log.info("Kall feilet pga IOException, prøver likevel på nytt");
            return client.newCall(request).execute();
        }
    }
}
