package no.nav.fo.veilarbarena.service;

import org.junit.Test;

import javax.ws.rs.WebApplicationException;

import static no.nav.fo.veilarbarena.service.OppfolgingsbrukerController.*;
import static org.assertj.core.api.Java6Assertions.assertThat;

public class OppfolgingsbrukerControllerTest {

    @Test
    public void skal_validere_page_number() {
        try {
            validatePageNumber("sadljkasdjklsa", 0);
        } catch (WebApplicationException e) {
            int status = getStatus(e);
            assertThat(status).isEqualTo(400);
        }
    }

    @Test
    public void skal_kaste_exception_ved_store_tall() {
        try {
            validatePageNumber("999999999999999999999", 0);
        } catch (WebApplicationException e) {
            int status = getStatus(e);
            assertThat(status).isEqualTo(400);
        }
    }

    @Test
    public void skal_sjekke_om_page_number_er_storre_enn_totalt_antall_pages() {
        try {
            validatePageNumber("2", 1);
        } catch (WebApplicationException e) {
            int status = getStatus(e);
            assertThat(status).isEqualTo(404);
        }
    }

    @Test
    public void skal_sjekke_at_page_size_ikke_er_for_liten() {
        try {
            validatePageSize("0");
        } catch (WebApplicationException e) {
            int status = getStatus(e);
            assertThat(status).isEqualTo(400);
        }
    }

    @Test
    public void skal_sjekke_at_page_size_ikke_er_for_stor() {
        try {
            validatePageSize(Integer.toString(PAGE_SIZE_MAX + 1));
        } catch (WebApplicationException e) {
            int status = getStatus(e);
            assertThat(status).isEqualTo(400);
        }
    }

    private int getStatus(WebApplicationException e) {
        return e.getResponse().getStatus();
    }

}
