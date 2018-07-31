package no.nav.fo.veilarbarena.service;

import no.nav.fo.veilarbarena.domain.Iserv28;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.time.ZonedDateTime;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class Iserv28ServiceTest {

    @Test
    public void skal_respektere_pagesize() {
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        JdbcTemplate db = mock(JdbcTemplate.class);
        when(db.query(sqlCaptor.capture(), any(Object[].class), ArgumentMatchers.<RowMapper<Iserv28>>any()))
                .thenReturn(emptyList());

        Iserv28Service service = new Iserv28Service(db);

        service.finnBrukereMedIservI28Dager(ZonedDateTime.now(), 199);

        String sql = sqlCaptor.getValue();

        assertThat(sql).contains("SELECT tidsstempel, aktoerid, iserv_fra_dato");
        assertThat(sql).contains("199 ROWS ONLY");
    }
}
