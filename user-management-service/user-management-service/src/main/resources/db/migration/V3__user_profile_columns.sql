ALTER TABLE users
  ADD COLUMN first_name VARCHAR(100) NULL AFTER username,
  ADD COLUMN last_name  VARCHAR(100) NULL AFTER first_name,
  ADD COLUMN date_of_birth DATE NULL AFTER last_name,
  ADD COLUMN gender VARCHAR(16) NULL AFTER date_of_birth;
