package no.nav.fo.veilarbarena;

import io.vavr.control.Option;
import io.vavr.control.Try;
import no.nav.fo.veilarbarena.domain.PersonId;

import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

public class RestUtils {
    public static Option<PersonId> getUserIdent(Provider<HttpServletRequest> requestProvider) {
        Option<PersonId> fnr = Option.of(requestProvider.get().getParameter("fnr")).map(PersonId::fnr);
        Option<PersonId> aktorId = Option.of(requestProvider.get().getParameter("aktorId")).map(PersonId::fnr);

        return fnr.orElse(aktorId);
    }
}
