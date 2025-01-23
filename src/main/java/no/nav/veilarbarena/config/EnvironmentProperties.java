package no.nav.veilarbarena.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.env")
public class EnvironmentProperties {
    private String loginserviceIdportenDiscoveryUrl;

    private String loginserviceIdportenAudience;

    private String naisAadDiscoveryUrl;

    private String naisAadClientId;

    private String amtTiltakClientId;

    private String amtPersonServiceClientId;

    private String poaoTilgangFSSClientId;
    private String poaoTilgangGCPClientId;

    private String tiltaksgjennomforingApiClientId;

    private String veilarbregistreringClientId;

    private String veilarbregistreringClientIdGCP;

    private String naisStsDiscoveryUrl;

    private String dbUrl;

    private String kafkaBrokersUrl;

    private String soapStsUrl;

    private String ytelseskontraktV3Endpoint;

	private String poaoTilgangUrl;

	private String poaoTilgangScope;

    private String veilarbaktivitetScope;

    private String veilarbaktivitetUrl;
}
