package no.nav.veilarbarena.utils;

import lombok.SneakyThrows;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

import static no.nav.veilarbarena.utils.RetryUtils.requestWithRetry;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RetryUtilsTest {

    OkHttpClient client = Mockito.mock(OkHttpClient.class);
    Request request = Mockito.mock(Request.class);
    Call call = Mockito.mock(Call.class);
    Response responseSuccessful = Mockito.mock(Response.class);
    Response responseFailure = Mockito.mock(Response.class);

    @BeforeAll
    public void beforeAll() {
        Mockito.when(responseSuccessful.isSuccessful()).thenReturn(true);
        Mockito.when(responseFailure.isSuccessful()).thenReturn(false);
        Mockito.when(client.newCall(request)).thenReturn(call);
    }

    @BeforeEach
    public void beforeEach() {
        Mockito.clearInvocations(call);
    }

    @SneakyThrows
    @Test
    public void skal_forsøke_en_gang_til_hvis_første_kall_feiler() {
        Mockito.when(call.execute()).thenReturn(responseFailure, responseSuccessful);
        var response = requestWithRetry(client, request);
        Mockito.verify(call, Mockito.times(2)).execute();
        assertTrue(response.isSuccessful());
    }

    @SneakyThrows
    @Test
    public void skal_ikke_forsøke_tre_ganger() {
        Mockito.when(call.execute()).thenReturn(responseFailure, responseFailure, responseSuccessful);
        var response = requestWithRetry(client, request);
        Mockito.verify(call, Mockito.times(2)).execute();
        assertFalse(response.isSuccessful());
    }

    @SneakyThrows
    @Test
    public void skal_ikke_forsøke_to_ganger_hvis_første_kall_er_vellykket() {
        Mockito.when(call.execute()).thenReturn(responseSuccessful, responseFailure);
        var response = requestWithRetry(client, request);
        Mockito.verify(call, Mockito.times(1)).execute();
        assertTrue(response.isSuccessful());
    }

}