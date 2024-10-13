-- add new (isAdmin) field
ALTER TABLE pb_client
    ADD COLUMN is_admin BOOLEAN;

ALTER TABLE pb_client
    ADD COLUMN is_superuser BOOLEAN;

UPDATE pb_client
SET is_superuser = true
WHERE chat_id = 516632773;