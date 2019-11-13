package no.nav.fo.veilarbarena.service;

import no.nav.batch.BatchJob;
import no.nav.fo.veilarbarena.scheduled.UserChangePublisher;
import no.nav.fo.veilarbarena.utils.AuthorizationUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import java.io.IOException;

@Component
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

            String jobId = BatchJob.runAsync(() -> {
                userChangePublisher.testOmDetFaktiskErKafkaSomErTreigSomFaen();
            });

            res.getWriter().write(String.format("Jobb med jobId %s startet", jobId));
            res.setStatus(200);

//            userChangePublisher.hentOgPubliserAlleOppfolgingsbrukere(); TODO: fix this
        } else {
            AuthorizationUtils.writeUnauthorized(res);
        }
    }
}
