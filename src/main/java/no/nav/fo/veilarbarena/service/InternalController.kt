package no.nav.fo.veilarbarena.service

import no.nav.fo.veilarbarena.scheduled.UserChangePublisher
import no.nav.fo.veilarbarena.utils.AuthorizationUtils
import org.springframework.stereotype.Component
import javax.inject.Inject
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.ws.rs.GET
import javax.ws.rs.Path

@Component
@WebServlet
@Path("/internal")
class InternalController @Inject constructor (
        private val userChangePublisher: UserChangePublisher
) : HttpServlet() {

    @GET
    @Path("/publiser_alle_brukere")
    override fun doGet(req: HttpServletRequest, res: HttpServletResponse) {
        if (AuthorizationUtils.isBasicAuthAuthorized(req)) {
            userChangePublisher.hentOgPubliserAlleOppfolgingsbrukere()
        } else {
            AuthorizationUtils.writeUnauthorized(res)
        }
    }
}