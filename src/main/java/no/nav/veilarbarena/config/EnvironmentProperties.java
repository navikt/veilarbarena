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

    private String openAmRefreshUrl;


    private String azureAdDiscoveryUrl;

    private String veilarbloginAzureAdClientId;


    private String azureAdB2cDiscoveryUrl;

    private String azureAdB2cClientId;


    private String naisStsDiscoveryUrl;

    private String abacUrl;

    private String aktorregisterUrl;

    private String dbUrl;

    private String kafkaBrokersUrl;

}
