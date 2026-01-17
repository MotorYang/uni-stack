---
name: mysql-database-design
description: MySQL 9.x database design standards and best practices. Use when designing database schemas, creating tables, writing DDL statements, or validating database structures. Enforces UUID7 primary keys, audit fields, logical deletion, proper indexing, and microservice-friendly constraints for distributed systems.
---

# MySQL Database Design Standards (MySQL 9.x)

Comprehensive database design rules for microservice architectures with MySQL 9.x, optimized for distributed systems, performance, and maintainability.

## Quick Reference

| Aspect | Standard | Example |
|--------|----------|---------|
| Primary Key | `id VARCHAR(36)` with UUID7 | `id VARCHAR(36) PRIMARY KEY COMMENT 'UUID7 primary key'` |
| Timestamps | `DATETIME(6)` microsecond precision | `create_time DATETIME(6) NOT NULL` |
| Logical Delete | `del_flag TINYINT(1) DEFAULT 0` | `0=active, 1=deleted` |
| Non-unique Index | `idx_` prefix | `idx_username`, `idx_email` |
| Unique Index | `uk_` prefix | `uk_email`, `uk_phone` |
| Foreign Keys | **PROHIBITED** | Use business layer validation |

## Mandatory Base Fields

Every table MUST include these audit fields:

```sql
CREATE TABLE table_name (
  -- Primary Key
  id VARCHAR(36) PRIMARY KEY COMMENT 'UUID7 primary key',
  
  -- Business fields...
  
  -- Audit Fields (REQUIRED)
  create_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Creation timestamp',
  update_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'Last update timestamp',
  create_by VARCHAR(64) DEFAULT NULL COMMENT 'Creator user ID',
  update_by VARCHAR(64) DEFAULT NULL COMMENT 'Last updater user ID',
  del_flag TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Logical delete flag: 0=active, 1=deleted',
  
  -- Indexes
  INDEX idx_create_time (create_time),
  INDEX idx_del_flag (del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Table description';
```

### Field Specifications

**Primary Key (`id`)**:
- Type: `VARCHAR(36)` to store UUID7 string format
- UUID7 provides: time-ordered sortability + distributed uniqueness + index-friendly structure
- Never use auto-increment IDs in distributed systems
- Never use traditional UUID v4 (random, non-sequential, poor index performance)

**Timestamps**:
- Type: `DATETIME(6)` for microsecond precision (critical for high-frequency operations)
- `create_time`: Auto-set on INSERT via `DEFAULT CURRENT_TIMESTAMP(6)`
- `update_time`: Auto-update on UPDATE via `ON UPDATE CURRENT_TIMESTAMP(6)`
- Index `create_time` for time-range queries

**Audit Fields**:
- `create_by` / `update_by`: Store user ID (VARCHAR for UUID compatibility)
- Nullable fields (use NULL when system-generated or anonymous operations)
- Populated by application layer (MyBatis-Plus MetaObjectHandler)

**Logical Deletion (`del_flag`)**:
- Type: `TINYINT(1)` for space efficiency
- Values: `0` = active, `1` = deleted
- **ALWAYS** index this field for query performance
- Composite indexes: include `del_flag` as first column when appropriate

## Primary Key Strategy: UUID7

### Why UUID7?

| Feature | UUID7 | Auto-increment | UUID v4 |
|---------|-------|----------------|---------|
| Distributed-friendly | ✅ | ❌ | ✅ |
| Time-ordered | ✅ | ✅ | ❌ |
| Index performance | ✅ | ✅ | ❌ |
| No coordination needed | ✅ | ❌ | ✅ |
| Predictability | Medium | High | Low |

### UUID7 Generation

**Java (Recommended library: `uuid-creator`)**:
```java
// Maven dependency
<dependency>
    <groupId>com.github.f4b6a3</groupId>
    <artifactId>uuid-creator</artifactId>
    <version>5.3.7</version>
</dependency>

// Usage
import com.github.f4b6a3.uuid.UuidCreator;

String uuid7 = UuidCreator.getTimeOrderedEpoch().toString();
```

**MyBatis-Plus Integration**:
```java
@TableId(type = IdType.ASSIGN_UUID) // Use custom UUID7 generator
private String id;
```

Configure custom ID generator in MyBatis-Plus config (see `references/mybatis-plus-config.md`).

## Forbidden Patterns

### ❌ Physical Foreign Keys

**NEVER use physical foreign keys**:
```sql
-- ❌ WRONG - Do NOT do this
CONSTRAINT fk_user_dept FOREIGN KEY (dept_id) REFERENCES sys_dept(id)
```

**Reasons**:
1. Blocks distributed database scaling (sharding/partitioning)
2. Cross-service references impossible in microservices
3. Performance overhead on INSERT/UPDATE/DELETE
4. Complex cascading delete management
5. Schema migration difficulties

**✅ Correct Approach**:
```sql
-- Define logical reference in column comment
dept_id VARCHAR(36) DEFAULT NULL COMMENT 'Department ID (references sys_dept.id)',
INDEX idx_dept_id (dept_id)
```

Enforce referential integrity in application layer:
- Use `@Transactional` for consistency
- Implement service-layer validation
- Use Feign for cross-service checks

### ❌ Other Prohibited Patterns

**Auto-increment IDs**:
```sql
-- ❌ WRONG
id BIGINT AUTO_INCREMENT PRIMARY KEY
```

**Traditional UUID v4**:
```sql
-- ❌ WRONG - random UUIDs fragment indexes
id CHAR(36) DEFAULT (UUID()) PRIMARY KEY
```

**Missing Audit Fields**:
```sql
-- ❌ WRONG - incomplete audit trail
CREATE TABLE users (
  id VARCHAR(36) PRIMARY KEY,
  username VARCHAR(50)
  -- Missing: create_time, update_time, create_by, update_by, del_flag
);
```

## Index Naming Conventions

### Non-unique Indexes: `idx_` Prefix

**Single column**:
```sql
INDEX idx_username (username),
INDEX idx_email (email),
INDEX idx_status (status)
```

**Composite index** (list all columns):
```sql
INDEX idx_dept_status (dept_id, status),
INDEX idx_create_time_del_flag (create_time, del_flag)
```

### Unique Indexes: `uk_` Prefix

**Single column**:
```sql
UNIQUE KEY uk_email (email),
UNIQUE KEY uk_phone (phone)
```

**Composite unique**:
```sql
UNIQUE KEY uk_username_tenant (username, tenant_id)
```

### Index Design Best Practices

**Column Order (Composite Indexes)**:
1. Equality conditions first (`WHERE col = ?`)
2. Range conditions last (`WHERE col > ?`)
3. High cardinality before low cardinality

**Example**:
```sql
-- Query: WHERE dept_id = ? AND status = ? AND create_time > ?
-- Correct order:
INDEX idx_dept_status_create_time (dept_id, status, create_time)
```

**Covering Indexes**:
```sql
-- Query: SELECT id, username, email WHERE status = 1
-- Covering index avoids table lookup:
INDEX idx_status_username_email (status, username, email)
```

**Logical Delete Optimization**:
```sql
-- Most queries filter del_flag=0, so include it first:
INDEX idx_del_flag_create_time (del_flag, create_time),
INDEX idx_del_flag_status (del_flag, status)
```

## Character Set & Collation (MySQL 9.x)

**Standard Configuration**:
```sql
ENGINE=InnoDB 
DEFAULT CHARSET=utf8mb4 
COLLATE=utf8mb4_0900_ai_ci
```

**Why `utf8mb4_0900_ai_ci`**:
- Full Unicode support (including emojis 😊)
- Accent-insensitive (`José` = `Jose`)
- Case-insensitive (recommended for most applications)
- Performance-optimized in MySQL 8.0+

**Case-sensitive Alternative** (if needed):
```sql
COLLATE=utf8mb4_0900_as_cs
```

## Data Type Selection Guide

### Strings

| Use Case | Type | Reason |
|----------|------|--------|
| IDs (UUID7) | `VARCHAR(36)` | Exact UUID string length |
| Short text (username, code) | `VARCHAR(64)` | Indexed efficiently |
| Medium text (address, description) | `VARCHAR(255)` | Common limit |
| Long text (content, JSON) | `TEXT` or `JSON` | No length constraint |
| Fixed codes (country, currency) | `CHAR(2)` or `CHAR(3)` | Fixed length, efficient |

### Numbers

| Use Case | Type | Reason |
|----------|------|--------|
| Boolean flags | `TINYINT(1)` | Minimal storage (1 byte) |
| Small enums (status 0-255) | `TINYINT UNSIGNED` | 1 byte, 0-255 range |
| Counters, quantities | `INT` or `BIGINT` | Standard integer types |
| Money (cents) | `BIGINT` | Avoid `DECIMAL` precision issues |
| Percentages | `DECIMAL(5,2)` | e.g., 100.00% |
| Scientific values | `DOUBLE` | Only when needed |

### Temporal

| Use Case | Type | Reason |
|----------|------|--------|
| Audit timestamps | `DATETIME(6)` | Microsecond precision |
| Event logs | `DATETIME(6)` | High-frequency events |
| Date only (birthdate) | `DATE` | No time component |
| Year only | `YEAR` | Compact (1 byte) |

**Avoid `TIMESTAMP`**: Limited range (1970-2038) and timezone issues.

### JSON

MySQL 9.x has excellent JSON support:
```sql
metadata JSON COMMENT 'Flexible metadata storage',
settings JSON COMMENT 'User-specific settings'
```

**JSON Indexes** (MySQL 9.x):
```sql
-- Index specific JSON field:
INDEX idx_metadata_type ((CAST(metadata->>'$.type' AS CHAR(50))))
```

## Table Design Patterns

### Standard Business Table

```sql
CREATE TABLE sys_user (
  -- Primary Key
  id VARCHAR(36) PRIMARY KEY COMMENT 'UUID7 primary key',
  
  -- Business Fields
  username VARCHAR(64) NOT NULL COMMENT 'Login username',
  password VARCHAR(128) NOT NULL COMMENT 'Encrypted password',
  nickname VARCHAR(64) DEFAULT NULL COMMENT 'Display name',
  email VARCHAR(128) DEFAULT NULL COMMENT 'Email address',
  phone VARCHAR(32) DEFAULT NULL COMMENT 'Phone number',
  avatar VARCHAR(512) DEFAULT NULL COMMENT 'Avatar URL',
  
  -- Status Fields
  status TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'Status: 0=disabled, 1=enabled',
  
  -- Logical Foreign Keys (NO physical FK constraints)
  dept_id VARCHAR(36) DEFAULT NULL COMMENT 'Department ID (references sys_dept.id)',
  
  -- Audit Fields
  create_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Creation timestamp',
  update_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'Update timestamp',
  create_by VARCHAR(64) DEFAULT NULL COMMENT 'Creator user ID',
  update_by VARCHAR(64) DEFAULT NULL COMMENT 'Last updater user ID',
  del_flag TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Delete flag: 0=active, 1=deleted',
  
  -- Indexes
  UNIQUE KEY uk_username (username),
  UNIQUE KEY uk_email (email),
  INDEX idx_dept_id (dept_id),
  INDEX idx_status (status),
  INDEX idx_del_flag_create_time (del_flag, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='System user table';
```

### Many-to-Many Relationship Table

```sql
CREATE TABLE sys_user_role (
  -- Primary Key
  id VARCHAR(36) PRIMARY KEY COMMENT 'UUID7 primary key',
  
  -- Relationship Fields
  user_id VARCHAR(36) NOT NULL COMMENT 'User ID (references sys_user.id)',
  role_id VARCHAR(36) NOT NULL COMMENT 'Role ID (references sys_role.id)',
  
  -- Audit Fields
  create_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Creation timestamp',
  update_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'Update timestamp',
  create_by VARCHAR(64) DEFAULT NULL COMMENT 'Creator user ID',
  update_by VARCHAR(64) DEFAULT NULL COMMENT 'Last updater user ID',
  del_flag TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Delete flag: 0=active, 1=deleted',
  
  -- Indexes
  UNIQUE KEY uk_user_role (user_id, role_id),
  INDEX idx_role_id (role_id),
  INDEX idx_del_flag (del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='User-Role relationship table';
```

### Tree Structure Table (Self-referencing)

```sql
CREATE TABLE sys_dept (
  -- Primary Key
  id VARCHAR(36) PRIMARY KEY COMMENT 'UUID7 primary key',
  
  -- Tree Structure
  parent_id VARCHAR(36) DEFAULT NULL COMMENT 'Parent department ID (NULL for root)',
  ancestors VARCHAR(512) DEFAULT NULL COMMENT 'Ancestor chain (comma-separated IDs)',
  
  -- Business Fields
  dept_name VARCHAR(64) NOT NULL COMMENT 'Department name',
  dept_code VARCHAR(32) NOT NULL COMMENT 'Department code',
  sort_order INT DEFAULT 0 COMMENT 'Display order',
  leader_id VARCHAR(36) DEFAULT NULL COMMENT 'Department leader user ID',
  
  -- Status Fields
  status TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'Status: 0=disabled, 1=enabled',
  
  -- Audit Fields
  create_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Creation timestamp',
  update_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'Update timestamp',
  create_by VARCHAR(64) DEFAULT NULL COMMENT 'Creator user ID',
  update_by VARCHAR(64) DEFAULT NULL COMMENT 'Last updater user ID',
  del_flag TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Delete flag: 0=active, 1=deleted',
  
  -- Indexes
  UNIQUE KEY uk_dept_code (dept_code),
  INDEX idx_parent_id (parent_id),
  INDEX idx_status (status),
  INDEX idx_del_flag (del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Department tree structure table';
```

## Validation Checklist

Before finalizing any table design, verify:

- [ ] Primary key is `id VARCHAR(36)` with UUID7
- [ ] All 5 audit fields present: `create_time`, `update_time`, `create_by`, `update_by`, `del_flag`
- [ ] Timestamps use `DATETIME(6)` with microsecond precision
- [ ] Logical delete field `del_flag` is `TINYINT(1) DEFAULT 0`
- [ ] **NO** physical foreign key constraints (`CONSTRAINT ... FOREIGN KEY`)
- [ ] All indexes follow naming convention: `idx_*` or `uk_*`
- [ ] Character set: `utf8mb4`, Collation: `utf8mb4_0900_ai_ci`
- [ ] Storage engine: `InnoDB`
- [ ] Table has meaningful `COMMENT`
- [ ] All columns have descriptive `COMMENT`
- [ ] Logical foreign key columns have `(references table.id)` in comment
- [ ] Composite indexes ordered optimally (equality → range)
- [ ] `del_flag` included in frequently-used composite indexes

## Common Anti-Patterns

### ❌ Underscore in Boolean Column Names
```sql
-- ❌ WRONG - redundant naming
is_active TINYINT(1),
is_deleted TINYINT(1)

-- ✅ CORRECT - clean naming
active TINYINT(1),
del_flag TINYINT(1)  -- Exception: standard audit field
```

### ❌ Using ENUM Type
```sql
-- ❌ WRONG - inflexible, migration issues
status ENUM('active', 'inactive', 'suspended')

-- ✅ CORRECT - use TINYINT with comment
status TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'Status: 0=inactive, 1=active, 2=suspended'
```

### ❌ VARCHAR Without Length Limit
```sql
-- ❌ WRONG - unclear intention
description VARCHAR(10000)

-- ✅ CORRECT - use TEXT for unlimited content
description TEXT COMMENT 'Detailed description'
```

### ❌ Mixing Snake_case and camelCase
```sql
-- ❌ WRONG - inconsistent
user_name VARCHAR(64),
createTime DATETIME(6)

-- ✅ CORRECT - consistent snake_case
user_name VARCHAR(64),
create_time DATETIME(6)
```

## MySQL 9.x Specific Features

### Generated Columns (Computed Fields)

```sql
CREATE TABLE orders (
  id VARCHAR(36) PRIMARY KEY,
  price DECIMAL(10,2) NOT NULL,
  quantity INT NOT NULL,
  
  -- Generated column (virtual, computed on read)
  total_amount DECIMAL(12,2) AS (price * quantity) VIRTUAL COMMENT 'Computed total',
  
  -- Stored generated column (computed on write)
  total_amount_stored DECIMAL(12,2) AS (price * quantity) STORED COMMENT 'Stored total',
  
  -- Index on generated column
  INDEX idx_total_amount (total_amount_stored)
);
```

### JSON Schema Validation (MySQL 9.x)

```sql
CREATE TABLE products (
  id VARCHAR(36) PRIMARY KEY,
  metadata JSON COMMENT 'Product metadata with schema validation',
  
  -- Add JSON schema constraint
  CONSTRAINT chk_metadata_schema CHECK (
    JSON_SCHEMA_VALID('{
      "type": "object",
      "properties": {
        "weight": {"type": "number"},
        "color": {"type": "string"}
      },
      "required": ["weight"]
    }', metadata)
  )
);
```

### Invisible Indexes (Testing)

```sql
-- Create invisible index (not used by optimizer)
CREATE INDEX idx_test_field (test_field) INVISIBLE;

-- Make visible after testing
ALTER TABLE table_name ALTER INDEX idx_test_field VISIBLE;
```

## Performance Optimization Tips

1. **Index Selectivity**: Index columns with high cardinality (many distinct values)
2. **Partial Indexes**: For large tables, consider filtered indexes (MySQL 9.x)
3. **Index Length Limits**: For VARCHAR indexes, use prefix: `INDEX idx_title (title(100))`
4. **Monitor Slow Queries**: Enable slow query log, use `EXPLAIN` to analyze
5. **Avoid SELECT ***: Specify needed columns for covering index benefits
6. **Batch Operations**: Use batch INSERT/UPDATE for better performance
7. **Connection Pooling**: Use HikariCP with proper sizing (see Spring Cloud framework skill)

## Integration with MyBatis-Plus

See `references/mybatis-plus-integration.md` for:
- UUID7 custom ID generator configuration
- MetaObjectHandler for audit field auto-fill
- Logical delete global configuration
- Entity annotations best practices

## Additional Resources

- `references/mysql9-new-features.md` - MySQL 9.x-specific enhancements
- `references/migration-guide.md` - Migrating from auto-increment to UUID7
- `references/sharding-patterns.md` - Database sharding strategies with UUID7
