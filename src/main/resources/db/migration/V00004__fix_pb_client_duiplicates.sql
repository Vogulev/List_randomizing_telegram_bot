-- set chat_id field unique
DELETE FROM pb_client a
WHERE a.ctid <> (SELECT min(b.ctid)
                 FROM   pb_client b WHERE  a.chat_id = b.chat_id);

ALTER TABLE pb_client
    ADD CONSTRAINT chat_id_unique UNIQUE (chat_id);
