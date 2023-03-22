package no.nav.veilarbarena.service;

import com.nimbusds.jwt.JWTClaimsSet;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.abac.AbacUtils;
import no.nav.common.abac.Pep;
import no.nav.common.abac.domain.request.ActionId;
import no.nav.common.auth.context.AuthContextHolder;
import no.nav.common.auth.context.UserRole;
import no.nav.common.types.identer.Fnr;
import no.nav.poao_tilgang.client.Decision;
import no.nav.poao_tilgang.client.NavAnsattTilgangTilEksternBrukerPolicyInput;
import no.nav.poao_tilgang.client.PoaoTilgangClient;
import no.nav.poao_tilgang.client.TilgangType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

@Slf4j
@Service
public class AuthService {

    private final AuthContextHolder authContextHolder;

    private final Pep veilarbPep;

    private final PoaoTilgangClient poaoTilgangClient;

    private final UnleashService unleashService;

    public static Logger secureLog = LoggerFactory.getLogger("SecureLog");

    @Autowired
    public AuthService(AuthContextHolder authContextHolder, Pep veilarbPep, PoaoTilgangClient poaoTilgangClient, UnleashService unleashService) {
        this.authContextHolder = authContextHolder;
        this.veilarbPep = veilarbPep;
        this.poaoTilgangClient = poaoTilgangClient;
        this.unleashService = unleashService;
    }

    public void sjekkTilgang(Fnr fnr) {
        String requestId = UUID.randomUUID().toString();
        String userRole = authContextHolder.getRole().map(UserRole::name).orElse("UKJENT");
        String tokenIssuer = authContextHolder.getIdTokenClaims().map(JWTClaimsSet::getIssuer).orElse("");
        String innloggetBrukerToken = authContextHolder.requireIdTokenString();
        Boolean abacDecision = veilarbPep.harTilgangTilPerson(innloggetBrukerToken, ActionId.READ, fnr);
        secureLog.info("abacDecision = {}, requestId , userRole = {}", abacDecision, requestId, userRole);

        if (unleashService.skalBrukePoaoTilgang()) {
            if (erSystembruker()) {
                Boolean harAADRolleForSystemTilgang = harAADRolleForSystemTilSystemTilgang();
                secureLog.info("AADRolleDecision = {}, requestId , userRole = {}", harAADRolleForSystemTilgang, requestId, userRole);
                if (!harAADRolleForSystemTilgang){
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN);
                }
            } else {
                Decision desicion = poaoTilgangClient.evaluatePolicy(new NavAnsattTilgangTilEksternBrukerPolicyInput(
                        hentInnloggetVeilederUUID(), TilgangType.LESE, fnr.get()
                )).getOrThrow();
                secureLog.info("PoaoTilgangDecision is deny = {}, requestId = {}", desicion.isDeny(), requestId);
                if (desicion.isDeny()) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN);
                }
            }
        } else {
            if (!abacDecision) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        }
    }


    public boolean erSystembruker() {
        return authContextHolder.erSystemBruker();
    }

    @SneakyThrows
    public void sjekkAtSystembrukerErWhitelistet(String... clientIdWhitelist) {
        String requestingAppClientId = authContextHolder.requireIdTokenClaims().getStringClaim("azp");
        boolean isWhitelisted = Arrays.asList(clientIdWhitelist).contains(requestingAppClientId);

        if (!isWhitelisted) {
            log.error("Systembruker {} er ikke whitelistet", requestingAppClientId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    public static Optional<String> getStringClaimOrEmpty(JWTClaimsSet claims, String claimName) {
        try {
            return ofNullable(claims.getStringClaim(claimName));
        } catch (Exception e) {
            return empty();
        }
    }

    public UUID hentInnloggetVeilederUUID() {
        return authContextHolder.getIdTokenClaims()
                .flatMap(claims -> getStringClaimOrEmpty(claims, "oid"))
                .map(UUID::fromString)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Fant ikke oid for innlogget veileder"));
    }

    public UUID hentInnloggetVeilederUUIDOrElseNull() {
        return authContextHolder.getIdTokenClaims()
                .flatMap(claims -> getStringClaimOrEmpty(claims, "oid"))
                .map(UUID::fromString)
                .orElse(null);
    }

    public String hentInnloggetVeilederpid() {
        return authContextHolder.getIdTokenClaims()
                .flatMap(claims -> getStringClaimOrEmpty(claims, "pid"))
                .orElse(null);
    }

    public String hentInnloggetVeilederNavIdent() {
        return authContextHolder.getIdTokenClaims()
                .flatMap(claims -> getStringClaimOrEmpty(claims, "NAVident"))
                .orElse(null);
    }

    public String hentInnloggetVeilederSubject() {
        return authContextHolder.getIdTokenClaims()
                .flatMap(claims -> getStringClaimOrEmpty(claims, "sub"))
                .orElse(null);
    }

    private boolean harAADRolleForSystemTilSystemTilgang() {
        return authContextHolder.getIdTokenClaims()
                .flatMap(claims -> {
                    try {
                        return Optional.ofNullable(claims.getStringListClaim("roles"));
                    } catch (ParseException e) {
                        return Optional.empty();
                    }
                })
                .orElse(emptyList())
                .contains("access_as_application");
    }

}
