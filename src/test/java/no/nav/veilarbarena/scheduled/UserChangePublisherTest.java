package no.nav.veilarbarena.scheduled;

import static org.assertj.core.api.Java6Assertions.assertThat;


class UserChangePublisherTest {

//    @Inject
//    private KafkaRepository kafkaRepository;
//    @Inject
//    private UserChangePublisher userChangePublisher;
//    @Inject
//    private JdbcTemplate db;
//
//    @BeforeAll
//    static void setup() {
//        setupTestConfig();
//        initSpringContext(
//                KafkaRepository.class,
//                UserChangePublisher.class,
//                UserChangeListenerMock.class,
//                AktoerRegisterService.class,
//                RestClientConfig.class
//        );
//    }
//
//    @Test
//    void findChangesSinceLastCheck() {
//        final User user = lagNyBruker();
//        kafkaRepository.insertFeiletBruker(user);
//        insertOppfolgingsBruker(user);
//
//        final List<User> allFailedKafkaUsers = userChangePublisher.findAllFailedKafkaUsers();
//
//        assertThat(allFailedKafkaUsers.get(0).getFodselsnr().get()).isEqualTo(user.getFodselsnr().get());
//
//    }
//
//    private void insertOppfolgingsBruker(User user) {
//        SqlUtils.insert(db, "OPPFOLGINGSBRUKER")
//                .value("FODSELSNR", user.getFodselsnr().get())
//                .value("PERSON_ID", "123")
//                .value("ETTERNAVN", "WAYNE")
//                .value("FORNAVN", "BRUCE")
//                .value("FORMIDLINGSGRUPPEKODE", "ISERV")
//                .value("KVALIFISERINGSGRUPPEKODE", "ASDF")
//                .value("RETTIGHETSGRUPPEKODE", "VLONN")
//                .value("ER_DOED", "N")
//                .execute();
//    }
//
//    public static void setupTestConfig() {
//        setProperty("no.nav.modig.security.systemuser.username", "test");
//        setProperty("no.nav.modig.security.systemuser.password", "test");
//        setProperty(ApplicationConfig.AKTOERREGISTER_API_V1_URL,"test");
//        setProperty(Constants.ISSO_HOST_URL_PROPERTY_NAME,"test");
//        setProperty(Constants.ISSO_RP_USER_USERNAME_PROPERTY_NAME,"test");
//        setProperty(Constants.ISSO_RP_USER_PASSWORD_PROPERTY_NAME,"test");
//        setProperty(Constants.ISSO_JWKS_URL_PROPERTY_NAME,"test");
//        setProperty(Constants.ISSO_ISSUER_URL_PROPERTY_NAME,"test");
//        setProperty(OIDC_REDIRECT_URL_PROPERTY_NAME,"test");
//        setProperty("NAIS_APP_NAME","test");
//    }
}
