-- Skal inneholde det samme som i migreringsfilene --
-- Hvis vi ikke lenger trenger database sync mot Arena så kan vi istedenfor bruke Flyway og migrere som vanlig for test --

-- V1_1__inital.sql
create table OPPFOLGINGSBRUKER (
  PERSON_ID NUMBER not null,
  FODSELSNR VARCHAR2(33) not null,
  ETTERNAVN VARCHAR2(90) not null,
  FORNAVN VARCHAR2(90) not null,
  NAV_KONTOR VARCHAR2(24),
  FORMIDLINGSGRUPPEKODE VARCHAR2(15) not null,
  ISERV_FRA_DATO DATE,
  KVALIFISERINGSGRUPPEKODE VARCHAR2(15) not null,
  RETTIGHETSGRUPPEKODE VARCHAR2(15) not null,
  HOVEDMAALKODE VARCHAR2(30),
  SIKKERHETSTILTAK_TYPE_KODE VARCHAR2(12),
  FR_KODE VARCHAR2(6),
  HAR_OPPFOLGINGSSAK VARCHAR2(3),
  SPERRET_ANSATT VARCHAR2(3),
  ER_DOED VARCHAR2(3) not null,
  DOED_FRA_DATO DATE,
  TIDSSTEMPEL DATE
);

-- V1_2__aktormapping.sql
CREATE TABLE METADATA (
  OPPFOLGINGSBRUKER_SIST_ENDRING TIMESTAMP NOT NULL
);

INSERT INTO METADATA (OPPFOLGINGSBRUKER_SIST_ENDRING)
VALUES (TO_TIMESTAMP('1970-01-01 00:00:00.000000', 'YYYY-MM-DD HH24:MI:SS.FF6'));

-- V1_3__metadata_fnr.sql
ALTER TABLE METADATA
ADD FODSELSNR VARCHAR2(33);


-- V1_4__feilede_kafka_meldinger.sql
CREATE TABLE FEILEDE_KAFKA_BRUKERE (
  FODSELSNR VARCHAR2(33) NOT NULL,
  TIDSPUNKT_FEILET TIMESTAMP NOT NULL
);


-- V1_5__legg_shedlock_tabell.sql
CREATE TABLE shedlock(
  name VARCHAR(64),
  lock_until TIMESTAMP(3) NULL,
  locked_at TIMESTAMP(3) NULL,
  locked_by  VARCHAR(255),
  PRIMARY KEY (name)
);

-- V1_6__fjern_shedlock_tabell.sql
DROP TABLE shedlock;

-- V1_7__fjern_feilede_kafka_brukere.sql
DROP TABLE FEILEDE_KAFKA_BRUKERE;

-- V1_8__kafka_producer_record.sql
CREATE SEQUENCE KAFKA_PRODUCER_RECORD_ID_SEQ;

CREATE TABLE KAFKA_PRODUCER_RECORD (
    ID                      NUMBER(18) NOT NULL,
    TOPIC                   VARCHAR(100) NOT NULL,
    KEY                     BLOB,
    VALUE                   BLOB,
    HEADERS_JSON            CLOB,
    CREATED_AT              TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    PRIMARY KEY (ID)	
);

-- V1_9__kafka_producer_record.sql (Trigger stottes ikke i H2 databasen)
CREATE TABLE OPPDATERTE_BRUKERE
(
    FNR         VARCHAR(33),
    TIDSSTEMPEL DATE,
    PRIMARY KEY (FNR)
);