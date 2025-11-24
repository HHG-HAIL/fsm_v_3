-- Add password column to users table
ALTER TABLE users ADD COLUMN password VARCHAR(255) NOT NULL DEFAULT 'temp_password';

-- Remove default after column is added (to enforce NOT NULL in future inserts)
ALTER TABLE users ALTER COLUMN password DROP DEFAULT;
