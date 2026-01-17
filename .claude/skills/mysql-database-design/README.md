# MySQL Database Design Skill

Complete skill package for MySQL 9.x database design standards with UUID7, audit fields, and microservice-friendly patterns.

## What This Skill Provides

- **Comprehensive Design Standards**: UUID7 primary keys, audit field patterns, logical deletion
- **MySQL 9.x Optimizations**: JSON features, generated columns, performance tips
- **MyBatis-Plus Integration**: Complete configuration and usage examples
- **Migration Guides**: Step-by-step instructions for migrating from auto-increment IDs
- **Ready-to-Use Templates**: SQL scripts for common table patterns

## Skill Structure

```
mysql-database-design/
├── SKILL.md                                    # Core standards and quick reference
├── references/                                 # Detailed documentation
│   ├── mybatis-plus-integration.md            # MyBatis-Plus setup and usage
│   ├── mysql9-new-features.md                 # MySQL 9.x specific features
│   └── migration-guide.md                     # Auto-increment to UUID7 migration
└── assets/                                     # Ready-to-use templates
    └── standard-tables.sql                    # Complete table templates
```

## When to Use This Skill

This skill automatically activates when you:
- Design database schemas
- Create table DDL statements
- Validate database structures
- Configure MyBatis-Plus
- Migrate from auto-increment IDs
- Ask about UUID7, audit fields, or logical deletion

## Key Features

### 1. UUID7 Primary Keys
- Time-ordered for better index performance
- Distributed-system friendly (no coordination needed)
- Compatible with microservice architectures

### 2. Mandatory Audit Fields
All tables include:
- `id` (VARCHAR(36)) - UUID7 primary key
- `create_time` (DATETIME(6)) - Microsecond precision
- `update_time` (DATETIME(6)) - Auto-updated
- `create_by` (VARCHAR(64)) - Creator user ID
- `update_by` (VARCHAR(64)) - Last modifier user ID
- `del_flag` (TINYINT(1)) - Logical deletion: 0=active, 1=deleted

### 3. No Physical Foreign Keys
- Foreign keys enforced at application layer
- Supports database sharding and partitioning
- Microservice-friendly cross-service references

### 4. Standardized Index Naming
- Non-unique indexes: `idx_column_name`
- Unique indexes: `uk_column_name`
- Composite indexes: `idx_col1_col2` or `uk_col1_col2`

### 5. MySQL 9.x Optimizations
- `utf8mb4_0900_ai_ci` collation for best performance
- JSON schema validation
- Generated columns for computed fields
- Enhanced prepared statement caching

## Quick Start Examples

### Creating a Standard Table

```sql
CREATE TABLE sys_user (
  -- Primary Key
  id VARCHAR(36) PRIMARY KEY COMMENT 'UUID7 primary key',
  
  -- Business Fields
  username VARCHAR(64) NOT NULL COMMENT 'Login username',
  email VARCHAR(128) DEFAULT NULL COMMENT 'Email address',
  
  -- Audit Fields (REQUIRED)
  create_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  update_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  create_by VARCHAR(64) DEFAULT NULL,
  update_by VARCHAR(64) DEFAULT NULL,
  del_flag TINYINT(1) NOT NULL DEFAULT 0,
  
  -- Indexes
  UNIQUE KEY uk_username (username),
  INDEX idx_del_flag (del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

### MyBatis-Plus Entity

```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class User extends BaseEntity {
    private String username;
    private String email;
    // Audit fields inherited from BaseEntity
}
```

### Using in Service Layer

```java
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    
    public void createUser(User user) {
        save(user);  // UUID7 and audit fields auto-filled
    }
    
    public void deleteUser(String id) {
        removeById(id);  // Sets del_flag=1 (logical delete)
    }
}
```

## Validation Checklist

Before finalizing any table:
- [ ] Primary key is `id VARCHAR(36)` with UUID7
- [ ] All 5 audit fields present
- [ ] Timestamps use `DATETIME(6)`
- [ ] Logical delete field is `del_flag TINYINT(1) DEFAULT 0`
- [ ] NO physical foreign key constraints
- [ ] Indexes follow naming convention
- [ ] Character set: `utf8mb4`, Collation: `utf8mb4_0900_ai_ci`
- [ ] Storage engine: `InnoDB`
- [ ] Table and columns have meaningful comments

## Common Use Cases

### Ask Claude:
- "Create a user table following MySQL database design standards"
- "Generate DDL for a department tree structure table"
- "How do I configure MyBatis-Plus for UUID7 and audit fields?"
- "Show me how to migrate from auto-increment IDs to UUID7"
- "What's the best way to handle logical deletion with MyBatis-Plus?"
- "Create a many-to-many relationship table for users and roles"

### Claude Will:
- Read SKILL.md for core standards
- Reference detailed guides as needed
- Use template SQL from assets
- Follow all design rules automatically
- Validate against the checklist

## Benefits

1. **Consistency**: All tables follow the same pattern
2. **Traceability**: Audit fields track all changes
3. **Scalability**: UUID7 supports distributed systems
4. **Maintainability**: Clear naming conventions and standards
5. **Performance**: Optimized for MySQL 9.x
6. **Safety**: Logical deletion prevents data loss
7. **Microservice-Ready**: No physical constraints, application-layer validation

## Tips for Best Results

1. **Be Specific**: "Create a user table with username, email, and department reference"
2. **Mention Context**: "I'm building a Spring Cloud microservice with MyBatis-Plus"
3. **Ask for Validation**: "Does this table design follow the MySQL standards?"
4. **Request Examples**: "Show me the complete MyBatis-Plus entity for this table"

## License

Proprietary - See LICENSE.txt for complete terms

---

**Created**: 2025-01
**Compatible With**: MySQL 9.x, MyBatis-Plus 3.5.7+, Spring Boot 3.x
**Maintained By**: Database Architecture Team
