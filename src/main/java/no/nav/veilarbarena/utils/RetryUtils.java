package no.nav.veilarbarena.utils;

import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.logging.Logger;

public class RetryUtils {

    private static final Logger log = Logger.getLogger(RetryUtils.class.getName());

    @SneakyThrows
    public static Response requestWithRetry(OkHttpClient client, Request request) {
        var response = client.newCall(request).execute();
        if(!response.isSuccessful()) {
            log.info("Kall feilet, forsøker på nytt");
            Thread.sleep(700);
            return client.newCall(request).execute();
        } else {
            log.info("Kall var vellykket første gang");
            return response;
        }

    }
}
