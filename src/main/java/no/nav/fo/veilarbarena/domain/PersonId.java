package no.nav.fo.veilarbarena.domain;

import lombok.EqualsAndHashCode;
import no.nav.dialogarena.aktor.AktorService;

@EqualsAndHashCode
public abstract class PersonId {
    private final String id;

    private PersonId(String id) {
        this.id = id;
    }

    public String get() {
        return id;
    }

    public static Fnr fnr(String fnr) {
        return new Fnr(fnr);
    }
    public static AktorId aktorId(String aktorId) {
        return new AktorId(aktorId);
    }

    public abstract Fnr toFnr(AktorService service);
    public abstract AktorId toAktorId(AktorService service);

    public static class Fnr extends PersonId {
        private Fnr(String id) {
            super(id);
        }

        @Override
        public Fnr toFnr(AktorService service) {
            return this;
        }

        @Override
        public AktorId toAktorId(AktorService service) {
            return service.getAktorId(this.get())
                    .map(PersonId::aktorId)
                    .orElseThrow(() -> new FnrAktorIdConvertionException("Fant ikke aktorId for " + this.get()));
        }
    }

    public static class AktorId extends PersonId {

        private AktorId(String id) {
            super(id);
        }

        @Override
        public Fnr toFnr(AktorService service) {
            return service.getFnr(this.get())
                    .map(PersonId::fnr)
                    .orElseThrow(() -> new FnrAktorIdConvertionException("Fant ikke fnr for " + this.get()));
        }

        @Override
        public AktorId toAktorId(AktorService service) {
            return this;
        }
    }

    static class FnrAktorIdConvertionException extends RuntimeException {
        FnrAktorIdConvertionException(String message) {
            super(message);
        }
    }
}
