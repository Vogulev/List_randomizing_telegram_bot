UPDATE telegram_user SET is_admin = false WHERE telegram_user.is_admin IS NULL;
UPDATE telegram_user SET is_superuser = false WHERE telegram_user.is_superuser IS NULL;