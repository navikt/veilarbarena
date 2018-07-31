package no.nav.fo.veilarbarena.config;

import no.nav.fo.feed.controller.FeedController;
import no.nav.fo.feed.producer.FeedProducer;
import no.nav.fo.veilarbarena.domain.Iserv28;
import no.nav.fo.veilarbarena.feed.Iserv28Provider;
import no.nav.fo.veilarbarena.service.Iserv28Service;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class FeedConfig {

    @Bean
    public Iserv28Service iserv28Service(JdbcTemplate jdbcTemplate) {
        return new Iserv28Service(jdbcTemplate);
    }

    @Bean
    public Iserv28Provider iserv28Provider(Iserv28Service iserv28Service) {
        return new Iserv28Provider(iserv28Service);
    }

    @Bean
    public FeedProducer<Iserv28> iserv28FeedProducer(Iserv28Provider provider) {
        return FeedProducer.<Iserv28>builder()
                .provider(provider)
                .maxPageSize(1000)
// TODO finne løsning for lokal-kjøring
//                .interceptors(singletonList(new OidcFeedOutInterceptor()))
//                .authorizationModule(new OidcFeedAuthorizationModule())
                .build();
    }

    @Bean
    public FeedController feedController(FeedProducer<Iserv28> iserv28FeedProducer) {
        FeedController controller = new FeedController();
        controller.addFeed("iserv28", iserv28FeedProducer);
        return controller;
    }


}
