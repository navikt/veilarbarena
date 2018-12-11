package no.nav.fo.veilarbarena.service;

import no.nav.dialogarena.aktor.AktorService;
import no.nav.fo.veilarbarena.api.UserDTO;
import no.nav.sbl.sql.SqlUtils;
import no.nav.sbl.sql.where.WhereClause;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.*;
import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

@Component
@Path("/oppfolgingsbruker/{fnr}")
public class OppfolgingsbrukerController {

    @Inject
    private JdbcTemplate db;

    @GET
    public UserDTO getOppfolgingsbruker(@PathParam("fnr") String fnr){
        UserDTO userDTO = hentOppfolgingsbruker(fnr);
        return userDTO;
    }

    public UserDTO hentOppfolgingsbruker(String fnr){
        WhereClause harFnr = WhereClause.equals("fodselsnr", fnr);

        UserDTO userDTO =  SqlUtils.select(db, "OPPFOLGINGSBRUKER", OppfolgingsbrukerController::mapper)
                .column("fodselsnr")
                .column("formidlingsgruppekode")
                .column("iserv_fra_dato")
                .column("nav_kontor")
                .column("kvalifiseringsgruppekode")
                .column("rettighetsgruppekode")
                .column("hovedmaalkode")
                .column("sikkerhetstiltak_type_kode")
                .column("fr_kode")
                .column("har_oppfolgingssak")
                .column("sperret_ansatt")
                .column("er_doed")
                .column("doed_fra_dato")
                .where(harFnr)
                .execute();

        return userDTO;
    }

    public static UserDTO mapper(ResultSet resultSet) throws SQLException{
        return UserDTO.builder()
                .fodselsnr(resultSet.getString("fodselsnr"))
                .formidlingsgruppekode(resultSet.getString("formidlingsgruppekode"))
                .iserv_fra_dato(convertTimestampToZonedDateTimeIfPresent(resultSet.getTimestamp("iserv_fra_dato")))
                .nav_kontor(resultSet.getString("nav_kontor"))
                .kvalifiseringsgruppekode(resultSet.getString("kvalifiseringsgruppekode"))
                .rettighetsgruppekode(resultSet.getString("rettighetsgruppekode"))
                .hovedmaalkode(resultSet.getString("hovedmaalkode"))
                .sikkerhetstiltak_type_kode(resultSet.getString("sikkerhetstiltak_type_kode"))
                .fr_kode(resultSet.getString("fr_kode"))
                .har_oppfolgingssak(convertStringToBoolean(resultSet.getString("har_oppfolgingssak")))
                .sperret_ansatt(convertStringToBoolean(resultSet.getString("sperret_ansatt")))
                .er_doed(convertStringToBoolean(resultSet.getString("er_doed")))
                .doed_fra_dato(convertTimestampToZonedDateTimeIfPresent(resultSet.getTimestamp("doed_fra_dato")))
                .build();
    }

    private static ZonedDateTime convertTimestampToZonedDateTimeIfPresent(Timestamp date){
        return Optional.ofNullable(date).isPresent() ?
                date.toLocalDateTime().atZone(ZoneId.systemDefault()) : null ;
    }

    private static boolean convertStringToBoolean(String flag){
        return Optional.ofNullable(flag).isPresent() ? flag.equals("J") : false ;
    }
}
