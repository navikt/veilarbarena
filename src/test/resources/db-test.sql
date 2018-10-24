-- setter opp schema_version for flyway
create table "schema_version" (
  "installed_rank" NUMBER not null
    constraint "schema_version_pk"
    primary key,
  "version" VARCHAR2(50),
  "description" VARCHAR2(200) not null,
  "type" VARCHAR2(20) not null,
  "script" VARCHAR2(1000) not null,
  "checksum" NUMBER,
  "installed_by" VARCHAR2(100) not null,
  "installed_on" TIMESTAMP(6) default CURRENT_TIMESTAMP not null,
  "execution_time" NUMBER not null,
  "success" NUMBER(1) not null
);

-- hopper over materialiserte views fra arena
INSERT INTO "PUBLIC"."schema_version" ("installed_rank", "version", "description", "type", "script", "checksum", "installed_by", "installed_on", "execution_time", "success") VALUES (1, '1.1', 'initial', 'SQL', 'V1_1__initial.sql', -1309861431, 'VEILARBARENA_Q6', TO_TIMESTAMP('2018-08-28 15:42:35.090665', 'YYYY-MM-DD HH24:MI:SS.FF6'), 7304, 1);

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
