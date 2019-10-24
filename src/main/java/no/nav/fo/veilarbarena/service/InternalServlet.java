package no.nav.fo.veilarbarena.service;

import no.nav.fo.veilarbarena.scheduled.UserChangePublisher;
import no.nav.fo.veilarbarena.utils.AuthorizationUtils;
import org.springframework.context.annotation.Configuration;

import javax.inject.Inject;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import java.io.IOException;

@Configuration
public class InternalServlet extends HttpServlet {

    private UserChangePublisher userChangePublisher;

    @Inject
    public InternalServlet(UserChangePublisher userChangePublisher) {
        this.userChangePublisher = userChangePublisher;
    }

    @GET
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (AuthorizationUtils.isBasicAuthAuthorized(req)) {
            userChangePublisher.hentOgPubliserAlleOppfolgingsbrukere();
        } else {
            AuthorizationUtils.writeUnauthorized(res);
        }
    }
}
