package no.nav.fo.veilarbarena.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.brukerdialog.security.domain.IdentType;
import no.nav.common.auth.SubjectHandler;
import no.nav.fo.veilarbarena.api.UserDTO;
import no.nav.fo.veilarbarena.api.UserPageDTO;
import no.nav.sbl.sql.SqlUtils;
import no.nav.sbl.sql.order.OrderClause;
import no.nav.sbl.sql.where.WhereClause;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static no.nav.brukerdialog.security.domain.IdentType.Systemressurs;

@Slf4j
@Component
@Path("/oppfolgingsbruker")
public class OppfolgingsbrukerController {

    static final int PAGE_SIZE_MAX = 1000;
    private static final int PAGE_NUMBER_MAX = 500_000;

    private final JdbcTemplate db;
    private final AuthService authService;

    @Inject
    public OppfolgingsbrukerController(JdbcTemplate db, AuthService authService) {
        this.db = db;
        this.authService = authService;
    }


    @GET
    @Path("/{fnr}")
    public UserDTO getOppfolgingsbruker(@PathParam("fnr") String fnr){
        authService.sjekkTilgang(fnr);
        return hentOppfolgingsbrukere(fnr);
    }

    @GET
    public UserPageDTO getOppfolgingsbruker(@DefaultValue("1") @QueryParam("page_number") int pageNumber, @DefaultValue("10") @QueryParam("page_size") int pageSize) {

        autoriserBruker();

        int totalNumberOfUsers = getTotalNumberOfUsers().orElseThrow(() -> new WebApplicationException(503));


        validatePageSize(pageSize);
        validatePageNumber(pageNumber, totalNumberOfUsers);

        int totalNumberOfPages = totalNumberOfUsers / pageSize;
        List<UserDTO> users = hentOppfolgingsbrukere(pageNumber, pageSize);

        String msg = String.format("totalNumberOfUsers: %s, users.size(): %s", totalNumberOfPages, users.size());
        log.info(msg);

        return new UserPageDTO(pageNumber, totalNumberOfPages, users);
    }

    private void autoriserBruker() {
        IdentType identType = SubjectHandler.getIdentType().orElseThrow(NotFoundException::new);
        String ident = SubjectHandler.getIdent().orElseThrow(NotFoundException::new);

        if (ugyldigIdent(identType, ident)) {
            throw new NotFoundException();
        }
    }

    static boolean ugyldigIdent(IdentType identType, String ident) {
        return !identType.equals(Systemressurs) || !"srvveilarboppfolging".equals(ident);
    }

    private Optional<Integer> getTotalNumberOfUsers() {
        Integer count = db.query("SELECT COUNT(*) FROM OPPFOLGINGSBRUKER", rs -> {
            rs.next();
            return rs.getInt(1);
        });
        return Optional.ofNullable(count);
    }

    private UserDTO hentOppfolgingsbrukere(String fnr){
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

    private List<UserDTO> hentOppfolgingsbrukere(int page, int pageSize){

        int rowNum = calculateRowNum(page, pageSize);

        String msg = String.format("rowNum: %s, page: %s, pageSize: %s", rowNum, page, pageSize);
        log.info(msg);

        return SqlUtils.select(db, "OPPFOLGINGSBRUKER", OppfolgingsbrukerController::mapper)
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
                .limit(pageSize)
                .orderBy(OrderClause.asc("fodselsnr"))
                .where(WhereClause.lt("ROWNUM", rowNum))
                .executeToList();
    }

    private static int calculateRowNum(int page, int pageSize) {
        return (page * pageSize) + 1;
    }


    private static UserDTO mapper(ResultSet resultSet) throws SQLException{
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
        return Optional.ofNullable(flag).isPresent() && flag.equals("J");
    }

    static void validatePageNumber(int pageNumber, int pagesTotal) {

        if (pageNumber < 1) {
            throw new WebApplicationException("Page number is below 1", 400);
        }

        if (pageNumber > pagesTotal) {
            throw new WebApplicationException("Page number is higher than total number of pages", 404);
        }

        if (pageNumber > PAGE_NUMBER_MAX) {
            throw new WebApplicationException("Page number exceeds max limit", 400);
        }
    }

    static int validatePageSize(int pageSize) {

        if (pageSize < 1) {
            throw new WebApplicationException("Page size too small", 400);
        }

        if (pageSize > PAGE_SIZE_MAX) {
            throw new WebApplicationException("Page size exceeds max limit", 400);
        }

        return pageSize;
    }
}
