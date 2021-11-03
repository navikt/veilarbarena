CREATE TABLE OPPDATERTE_BRUKERE
(
    FNR         VARCHAR(33),
    TIDSSTEMPEL DATE,
    PRIMARY KEY (FNR)
);

CREATE OR REPLACE TRIGGER Arena_pa_kafka
    AFTER INSERT OR UPDATE
    ON OPPFOLGINGSBRUKER
    FOR EACH ROW
BEGIN
    merge into OPPDATERTE_BRUKERE
    using dual
    on (FNR = :new.FODSELSNR)
    when not matched then
        insert (FNR, TIDSSTEMPEL) values (:new.FODSELSNR, :new.TIDSSTEMPEL)
    when matched then
        update set TIDSSTEMPEL = :new.TIDSSTEMPEL;
END;
/