-- Incremental migration for existing clothify_store_db installations.
-- For fresh installs, use schema.sql instead.

USE clothify_store_db;

-- Run only if upgrading from v1 schema (skip if tables already exist)
