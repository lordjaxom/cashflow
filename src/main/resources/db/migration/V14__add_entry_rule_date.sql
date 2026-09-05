ALTER TABLE entry
    ADD rule_date date;

UPDATE entry
SET rule_date = date
WHERE rule_id IS NOT NULL;
