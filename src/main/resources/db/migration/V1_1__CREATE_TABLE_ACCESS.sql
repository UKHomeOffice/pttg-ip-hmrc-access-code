CREATE TABLE IF NOT EXISTS access
(
  id             BIGSERIAL PRIMARY KEY,
  code           VARCHAR(255) NOT NULL,
  expiry         TIMESTAMP    NOT NULL,
  created_date   TIMESTAMP    NOT NULL

);

INSERT INTO access (id, code, expiry, created_date)
VALUES (
    1,
    'placeholder',
    current_timestamp,
    '1999-12-31 23:59:59'
);


