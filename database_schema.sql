-- ====================================
-- FarmMarket Database Schema
-- Email OTP Authentication Support
-- ====================================

-- Database creation (automatically handled by Spring Boot with createDatabaseIfNotExist=true)
CREATE DATABASE IF NOT EXISTS farmmarket_db;
USE farmmarket_db;

-- The users table will be automatically created/updated by Hibernate with these fields:
-- 
-- User Entity Fields:
-- - id (BIGINT, AUTO_INCREMENT, PRIMARY KEY)
-- - name (VARCHAR(255), NOT NULL)
-- - email (VARCHAR(255), NOT NULL, UNIQUE)
-- - password (VARCHAR(255), NOT NULL)
-- - role (VARCHAR(20), NOT NULL) -- FARMER, BUYER, ADMIN
-- - phone_number (VARCHAR(50))
-- - address (TEXT)
-- - city (VARCHAR(100))
-- - state (VARCHAR(100))
-- - zip_code (VARCHAR(20))
-- - profile_image_url (VARCHAR(500))
-- - is_active (BOOLEAN, DEFAULT TRUE)
-- 
-- OTP Fields (NEW):
-- - otp (VARCHAR(6)) -- 6-digit OTP code
-- - otp_expiry (DATETIME) -- OTP expiration timestamp
-- - otp_attempts (INT, DEFAULT 0) -- Number of failed OTP attempts
-- - last_otp_sent (DATETIME) -- Timestamp of last OTP sent (for rate limiting)
-- 
-- Farmer-specific fields:
-- - farm_name (VARCHAR(255))
-- - farm_location (VARCHAR(255))
-- - farm_bio (TEXT)
-- 
-- Timestamps:
-- - created_at (DATETIME)
-- - updated_at (DATETIME)

-- Manual Index Creation (Optional - Hibernate handles this)
-- CREATE UNIQUE INDEX idx_user_email ON users(email);
-- CREATE INDEX idx_user_role ON users(role);
-- CREATE INDEX idx_otp_expiry ON users(otp_expiry);

-- ====================================
-- Sample OTP Authentication Flow
-- ====================================

-- 1. User requests OTP:
--    INSERT/UPDATE users SET otp='123456', otp_expiry='2026-04-06 03:00:00', 
--    otp_attempts=0, last_otp_sent='2026-04-06 02:55:00' WHERE email='user@example.com';

-- 2. User verifies OTP:
--    SELECT * FROM users WHERE email='user@example.com' AND otp='123456' AND otp_expiry > NOW();

-- 3. On successful verification:
--    UPDATE users SET otp=NULL, otp_expiry=NULL, otp_attempts=0, is_active=TRUE WHERE email='user@example.com';

-- 4. On failed verification:
--    UPDATE users SET otp_attempts = otp_attempts + 1 WHERE email='user@example.com';

-- ====================================
-- Query Examples
-- ====================================

-- Check active OTPs (for debugging):
-- SELECT email, otp, otp_expiry, otp_attempts, last_otp_sent 
-- FROM users 
-- WHERE otp IS NOT NULL AND otp_expiry > NOW();

-- Find users who exceeded OTP attempts:
-- SELECT email, otp_attempts FROM users WHERE otp_attempts >= 3;

-- Clean up expired OTPs (optional scheduled task):
-- UPDATE users SET otp=NULL, otp_expiry=NULL WHERE otp_expiry < NOW();
