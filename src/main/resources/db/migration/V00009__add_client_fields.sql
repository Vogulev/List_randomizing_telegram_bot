ALTER TABLE pb_client
    RENAME TO telegram_user;

ALTER TABLE telegram_user
    ADD COLUMN if not exists telegram_id bigint unique;

ALTER TABLE telegram_user
    ADD COLUMN if not exists surname varchar;

ALTER TABLE telegram_user
    ADD COLUMN if not exists username varchar;