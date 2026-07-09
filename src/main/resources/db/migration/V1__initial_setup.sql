-- PostgreSQL extensions required by TaskFlow

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";   -- UUID generation helpers
CREATE EXTENSION IF NOT EXISTS "pg_trgm";     -- Trigram text similarity (fuzzy search)
CREATE EXTENSION IF NOT EXISTS "btree_gin";   -- B-tree operators on GIN indexes
