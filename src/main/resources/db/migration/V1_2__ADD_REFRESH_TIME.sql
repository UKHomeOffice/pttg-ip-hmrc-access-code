
ALTER TABLE access ADD COLUMN refresh_time TIMESTAMP;

UPDATE access SET refresh_time = expiry - INTERVAL '3' HOUR;

ALTER TABLE access ALTER COLUMN refresh_time SET NOT NULL;