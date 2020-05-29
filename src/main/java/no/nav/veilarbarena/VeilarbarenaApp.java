package no.nav.veilarbarena;

import no.nav.common.cxf.StsSecurityConstants;
import no.nav.common.utils.Credentials;
import no.nav.common.utils.SslUtils;
import org.springframework.boot.SpringApplication;

import static no.nav.common.utils.NaisUtils.getCredentials;


public class VeilarbarenaApp {
    public static void main(String... args) {
        Credentials serviceUser = getCredentials("service_user");

        //CXF
        System.setProperty(StsSecurityConstants.SYSTEMUSER_USERNAME, serviceUser.username);
        System.setProperty(StsSecurityConstants.SYSTEMUSER_PASSWORD, serviceUser.password);

        SslUtils.setupTruststore();
        SpringApplication.run(VeilarbarenaApp.class, args);
    }
}
