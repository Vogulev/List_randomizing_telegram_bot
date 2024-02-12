-- ensure that the table with this name is removed before creating a new one.
DROP TABLE IF EXISTS pb_user;

-- Create user table
CREATE TABLE pb_user
(
    id bigserial primary key,
    name varchar unique not null
);