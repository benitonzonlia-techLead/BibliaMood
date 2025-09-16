-- Ensure score column exists on verse_emotion for tests
ALTER TABLE IF EXISTS verse_emotion ADD COLUMN IF NOT EXISTS score DOUBLE;
CREATE SCHEMA IF NOT EXISTS bibliamood;
