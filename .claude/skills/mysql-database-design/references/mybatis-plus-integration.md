# MyBatis-Plus Integration with UUID7 and Audit Fields

Complete guide for integrating MySQL database design standards with MyBatis-Plus.

## UUID7 Custom ID Generator

### Maven Dependency

```xml
<dependency>
    <groupId>com.github.f4b6a3</groupId>
    <artifactId>uuid-creator</artifactId>
    <version>5.3.7</version>
</dependency>
```

### Custom ID Generator Implementation

```java
package com.yourproject.common.config;

import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.github.f4b6a3.uuid.UuidCreator;
import org.springframework.stereotype.Component;

@Component
public class Uuid7Generator implements IdentifierGenerator {
    
    @Override
    public String nextId(Object entity) {
        return UuidCreator.getTimeOrderedEpoch().toString();
    }
    
    @Override
    public Number nextUUID(Object entity) {
        // Not used for String IDs
        throw new UnsupportedOperationException("UUID7 generator returns String, not Number");
    }
}
```

### MyBatis-Plus Configuration

```java
package com.yourproject.common.config;

import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisPlusConfig {
    
    @Bean
    public GlobalConfig globalConfig(Uuid7Generator uuid7Generator) {
        GlobalConfig globalConfig = new GlobalConfig();
        
        // Set UUID7 as default ID generator
        globalConfig.setIdentifierGenerator(uuid7Generator);
        
        // Database config
        GlobalConfig.DbConfig dbConfig = new GlobalConfig.DbConfig();
        dbConfig.setLogicDeleteField("delFlag");  // Logical delete field name
        dbConfig.setLogicDeleteValue("1");        // Deleted value
        dbConfig.setLogicNotDeleteValue("0");     // Not deleted value
        globalConfig.setDbConfig(dbConfig);
        
        return globalConfig;
    }
    
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // Pagination plugin
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        
        // Optimistic locking plugin (if needed)
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        
        return interceptor;
    }
}
```

## Auto-fill Configuration (Audit Fields)

### MetaObjectHandler Implementation

```java
package com.yourproject.common.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AuditMetaObjectHandler implements MetaObjectHandler {
    
    @Override
    public void insertFill(MetaObject metaObject) {
        // Auto-fill create_time
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        
        // Auto-fill update_time
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        
        // Auto-fill create_by (get from security context)
        String currentUserId = getCurrentUserId();
        this.strictInsertFill(metaObject, "createBy", String.class, currentUserId);
        
        // Auto-fill update_by
        this.strictInsertFill(metaObject, "updateBy", String.class, currentUserId);
    }
    
    @Override
    public void updateFill(MetaObject metaObject) {
        // Auto-fill update_time
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        
        // Auto-fill update_by
        String currentUserId = getCurrentUserId();
        this.strictUpdateFill(metaObject, "updateBy", String.class, currentUserId);
    }
    
    /**
     * Get current user ID from security context
     * Implement based on your authentication mechanism (JWT, Spring Security, etc.)
     */
    private String getCurrentUserId() {
        // Example implementation - adjust based on your auth system
        // return SecurityContextHolder.getContext().getAuthentication().getName();
        
        // For development/testing
        return "system";
    }
}
```

## Base Entity Class

```java
package com.yourproject.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public abstract class BaseEntity implements Serializable {
    
    /**
     * Primary key - UUID7
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    
    /**
     * Creation timestamp
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * Last update timestamp
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * Creator user ID
     */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;
    
    /**
     * Last updater user ID
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;
    
    /**
     * Logical delete flag: 0=active, 1=deleted
     */
    @TableLogic
    @TableField(value = "del_flag")
    private Integer delFlag;
}
```

## Entity Class Example

```java
package com.yourproject.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.yourproject.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class User extends BaseEntity {
    
    /**
     * Login username
     */
    private String username;
    
    /**
     * Encrypted password
     */
    private String password;
    
    /**
     * Display name
     */
    private String nickname;
    
    /**
     * Email address
     */
    private String email;
    
    /**
     * Phone number
     */
    private String phone;
    
    /**
     * Avatar URL
     */
    private String avatar;
    
    /**
     * Status: 0=disabled, 1=enabled
     */
    private Integer status;
    
    /**
     * Department ID (logical FK to sys_dept.id)
     */
    private String deptId;
}
```

## Application Configuration

```yaml
# application.yml
mybatis-plus:
  configuration:
    # Auto camelCase mapping
    map-underscore-to-camel-case: true
    # Log SQL statements
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl
  
  global-config:
    # Banner
    banner: false
    
    db-config:
      # Use custom UUID7 generator
      id-type: ASSIGN_ID
      
      # Logical delete configuration
      logic-delete-field: delFlag
      logic-delete-value: 1
      logic-not-delete-value: 0
  
  # Mapper XML locations
  mapper-locations: classpath*:/mapper/**/*.xml
  
  # Entity package for type aliases
  type-aliases-package: com.yourproject.*.entity
```

## Usage Examples

### Simple CRUD

```java
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    
    // CREATE - UUID7 and audit fields auto-filled
    public void createUser(User user) {
        save(user);  // ID generated automatically
    }
    
    // READ - Logical delete handled automatically
    public User getUserById(String id) {
        return getById(id);  // Only returns if del_flag=0
    }
    
    // UPDATE - update_time and update_by auto-filled
    public void updateUser(User user) {
        updateById(user);  // update_time auto-updated
    }
    
    // DELETE - Logical delete (sets del_flag=1)
    public void deleteUser(String id) {
        removeById(id);  // Sets del_flag=1, doesn't physically delete
    }
}
```

### Query with LambdaWrapper

```java
// Query active users from specific department
List<User> users = lambdaQuery()
    .eq(User::getDeptId, deptId)
    .eq(User::getStatus, 1)
    // del_flag automatically added to WHERE clause
    .list();

// Time range query
List<User> recentUsers = lambdaQuery()
    .ge(User::getCreateTime, LocalDateTime.now().minusDays(7))
    .orderByDesc(User::getCreateTime)
    .list();
```

### Pagination

```java
// Page query
Page<User> page = new Page<>(current, size);
LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
wrapper.like(StringUtils.hasText(keyword), User::getUsername, keyword)
       .orderByDesc(User::getCreateTime);

page(page, wrapper);
```

### Batch Operations

```java
// Batch insert - IDs auto-generated
List<User> users = Arrays.asList(user1, user2, user3);
saveBatch(users);

// Batch logical delete
List<String> ids = Arrays.asList(id1, id2, id3);
removeByIds(ids);  // Sets del_flag=1 for all
```

## Testing Configuration

```java
@SpringBootTest
class UserServiceTest {
    
    @Autowired
    private IUserService userService;
    
    @Test
    void testUuid7Generation() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("encrypted");
        
        userService.save(user);
        
        // Verify UUID7 format (36 characters including hyphens)
        assertNotNull(user.getId());
        assertEquals(36, user.getId().length());
        assertTrue(user.getId().contains("-"));
        
        // Verify audit fields auto-filled
        assertNotNull(user.getCreateTime());
        assertNotNull(user.getUpdateTime());
        assertNotNull(user.getCreateBy());
        assertEquals(0, user.getDelFlag());
    }
    
    @Test
    void testLogicalDelete() {
        User user = createTestUser();
        String userId = user.getId();
        
        // Logical delete
        userService.removeById(userId);
        
        // Verify cannot retrieve (del_flag=1)
        User deletedUser = userService.getById(userId);
        assertNull(deletedUser);
        
        // Verify still in database (physical record exists)
        User physicalRecord = userService.getBaseMapper()
            .selectById(userId);  // Bypasses logical delete
        assertNotNull(physicalRecord);
        assertEquals(1, physicalRecord.getDelFlag());
    }
}
```

## Common Pitfalls

### ❌ Wrong: Manually Setting ID
```java
User user = new User();
user.setId(UUID.randomUUID().toString());  // ❌ Don't do this
userService.save(user);
```

### ✅ Correct: Let MyBatis-Plus Generate ID
```java
User user = new User();
// ID will be auto-generated using UUID7
userService.save(user);
```

### ❌ Wrong: Manually Setting Audit Fields
```java
User user = new User();
user.setCreateTime(LocalDateTime.now());  // ❌ Redundant
user.setCreateBy(currentUserId);          // ❌ Redundant
```

### ✅ Correct: Let MetaObjectHandler Fill Audit Fields
```java
User user = new User();
// Audit fields auto-filled by MetaObjectHandler
userService.save(user);
```

### ❌ Wrong: Physical Delete Query
```java
// ❌ This bypasses logical delete
userMapper.delete(new LambdaQueryWrapper<User>().eq(User::getId, id));
```

### ✅ Correct: Use Service Layer Methods
```java
// ✅ Properly handles logical delete
userService.removeById(id);
```

## Performance Considerations

1. **UUID7 vs Auto-increment**: UUID7 performs ~95% as well as auto-increment in benchmarks
2. **Index on del_flag**: Always index for query performance with logical deletes
3. **Composite Indexes**: Include `del_flag` as first column in frequently-used indexes
4. **Batch Operations**: Use `saveBatch()` instead of loops for better performance
5. **Pagination**: Always use `Page<T>` for large result sets

## Migration from Auto-increment to UUID7

See `references/migration-guide.md` for step-by-step instructions on migrating existing databases.
