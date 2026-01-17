# Migration Guide: Auto-increment to UUID7

Step-by-step guide for migrating existing databases from auto-increment IDs to UUID7.

## Overview

This guide covers:
1. Pre-migration planning and risk assessment
2. Database schema migration
3. Application code updates
4. Data migration strategies
5. Rollback procedures

## Phase 1: Planning and Assessment

### 1.1 Identify Tables to Migrate

```sql
-- List all tables with auto-increment IDs
SELECT 
  TABLE_SCHEMA,
  TABLE_NAME,
  COLUMN_NAME
FROM INFORMATION_SCHEMA.COLUMNS
WHERE EXTRA LIKE '%auto_increment%'
  AND TABLE_SCHEMA = 'your_database';
```

### 1.2 Assess Foreign Key Dependencies

```sql
-- Find all foreign key relationships
SELECT 
  CONSTRAINT_NAME,
  TABLE_NAME,
  COLUMN_NAME,
  REFERENCED_TABLE_NAME,
  REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE CONSTRAINT_SCHEMA = 'your_database'
  AND REFERENCED_TABLE_NAME IS NOT NULL;
```

### 1.3 Estimate Data Volume

```sql
-- Check row counts
SELECT 
  TABLE_NAME,
  TABLE_ROWS,
  ROUND((DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024, 2) as size_mb
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'your_database'
ORDER BY TABLE_ROWS DESC;
```

## Phase 2: Create Migration Plan

### 2.1 Migration Strategy Options

**Option A: Fresh Start (New Application)**
- Create new tables with UUID7 from the beginning
- No migration needed
- Recommended for greenfield projects

**Option B: Parallel Tables (Zero Downtime)**
- Create new UUID7 tables alongside existing tables
- Dual-write to both tables during transition
- Gradually switch reads to new tables
- Recommended for production systems

**Option C: In-place Migration (Downtime Required)**
- Modify existing tables directly
- Simpler but requires downtime
- Recommended for small datasets or maintenance windows

### 2.2 Dependency Order

Migrate in this order:
1. Leaf tables (no dependencies)
2. Parent tables (referenced by others)
3. Join tables (many-to-many)

## Phase 3: Schema Migration Scripts

### 3.1 Add UUID Column to Existing Table

```sql
-- Step 1: Add new UUID column (nullable initially)
ALTER TABLE users
ADD COLUMN uuid VARCHAR(36) DEFAULT NULL COMMENT 'UUID7 identifier' AFTER id;

-- Step 2: Create unique index on UUID
ALTER TABLE users
ADD UNIQUE KEY uk_uuid (uuid);
```

### 3.2 Generate UUIDs for Existing Records

**Java Application Code**:
```java
import com.github.f4b6a3.uuid.UuidCreator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UuidMigrationService {
    
    private final JdbcTemplate jdbcTemplate;
    
    public UuidMigrationService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @Transactional
    public void generateUuidsForUsers() {
        // Get all records without UUID
        List<Long> ids = jdbcTemplate.queryForList(
            "SELECT id FROM users WHERE uuid IS NULL",
            Long.class
        );
        
        // Batch update with UUIDs
        List<Object[]> batchArgs = new ArrayList<>();
        for (Long id : ids) {
            String uuid = UuidCreator.getTimeOrderedEpoch().toString();
            batchArgs.add(new Object[]{uuid, id});
        }
        
        jdbcTemplate.batchUpdate(
            "UPDATE users SET uuid = ? WHERE id = ?",
            batchArgs
        );
    }
}
```

**Or SQL Script** (for smaller datasets):
```sql
-- Generate UUIDs using MySQL's UUID() function temporarily
-- Note: This generates UUID v1, not UUID7. For production, use application code above.
UPDATE users 
SET uuid = UUID() 
WHERE uuid IS NULL;

-- Verify all records have UUIDs
SELECT COUNT(*) as missing_uuid 
FROM users 
WHERE uuid IS NULL;
-- Should return 0
```

### 3.3 Update Foreign Key References

```sql
-- Step 1: Add UUID foreign key column
ALTER TABLE orders
ADD COLUMN user_uuid VARCHAR(36) DEFAULT NULL COMMENT 'User UUID reference' AFTER user_id;

-- Step 2: Populate UUID FK from existing relationship
UPDATE orders o
INNER JOIN users u ON o.user_id = u.id
SET o.user_uuid = u.uuid;

-- Step 3: Verify data integrity
SELECT COUNT(*) as orphaned_records
FROM orders
WHERE user_id IS NOT NULL AND user_uuid IS NULL;
-- Should return 0

-- Step 4: Add index on UUID FK
ALTER TABLE orders
ADD INDEX idx_user_uuid (user_uuid);
```

## Phase 4: Application Code Migration

### 4.1 Update Entity Classes

**Before (Auto-increment)**:
```java
@Data
@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String username;
    // ...
}
```

**After (UUID7)**:
```java
@Data
@TableName("users")
public class User {
    @TableId(type = IdType.ASSIGN_ID)  // Uses UUID7 generator
    private String uuid;
    
    @TableField("id")
    private Long legacyId;  // Keep temporarily for compatibility
    
    private String username;
    // ...
}
```

### 4.2 Update Service Layer

**Before**:
```java
public User createUser(UserCreateDTO dto) {
    User user = new User();
    user.setUsername(dto.getUsername());
    // ID auto-generated
    userMapper.insert(user);
    return user;
}
```

**After**:
```java
public User createUser(UserCreateDTO dto) {
    User user = new User();
    user.setUsername(dto.getUsername());
    // UUID auto-generated by UUID7 generator
    userMapper.insert(user);
    return user;
}
```

### 4.3 Update Controllers (API Compatibility)

**Option A: Support Both ID Formats**:
```java
@GetMapping("/{identifier}")
public RestResult<UserVO> getUser(@PathVariable String identifier) {
    User user;
    
    // Check if identifier is UUID format
    if (identifier.length() == 36 && identifier.contains("-")) {
        user = userService.getByUuid(identifier);
    } else {
        // Legacy: numeric ID
        user = userService.getByLegacyId(Long.parseLong(identifier));
    }
    
    return RestResult.success(userConverter.toVO(user));
}
```

**Option B: Versioned API**:
```java
// V1 API - Legacy (numeric IDs)
@GetMapping("/api/v1/users/{id}")
public RestResult<UserVO> getUserV1(@PathVariable Long id) {
    User user = userService.getByLegacyId(id);
    return RestResult.success(userConverter.toVO(user));
}

// V2 API - New (UUID)
@GetMapping("/api/v2/users/{uuid}")
public RestResult<UserVO> getUserV2(@PathVariable String uuid) {
    User user = userService.getByUuid(uuid);
    return RestResult.success(userConverter.toVO(user));
}
```

## Phase 5: Data Migration Execution

### 5.1 Migration Checklist

- [ ] Full database backup created
- [ ] Migration scripts tested on staging environment
- [ ] Rollback procedure documented and tested
- [ ] Monitoring alerts configured
- [ ] Downtime window scheduled (if applicable)
- [ ] Stakeholders notified

### 5.2 Parallel Table Migration (Zero Downtime)

```sql
-- Step 1: Create new table with UUID schema
CREATE TABLE users_new (
  id VARCHAR(36) PRIMARY KEY COMMENT 'UUID7 primary key',
  username VARCHAR(64) NOT NULL,
  -- ... other fields ...
  legacy_id BIGINT UNIQUE COMMENT 'Legacy auto-increment ID for reference',
  
  create_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  update_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  create_by VARCHAR(64),
  update_by VARCHAR(64),
  del_flag TINYINT(1) NOT NULL DEFAULT 0,
  
  UNIQUE KEY uk_username (username),
  INDEX idx_legacy_id (legacy_id),
  INDEX idx_del_flag (del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Step 2: Copy existing data
INSERT INTO users_new (id, username, legacy_id, create_time, update_time)
SELECT 
  UUID() as id,  -- Temporary, replace with UUID7 in application
  username,
  id as legacy_id,
  create_time,
  update_time
FROM users;

-- Step 3: Dual-write application code
@Service
public class UserServiceImpl {
    
    @Transactional
    public void createUser(User user) {
        // Write to new table
        userMapperNew.insert(user);
        
        // Also write to legacy table for compatibility
        userMapperLegacy.insert(user);
    }
}

-- Step 4: Gradually switch reads to new table
-- Monitor performance and errors

-- Step 5: Stop writing to old table once stable

-- Step 6: Rename tables
RENAME TABLE users TO users_legacy, users_new TO users;
```

### 5.3 In-place Migration (With Downtime)

```sql
-- Step 1: Backup
mysqldump -u root -p database_name > backup_before_migration.sql

-- Step 2: Stop application
-- (Ensure no active connections)

-- Step 3: Modify table structure
-- This can be slow for large tables - consider pt-online-schema-change

ALTER TABLE users
MODIFY COLUMN id VARCHAR(36) NOT NULL COMMENT 'UUID7 primary key',
DROP COLUMN auto_increment;  -- Remove auto-increment attribute

-- Step 4: Update all foreign key columns
ALTER TABLE orders
MODIFY COLUMN user_id VARCHAR(36) COMMENT 'User UUID reference';

-- Step 5: Restart application with updated code

-- Step 6: Verify
SELECT COUNT(*) FROM users WHERE LENGTH(id) != 36;  -- Should be 0
```

## Phase 6: Cleanup and Optimization

### 6.1 Remove Legacy Columns

After migration is stable (recommend waiting 2-4 weeks):

```sql
-- Step 1: Verify legacy columns not in use
-- Check application logs and query logs

-- Step 2: Remove legacy ID column from new records
ALTER TABLE users
DROP COLUMN legacy_id;

-- Step 3: Remove legacy tables
DROP TABLE IF EXISTS users_legacy;
```

### 6.2 Optimize Indexes

```sql
-- Analyze table after migration
ANALYZE TABLE users;

-- Update statistics
OPTIMIZE TABLE users;

-- Verify index usage
SELECT 
  TABLE_NAME,
  INDEX_NAME,
  SEQ_IN_INDEX,
  COLUMN_NAME,
  CARDINALITY
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = 'your_database'
  AND TABLE_NAME = 'users';
```

## Phase 7: Rollback Procedures

### 7.1 Quick Rollback (If Parallel Migration)

```sql
-- Simply rename tables back
RENAME TABLE users TO users_new, users_legacy TO users;

-- Revert application to previous version
```

### 7.2 Full Rollback (If In-place Migration)

```sql
-- Restore from backup
mysql -u root -p database_name < backup_before_migration.sql

-- Revert application code to previous version
```

## Performance Impact Assessment

### Before Migration Benchmark

```sql
-- Measure INSERT performance
SET @start = NOW(6);
INSERT INTO users (username, email) VALUES ('test_user', 'test@example.com');
SELECT TIMESTAMPDIFF(MICROSECOND, @start, NOW(6)) as insert_microseconds;

-- Measure SELECT performance
SET @start = NOW(6);
SELECT * FROM users WHERE id = 12345;
SELECT TIMESTAMPDIFF(MICROSECOND, @start, NOW(6)) as select_microseconds;

-- Index size
SELECT 
  INDEX_NAME,
  STAT_VALUE * @@innodb_page_size / 1024 / 1024 as size_mb
FROM mysql.innodb_index_stats
WHERE DATABASE_NAME = 'your_database' AND TABLE_NAME = 'users';
```

### After Migration Benchmark

Re-run the same queries and compare results.

**Expected Performance Changes**:
- INSERT: ~5-10% slower (UUID generation overhead)
- SELECT by PK: Similar performance (both are indexed)
- Index size: ~20-30% larger (VARCHAR(36) vs BIGINT)
- Range queries: Slightly slower (string comparison vs numeric)

## Common Issues and Solutions

### Issue 1: Duplicate UUID Generated

**Problem**: Application generates duplicate UUID (very rare with UUID7)

**Solution**:
```sql
-- Add unique constraint to catch duplicates
ALTER TABLE users ADD UNIQUE KEY uk_id (id);

-- In application, retry on duplicate key error
try {
    userMapper.insert(user);
} catch (DuplicateKeyException e) {
    // Regenerate UUID and retry
    user.setId(UuidCreator.getTimeOrderedEpoch().toString());
    userMapper.insert(user);
}
```

### Issue 2: Performance Degradation

**Problem**: Queries slower after migration

**Solutions**:
1. Rebuild indexes: `OPTIMIZE TABLE users;`
2. Update statistics: `ANALYZE TABLE users;`
3. Add covering indexes for frequent queries
4. Consider index prefix length for VARCHAR UUIDs

### Issue 3: Application Code References Legacy IDs

**Problem**: External systems still use numeric IDs

**Solution**: Keep `legacy_id` column for extended transition period:
```java
@Data
public class User {
    private String id;        // UUID7
    private Long legacyId;    // Keep for backward compatibility
    // ...
}
```

## Validation and Testing

### Automated Tests

```java
@Test
void testUuidMigration() {
    // Create user with UUID
    User user = new User();
    user.setUsername("test_migration");
    userService.save(user);
    
    // Verify UUID format
    assertNotNull(user.getId());
    assertEquals(36, user.getId().length());
    assertTrue(user.getId().matches("^[0-9a-f]{8}-[0-9a-f]{4}-7[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"));
    
    // Verify retrieval
    User retrieved = userService.getById(user.getId());
    assertEquals(user.getUsername(), retrieved.getUsername());
}

@Test
void testForeignKeyIntegrity() {
    // Create parent record
    User user = userService.createUser(new UserCreateDTO("parent_user"));
    
    // Create child record
    Order order = new Order();
    order.setUserId(user.getId());
    orderService.save(order);
    
    // Verify relationship
    Order retrieved = orderService.getById(order.getId());
    assertEquals(user.getId(), retrieved.getUserId());
}
```

## Timeline Recommendation

**Small Database (< 1M records)**:
- Planning: 1-2 days
- Development: 3-5 days
- Testing: 3-5 days
- Migration: 1-2 hours downtime

**Medium Database (1M-10M records)**:
- Planning: 3-5 days
- Development: 1-2 weeks
- Testing: 1-2 weeks
- Migration: Zero downtime (parallel tables)

**Large Database (> 10M records)**:
- Planning: 1-2 weeks
- Development: 2-3 weeks
- Testing: 2-3 weeks
- Migration: Zero downtime (parallel tables)
- Transition period: 4-8 weeks

## Post-Migration Monitoring

Monitor these metrics for 2-4 weeks:

1. **Application Errors**: Check for ID-related exceptions
2. **Query Performance**: Compare before/after query times
3. **Index Usage**: Ensure UUIDs are properly indexed
4. **Storage Growth**: Monitor disk usage (UUIDs use more space)
5. **Replication Lag**: If using replication, monitor lag

```sql
-- Daily monitoring query
SELECT 
  DATE(create_time) as date,
  COUNT(*) as records_created,
  COUNT(DISTINCT id) as unique_ids,
  AVG(LENGTH(id)) as avg_id_length
FROM users
WHERE create_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)
GROUP BY DATE(create_time);
```

## Conclusion

UUID7 migration is a significant undertaking but provides long-term benefits:
- ✅ Distributed system compatibility
- ✅ No ID coordination needed
- ✅ Time-ordered for better index performance
- ✅ Microservice-friendly architecture

Follow this guide carefully, test thoroughly, and maintain rollback capability throughout the migration process.
