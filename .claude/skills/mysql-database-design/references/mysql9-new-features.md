# MySQL 9.x New Features and Optimizations

Key features in MySQL 9.x relevant to microservice database design.

## Performance Improvements

### 1. Enhanced Prepared Statement Caching

MySQL 9.x significantly improves prepared statement performance:

```yaml
# application.yml - HikariCP datasource configuration
spring:
  datasource:
    hikari:
      data-source-properties:
        cachePrepStmts: true
        prepStmtCacheSize: 250
        prepStmtCacheSqlLimit: 2048
        useServerPrepStmts: true  # MySQL 9.x optimized
```

### 2. Better Collation Performance

The `utf8mb4_0900_ai_ci` collation is optimized in MySQL 9.x:
- Faster string comparisons
- Improved index performance
- Better memory utilization

```sql
-- Use this collation for all new tables
DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
```

### 3. JSON Performance Enhancements

MySQL 9.x has significantly faster JSON operations:

```sql
CREATE TABLE products (
  id VARCHAR(36) PRIMARY KEY,
  attributes JSON,
  
  -- MySQL 9.x: Index on JSON field with generated column
  attr_category VARCHAR(50) AS (attributes->>'$.category') STORED,
  INDEX idx_attr_category (attr_category)
);

-- Fast JSON queries
SELECT * FROM products WHERE attributes->>'$.category' = 'electronics';
```

## Security Enhancements

### 1. `caching_sha2_password` as Default

MySQL 9.x defaults to `caching_sha2_password` authentication:

**Connection String Requirements**:
```
jdbc:mysql://host:3306/db?allowPublicKeyRetrieval=true&useSSL=true
```

**For Development** (less secure):
```
jdbc:mysql://host:3306/db?allowPublicKeyRetrieval=true&useSSL=false
```

**For Production** (secure):
```
jdbc:mysql://host:3306/db?useSSL=true&requireSSL=true&verifyServerCertificate=true
```

### 2. Password Validation Plugin

```sql
-- Install password validation plugin
INSTALL PLUGIN validate_password SONAME 'validate_password.so';

-- Configure password policy
SET GLOBAL validate_password.policy = MEDIUM;
SET GLOBAL validate_password.length = 12;
SET GLOBAL validate_password.mixed_case_count = 1;
SET GLOBAL validate_password.number_count = 1;
SET GLOBAL validate_password.special_char_count = 1;
```

## Advanced JSON Features

### 1. JSON Schema Validation

```sql
CREATE TABLE user_settings (
  id VARCHAR(36) PRIMARY KEY,
  user_id VARCHAR(36) NOT NULL,
  settings JSON,
  
  -- Validate JSON structure
  CONSTRAINT chk_settings_schema CHECK (
    JSON_SCHEMA_VALID('{
      "type": "object",
      "properties": {
        "theme": {"type": "string", "enum": ["light", "dark"]},
        "language": {"type": "string"},
        "notifications": {
          "type": "object",
          "properties": {
            "email": {"type": "boolean"},
            "push": {"type": "boolean"}
          }
        }
      },
      "required": ["theme", "language"]
    }', settings)
  )
);

-- Valid insert
INSERT INTO user_settings VALUES (
  UUID(),
  'user123',
  '{"theme":"dark","language":"en","notifications":{"email":true,"push":false}}'
);

-- Invalid insert - will fail
INSERT INTO user_settings VALUES (
  UUID(),
  'user123',
  '{"theme":"invalid","language":"en"}'  -- theme not in enum
);
```

### 2. JSON Table Function

```sql
-- Extract JSON array as table
SELECT jt.* 
FROM products,
JSON_TABLE(
  attributes,
  '$.tags[*]' COLUMNS(
    tag_name VARCHAR(50) PATH '$'
  )
) AS jt;
```

### 3. Multi-valued Indexes

```sql
CREATE TABLE products (
  id VARCHAR(36) PRIMARY KEY,
  tags JSON,
  
  -- Index on JSON array elements (MySQL 9.x)
  INDEX idx_tags ((CAST(tags->'$[*]' AS CHAR(50) ARRAY)))
);

-- Fast query on array values
SELECT * FROM products WHERE 'electronics' MEMBER OF (tags->'$');
```

## Generated Columns

### Virtual Generated Columns

Computed on read, not stored:

```sql
CREATE TABLE orders (
  id VARCHAR(36) PRIMARY KEY,
  price DECIMAL(10,2) NOT NULL,
  quantity INT NOT NULL,
  discount_rate DECIMAL(4,2) DEFAULT 0,
  
  -- Virtual: computed on SELECT
  subtotal DECIMAL(12,2) AS (price * quantity) VIRTUAL,
  discount_amount DECIMAL(12,2) AS (price * quantity * discount_rate / 100) VIRTUAL,
  total DECIMAL(12,2) AS (price * quantity * (1 - discount_rate / 100)) VIRTUAL
);

-- Query uses generated column
SELECT id, subtotal, discount_amount, total FROM orders WHERE total > 1000;
```

### Stored Generated Columns

Computed on write, stored physically:

```sql
CREATE TABLE users (
  id VARCHAR(36) PRIMARY KEY,
  first_name VARCHAR(50),
  last_name VARCHAR(50),
  
  -- Stored: computed on INSERT/UPDATE, physically stored
  full_name VARCHAR(101) AS (CONCAT(first_name, ' ', last_name)) STORED,
  
  INDEX idx_full_name (full_name)  -- Can index stored columns
);

-- Fast search on generated column
SELECT * FROM users WHERE full_name LIKE 'John%';
```

## Invisible Indexes

Test index performance without dropping:

```sql
-- Create invisible index (not used by optimizer)
CREATE INDEX idx_email ON users(email) INVISIBLE;

-- Test queries (won't use idx_email)
EXPLAIN SELECT * FROM users WHERE email = 'test@example.com';

-- Make visible if helpful
ALTER TABLE users ALTER INDEX idx_email VISIBLE;

-- Or drop if not helpful
DROP INDEX idx_email ON users;
```

## Window Functions Enhancements

MySQL 9.x optimizes window functions:

```sql
-- Ranking within department
SELECT 
  id,
  username,
  dept_id,
  salary,
  ROW_NUMBER() OVER (PARTITION BY dept_id ORDER BY salary DESC) as dept_rank,
  RANK() OVER (ORDER BY salary DESC) as company_rank
FROM users
WHERE del_flag = 0;

-- Running totals
SELECT 
  order_date,
  amount,
  SUM(amount) OVER (ORDER BY order_date ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) as running_total
FROM orders;
```

## Histogram Statistics

Improve query optimizer decisions:

```sql
-- Analyze table and create histogram
ANALYZE TABLE users UPDATE HISTOGRAM ON username, email;

-- View histogram info
SELECT * FROM INFORMATION_SCHEMA.COLUMN_STATISTICS 
WHERE SCHEMA_NAME = 'your_db' AND TABLE_NAME = 'users';

-- Drop histogram
ANALYZE TABLE users DROP HISTOGRAM ON username;
```

## Descending Indexes

MySQL 9.x fully supports descending indexes:

```sql
-- Optimize ORDER BY with mixed sort directions
CREATE INDEX idx_create_time_desc ON users(dept_id ASC, create_time DESC);

-- Efficiently uses index
SELECT * FROM users 
WHERE dept_id = 'dept123' 
ORDER BY create_time DESC 
LIMIT 10;
```

## Common Table Expressions (CTE) Optimizations

MySQL 9.x better optimizes CTEs:

```sql
-- Recursive CTE for tree structure
WITH RECURSIVE dept_tree AS (
  -- Anchor member: root departments
  SELECT id, parent_id, dept_name, 0 as level
  FROM sys_dept
  WHERE parent_id IS NULL AND del_flag = 0
  
  UNION ALL
  
  -- Recursive member: child departments
  SELECT d.id, d.parent_id, d.dept_name, dt.level + 1
  FROM sys_dept d
  INNER JOIN dept_tree dt ON d.parent_id = dt.id
  WHERE d.del_flag = 0
)
SELECT * FROM dept_tree ORDER BY level, dept_name;

-- Non-recursive CTE (materialized in MySQL 9.x)
WITH active_users AS (
  SELECT * FROM users WHERE status = 1 AND del_flag = 0
)
SELECT 
  d.dept_name,
  COUNT(u.id) as user_count
FROM sys_dept d
LEFT JOIN active_users u ON d.id = u.dept_id
GROUP BY d.id, d.dept_name;
```

## Configuration Recommendations

### MySQL Server Configuration (`my.cnf`)

```ini
[mysqld]
# Character set
character-set-server = utf8mb4
collation-server = utf8mb4_0900_ai_ci

# InnoDB settings
innodb_buffer_pool_size = 4G  # 60-80% of available RAM
innodb_log_file_size = 512M
innodb_flush_log_at_trx_commit = 2  # Performance vs durability tradeoff
innodb_flush_method = O_DIRECT

# Query cache (disabled in MySQL 9.x, use application-level caching)
query_cache_type = 0
query_cache_size = 0

# Connection limits
max_connections = 500
max_allowed_packet = 64M

# Performance schema
performance_schema = ON

# Slow query log
slow_query_log = 1
slow_query_log_file = /var/log/mysql/slow-query.log
long_query_time = 1

# Binary logging (for replication/backup)
log_bin = mysql-bin
binlog_format = ROW
binlog_expire_logs_seconds = 604800  # 7 days
```

### HikariCP Connection Pool (Recommended)

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/db?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    
    hikari:
      # Pool sizing
      minimum-idle: 5
      maximum-pool-size: 20
      
      # Connection lifecycle
      max-lifetime: 1800000      # 30 minutes
      idle-timeout: 600000       # 10 minutes
      connection-timeout: 30000  # 30 seconds
      
      # Connection testing
      connection-test-query: SELECT 1
      
      # MySQL 9.x optimizations
      data-source-properties:
        cachePrepStmts: true
        prepStmtCacheSize: 250
        prepStmtCacheSqlLimit: 2048
        useServerPrepStmts: true
        rewriteBatchedStatements: true  # Batch insert optimization
```

## Monitoring and Troubleshooting

### Performance Schema Queries

```sql
-- Find slow queries
SELECT 
  DIGEST_TEXT,
  COUNT_STAR,
  AVG_TIMER_WAIT/1000000000000 as avg_seconds,
  MAX_TIMER_WAIT/1000000000000 as max_seconds
FROM performance_schema.events_statements_summary_by_digest
ORDER BY AVG_TIMER_WAIT DESC
LIMIT 10;

-- Table I/O statistics
SELECT 
  OBJECT_NAME,
  COUNT_READ,
  COUNT_WRITE,
  SUM_TIMER_READ/1000000000000 as read_seconds,
  SUM_TIMER_WRITE/1000000000000 as write_seconds
FROM performance_schema.table_io_waits_summary_by_table
WHERE OBJECT_SCHEMA = 'your_database'
ORDER BY SUM_TIMER_WAIT DESC;
```

### Index Usage Analysis

```sql
-- Find unused indexes
SELECT 
  OBJECT_SCHEMA,
  OBJECT_NAME,
  INDEX_NAME
FROM performance_schema.table_io_waits_summary_by_index_usage
WHERE INDEX_NAME IS NOT NULL
  AND COUNT_STAR = 0
  AND OBJECT_SCHEMA NOT IN ('mysql', 'performance_schema', 'information_schema')
ORDER BY OBJECT_SCHEMA, OBJECT_NAME;
```

## Best Practices Summary

1. **Use `utf8mb4_0900_ai_ci`** for all new tables
2. **Enable prepared statement caching** in connection pool
3. **Leverage JSON validation** for schema enforcement
4. **Use stored generated columns** for frequently-queried computed fields
5. **Create histograms** on columns with skewed data distribution
6. **Test indexes with INVISIBLE** before committing
7. **Monitor with Performance Schema** regularly
8. **Configure connection pool** appropriately for workload
9. **Use `caching_sha2_password`** with proper SSL configuration
10. **Optimize CTEs** for complex queries instead of subqueries
