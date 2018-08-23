package no.nav.fo.veilarbarena;

import io.vavr.API;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.control.Option;
import io.vavr.control.Try;
import lombok.Value;
import no.nav.fo.veilarbarena.domain.PersonId;
import no.nav.fo.veilarbarena.soapproxy.oppfolgingstatus.OppfolgingstatusController;

import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;
import static io.vavr.Predicates.instanceOf;

public class RestUtils {
    public static Option<PersonId> getUserIdent(Provider<HttpServletRequest> requestProvider) {
        Option<PersonId> fnr = Option.of(requestProvider.get().getParameter("fnr")).map(PersonId::fnr);
        Option<PersonId> aktorId = Option.of(requestProvider.get().getParameter("aktorId")).map(PersonId::fnr);

        return fnr.orElse(aktorId);
    }
}
