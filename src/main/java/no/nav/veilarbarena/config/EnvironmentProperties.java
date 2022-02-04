package no.nav.veilarbarena.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.env")
public class EnvironmentProperties {

    private String openAmDiscoveryUrl;

    private String veilarbloginOpenAmClientId;
    private String modialoginOpenAmClientId;

    private String openAmRefreshUrl;


    private String azureAdDiscoveryUrl;

    private String veilarbloginAzureAdClientId;


    private String loginserviceIdportenDiscoveryUrl;

    private String loginserviceIdportenAudience;


    private String naisAadDiscoveryUrl;

    private String naisAadClientId;

    private String poaoGcpProxyClientId;

    private String amtTiltakClientId;

    private String tiltaksgjennomforingApiClientId;

    private String veilarbregistreringClientId;

    private String naisStsDiscoveryUrl;

    private String abacUrl;

    private String aktorregisterUrl;

    private String dbUrl;

    private String kafkaBrokersUrl;

    private String unleashUrl;

    private String soapStsUrl;

    private String ytelseskontraktV3Endpoint;

}
