CREATE SEQUENCE KAFKA_PRODUCER_RECORD_ID_SEQ;

CREATE TABLE KAFKA_PRODUCER_RECORD (
    ID                      NUMBER(18) NOT NULL PRIMARY KEY,
    TOPIC                   VARCHAR(100) NOT NULL,
    KEY                     BLOB,
    VALUE                   BLOB,
    HEADERS_JSON            CLOB,
    CREATED_AT              TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);