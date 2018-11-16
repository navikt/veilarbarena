package no.nav.fo.veilarbarena.service;

import no.nav.dialogarena.aktor.AktorService;

import java.util.Optional;

public class AktorServiceMock implements AktorService{

    @Override
    public Optional<String> getFnr(String aktorId) {
        return Optional.empty();
    }

    @Override
    public Optional<String> getAktorId(String fnr) {
        return Optional.empty();
    }
}
