-- set chat_id field unique
ALTER TABLE pb_client
    ADD CONSTRAINT chat_id_unique UNIQUE (chat_id);
