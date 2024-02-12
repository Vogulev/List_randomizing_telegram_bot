-- ensure that the table with this name is removed before creating a new one.
DROP TABLE IF EXISTS pb_client;

-- Create user table
CREATE TABLE pb_client
(
    id bigserial primary key,
    name varchar,
    chat_id bigint
);