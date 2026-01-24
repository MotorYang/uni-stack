---
name: spring-cloud-microservice-framework
description: Constraints for generating Maven dependencies and code for a modern Spring Cloud framework (gateway, auth, system, common, config) using JDK 21 with JWT Dual-Token Authentication, MyBatis-Plus persistence, and MySQL database.
---

When adding Maven dependencies, configuring the project, or generating code, always adhere to these constraints:

## 1. Version Enforcement

- **Spring Boot**: `3.3.x` (latest stable)
- **Spring Cloud**: `2023.0.x` (Leyton)
- **Spring Cloud Alibaba**: `2023.0.1.2` (if Alibaba components are needed)
- **MyBatis-Plus**: `3.5.7` (latest stable with Spring Boot 3.x support)
- **MySQL Connector**: `9.1.0` or later (use `com.mysql:mysql-connector-j` for MySQL 9.x compatibility)
- **MapStruct**: `1.6.3` (for DTO/VO/Entity conversions)
- **JDK Version**: Always set `<java.version>21</java.version>`

## 2. Module Architecture & Boundaries

### 2.1 Microservice Naming Convention

**CRITICAL**: Microservice module names must NOT use "my" or other custom prefixes. Use direct, descriptive names:

- ✅ **gateway** (NOT mygateway)
- ✅ **auth** (NOT myauth)
- ✅ **system** (NOT mysystem)
- ✅ **common** (NOT mycommon)
- ✅ **config** (NOT myconfig)

### 2.2 Microservice Responsibilities

- **gateway**: Routing, authentication filtering, and rate limiting. **Strictly no database access.**
- **auth**: Identity verification and Token management. Fetch user details from `system` via Feign. **No direct user/role table access.**
- **system**: The core administrative base (Users, Roles, Menus, Depts). Provides foundational data for other modules. **Primary database interaction module.**
- **common**: Stateless library for shared DTOs, Utils, Global Exception Handling, and MyBatis-Plus base configurations.
- **config**: Dedicated configuration center.

### 2.3 Standard Microservice Internal Structure

**Each microservice** (gateway, auth, system, etc.) must follow this internal package structure:

```
{microservice-name}
├── src/main/java/com/{company}/{microservice-name}
│   ├── {ModuleName1Application}.java    # Spring Boot main class
│   ├── config/                           # Configuration classes
│   ├── modules/                          # Business modules
│   │   ├── user/                         # User module
│   │   │   ├── controller/
│   │   │   │   └── UserController.java
│   │   │   ├── service/
│   │   │   │   ├── IUserService.java
│   │   │   │   └── impl/
│   │   │   │       └── UserServiceImpl.java
│   │   │   ├── mapper/
│   │   │   │   └── UserMapper.java
│   │   │   ├── entity/
│   │   │   │   └── User.java
│   │   │   ├── model/
│   │   │   │   ├── dto/
│   │   │   │   │   ├── UserCreateDTO.java
│   │   │   │   │   ├── UserUpdateDTO.java
│   │   │   │   │   └── UserQueryDTO.java
│   │   │   │   └── vo/
│   │   │   │       ├── UserVO.java
│   │   │   │       └── UserDetailVO.java
│   │   │   └── converter/
│   │   │       └── UserConverter.java    # MapStruct converter
│   │   ├── role/                         # Role module
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   ├── mapper/
│   │   │   ├── entity/
│   │   │   ├── model/
│   │   │   │   ├── dto/
│   │   │   │   └── vo/
│   │   │   └── converter/
│   │   ├── menu/                         # Menu module
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   ├── mapper/
│   │   │   ├── entity/
│   │   │   ├── model/
│   │   │   │   ├── dto/
│   │   │   │   └── vo/
│   │   │   └── converter/
│   │   └── dept/                         # Department module
│   │       ├── controller/
│   │       ├── service/
│   │       ├── mapper/
│   │       ├── entity/
│   │       ├── model/
│   │       │   ├── dto/
│   │       │   └── vo/
│   │       └── converter/
│   └── common/                           # Microservice-level shared code
│       ├── exception/
│       └── util/
└── src/main/resources
    ├── mapper/                           # MyBatis XML mappers
    │   ├── user/
    │   │   └── UserMapper.xml
    │   ├── role/
    │   │   └── RoleMapper.xml
    │   ├── menu/
    │   │   └── MenuMapper.xml
    │   └── dept/
    │       └── DeptMapper.xml
    ├── application.yml
    └── bootstrap.yml (if needed)
```

### 2.4 Business Module Structure Standard

**CRITICAL**: Each business module (User, Role, Menu, Dept, etc.) MUST be completely independent and follow this exact structure:

```
{module-name}/
├── controller/              # REST API controllers
│   └── {Module}Controller.java
├── service/                 # Service layer
│   ├── I{Module}Service.java           # Service interface (extends IService<T>)
│   └── impl/
│       └── {Module}ServiceImpl.java    # Service implementation (extends ServiceImpl<M, T>)
├── mapper/                  # Data access layer
│   └── {Module}Mapper.java             # Mapper interface (extends BaseMapper<T>)
├── entity/                  # Database entities
│   └── {Module}.java                   # Entity class (extends BaseEntity)
├── model/                   # Data transfer objects
│   ├── dto/                            # Data Transfer Objects (requests)
│   │   ├── {Module}CreateDTO.java      # For create operations
│   │   ├── {Module}UpdateDTO.java      # For update operations
│   │   └── {Module}QueryDTO.java       # For query/search operations
│   └── vo/                             # View Objects (responses)
│       ├── {Module}VO.java             # Basic view object
│       └── {Module}DetailVO.java       # Detailed view object
└── converter/               # MapStruct converters
    └── {Module}Converter.java          # Entity <-> DTO/VO conversions
```

**Example for User Module:**
```
user/
├── controller/
│   └── UserController.java
├── service/
│   ├── IUserService.java
│   └── impl/
│       └── UserServiceImpl.java
├── mapper/
│   └── UserMapper.java
├── entity/
│   └── User.java
├── model/
│   ├── dto/
│   │   ├── UserCreateDTO.java
│   │   ├── UserUpdateDTO.java
│   │   └── UserQueryDTO.java
│   └── vo/
│       ├── UserVO.java
│       └── UserDetailVO.java
└── converter/
    └── UserConverter.java
```

**Module Independence Rules:**
- Each module is self-contained and independent
- No cross-module direct dependencies at the code level
- Cross-module data access ONLY via Feign client
- Modules should NOT share entities, DTOs, or VOs
- Each module has its own converter for entity/DTO/VO transformations

## 3. Authentication & Security

**Mechanism**: Use JWT-based authentication with a **Dual-Token** strategy.

- **Access Token**: Short-lived (e.g., 30m-2h) for API access.
- **Refresh Token**: Long-lived (e.g., 7d-30d) used strictly to obtain a new Access Token.
- **Storage**: Use Redis to manage Token states (e.g., logout, revocation).

## 4. MyBatis-Plus Integration

### 4.1 Dependency Configuration

In parent `pom.xml` `<dependencyManagement>`:

```xml
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
    <version>3.5.7</version>
</dependency>
```

In modules that need database access (typically `system` and other business modules):

```xml
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
</dependency>
```

### 4.2 Base Configuration in Common Module

Create in `common` module:

- `BaseEntity`: Base entity with common fields (id, createTime, updateTime, createBy, updateBy, deleted)
- `MybatisPlusConfig`: Configuration class with `PaginationInnerInterceptor` and optional `OptimisticLockerInnerInterceptor`
- Auto-fill configuration for audit fields using `MetaObjectHandler`

### 4.3 MyBatis-Plus Features to Leverage

- **CRUD Interface**: Use `BaseMapper<T>` for standard operations
- **Service Layer**: Extend `ServiceImpl<M extends BaseMapper<T>, T>` for service implementation
- **Pagination**: Use `Page<T>` for pagination queries
- **Logical Delete**: Configure `@TableLogic` for soft deletes
- **Auto-fill**: Use `@TableField(fill = FieldFill.INSERT)` for createTime/createBy
- **Lambda Wrapper**: Prefer `LambdaQueryWrapper<T>` and `LambdaUpdateWrapper<T>` for type-safe queries

### 4.4 Configuration Properties

In `application.yml`:

```yaml
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl
  global-config:
    db-config:
      id-type: ASSIGN_ID  # Snowflake ID
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
  mapper-locations: classpath*:/mapper/**/*.xml
  type-aliases-package: com.yourproject.*.entity
```

## 5. MapStruct Integration

### 5.1 Dependency Configuration

In parent `pom.xml` `<properties>`:

```xml
<mapstruct.version>1.6.3</mapstruct.version>
```

In parent `pom.xml` `<dependencyManagement>`:

```xml
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>${mapstruct.version}</version>
</dependency>
```

In modules that need entity/DTO/VO conversion:

```xml
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
</dependency>

<!-- MapStruct processor for compile-time generation -->
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct-processor</artifactId>
    <version>${mapstruct.version}</version>
    <scope>provided</scope>
</dependency>
```

### 5.2 Converter Pattern

Each business module must have a dedicated converter interface in the `converter` package:

```java
package com.company.system.modules.user.converter;

import com.company.system.modules.user.entity.User;
import com.company.system.modules.user.model.dto.UserCreateDTO;
import com.company.system.modules.user.model.dto.UserUpdateDTO;
import com.company.system.modules.user.model.vo.UserVO;
import com.company.system.modules.user.model.vo.UserDetailVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserConverter {
    
    UserConverter INSTANCE = Mappers.getMapper(UserConverter.class);
    
    /**
     * Entity to VO
     */
    UserVO toVO(User user);
    
    /**
     * Entity to Detail VO
     */
    UserDetailVO toDetailVO(User user);
    
    /**
     * Create DTO to Entity
     */
    User toEntity(UserCreateDTO dto);
    
    /**
     * Update DTO to Entity (for partial updates)
     */
    void updateEntity(UserUpdateDTO dto, @MappingTarget User user);
    
    /**
     * Entity list to VO list
     */
    List<UserVO> toVOList(List<User> users);
}
```

### 5.3 MapStruct Usage Rules

- Place all converter interfaces in the `converter/` package within each module
- Use `@Mapper(componentModel = "spring")` for Spring-managed beans
- Define conversions for: Entity ↔ CreateDTO, Entity ↔ UpdateDTO, Entity → VO
- Use `@MappingTarget` for update operations to avoid creating new instances
- Leverage `@Mapping` for custom field mappings when names differ
- Inject converters in service layer, NOT in controllers

## 6. MySQL Database Configuration

### 6.1 Dependency Configuration

In parent `pom.xml` `<dependencyManagement>`:

```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>9.1.0</version>
</dependency>
```

In modules that need database access:

```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

### 6.2 Database Connection Configuration

In `application.yml` or `application-{env}.yml`:

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://192.168.1.42:3306/frame?useUnicode=true&characterEncoding=utf8mb4&zeroDateTimeBehavior=convertToNull&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: ${DB_USERNAME:motoryang}
    password: ${DB_PASSWORD:745700Yxy@}
    # HikariCP Configuration (default in Spring Boot 3.x)
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      auto-commit: true
      idle-timeout: 600000
      max-lifetime: 1800000
      connection-timeout: 30000
      connection-test-query: SELECT 1
      # MySQL 9.x optimizations
      data-source-properties:
        cachePrepStmts: true
        prepStmtCacheSize: 250
        prepStmtCacheSqlLimit: 2048
        useServerPrepStmts: true
```

**MySQL 9.x Specific Notes**:
- MySQL 9.x has improved default configuration for prepared statements
- The `utf8mb4_0900_ai_ci` collation offers better performance than older collations
- Consider enabling `useServerPrepStmts=true` for better performance with prepared statements
- MySQL 9.x authentication defaults to `caching_sha2_password` (ensure your driver supports it)

### 6.3 MySQL Version Requirements

- **Minimum Version**: MySQL 8.0+ (MySQL 9.x fully supported)
- **Recommended Version**: MySQL 9.0+ for latest features and performance improvements
- **Character Set**: UTF8MB4 (supports full Unicode including emojis)
- **Collation**: `utf8mb4_0900_ai_ci` (MySQL 8.0+) or `utf8mb4_unicode_ci`
- **Storage Engine**: InnoDB (default and recommended for transaction support and foreign keys)
- **Note**: MySQL 9.x introduces improved performance, better JSON handling, and enhanced security features

### 6.4 Database Design Principles

- Use `BIGINT` for primary keys (compatible with MyBatis-Plus Snowflake ID)
- Use `DATETIME` or `TIMESTAMP` for temporal fields
- Always include audit fields: `create_time`, `update_time`, `create_by`, `update_by`
- Use `TINYINT(1)` for boolean flags and logical delete field (`deleted`)
- Define proper indexes for frequently queried fields
- Use foreign keys judiciously (consider application-level constraints for microservices)

## 7. Dependency Management Best Practices

- Never hardcode versions in sub-module `<dependency>` tags
- Always use `<dependencyManagement>` section with official BOMs for Spring Cloud and Spring Boot
- Group related dependencies together in the parent POM
- Use `<scope>runtime</scope>` for JDBC drivers
- Use `<scope>provided</scope>` for compile-time only dependencies (e.g., MapStruct processor)

**Example parent POM structure**:

```xml
<properties>
    <java.version>21</java.version>
    <spring-boot.version>3.3.6</spring-boot.version>
    <spring-cloud.version>2023.0.3</spring-cloud.version>
    <mybatis-plus.version>3.5.7</mybatis-plus.version>
    <mysql.version>9.1.0</mysql.version>
    <mapstruct.version>1.6.3</mapstruct.version>
</properties>

<dependencyManagement>
    <dependencies>
        <!-- Spring Boot BOM -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-dependencies</artifactId>
            <version>${spring-boot.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
        
        <!-- Spring Cloud BOM -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>${spring-cloud.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
        
        <!-- MyBatis-Plus -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
            <version>${mybatis-plus.version}</version>
        </dependency>
        
        <!-- MySQL Driver -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <version>${mysql.version}</version>
        </dependency>
        
        <!-- MapStruct -->
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct</artifactId>
            <version>${mapstruct.version}</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

## 8. JDK 21 & Performance

- Always include configuration to enable Virtual Threads: `spring.threads.virtual.enabled=true`
- Use modern Java syntax (var, text blocks, record, switch patterns) where appropriate
- Leverage pattern matching for instanceof checks
- Use record classes for DTOs and VOs to reduce boilerplate

## 9. Bootstrap & Config Note

- `bootstrap.yml` is disabled by default in Spring Boot 3.x
- Use `application.yml` for configuration
- Add `spring-cloud-starter-bootstrap` dependency explicitly if Config Client requires bootstrap phase
- Consider using Spring Cloud Config Server's native or Git backend for centralized configuration

## 10. Coding Standards

### 10.1 Inter-Service Communication

- All inter-service communication must use OpenFeign
- Define Feign clients in separate modules or packages
- Use `@FeignClient` with service name for automatic load balancing
- Implement fallback factories for circuit breaking

### 10.2 Controller Standards

- All controller responses must be wrapped in a uniform `RestResult<T>` object defined in `common`
- Use `record` for DTOs and VOs where appropriate to leverage JDK 21 features
- Implement global exception handler using `@ControllerAdvice`
- Use validation annotations (`@Valid`, `@Validated`) for request parameter validation
- Controllers should ONLY handle HTTP concerns; delegate business logic to service layer
- Use MapStruct converters to transform between DTOs/VOs and entities

**Example Controller:**
```java
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final IUserService userService;
    private final UserConverter userConverter;
    
    @PostMapping
    public RestResult<UserVO> create(@Valid @RequestBody UserCreateDTO dto) {
        User user = userConverter.toEntity(dto);
        userService.save(user);
        return RestResult.success(userConverter.toVO(user));
    }
    
    @PutMapping("/{id}")
    public RestResult<UserVO> update(@PathVariable Long id, 
                                     @Valid @RequestBody UserUpdateDTO dto) {
        User user = userService.getById(id);
        userConverter.updateEntity(dto, user);
        userService.updateById(user);
        return RestResult.success(userConverter.toVO(user));
    }
}
```

### 10.3 Service Layer Standards

- Service interfaces should extend `IService<T>` from MyBatis-Plus
- Service implementations should extend `ServiceImpl<M, T>`
- Use transactions (`@Transactional`) appropriately with proper propagation and isolation levels
- Implement business logic validation in service layer, not in controllers
- Never return entities directly; use converters to transform to VOs

**Example Service:**
```java
public interface IUserService extends IService<User> {
    UserDetailVO getUserDetail(Long id);
    Page<UserVO> pageQuery(UserQueryDTO queryDTO);
}

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    
    private final UserConverter userConverter;
    
    @Override
    public UserDetailVO getUserDetail(Long id) {
        User user = getById(id);
        return userConverter.toDetailVO(user);
    }
    
    @Override
    public Page<UserVO> pageQuery(UserQueryDTO queryDTO) {
        Page<User> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getUsername()), 
                    User::getUsername, queryDTO.getUsername());
        page(page, wrapper);
        
        Page<UserVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(userConverter.toVOList(page.getRecords()));
        return voPage;
    }
}
```

### 10.4 Data Access Layer Standards

- Mapper interfaces should extend `BaseMapper<T>` from MyBatis-Plus
- Use `@Mapper` annotation on mapper interfaces
- Prefer Lambda wrappers over XML for simple queries
- Use XML mappers for complex queries with dynamic SQL
- Place mapper XML files in `src/main/resources/mapper/{module}/` directory

**Example Mapper:**
```java
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * Complex query example (defined in XML)
     */
    List<UserDetailVO> selectUserDetailsByDept(@Param("deptId") Long deptId);
}
```

### 10.5 Entity/DTO/VO Standards

**Entity (Database layer):**
```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class User extends BaseEntity {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private String username;
    
    private String password;
    
    private String email;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
```

**DTO (Request layer):**
```java
public record UserCreateDTO(
    @NotBlank(message = "Username cannot be blank")
    String username,
    
    @NotBlank(message = "Password cannot be blank")
    @Length(min = 6, max = 20)
    String password,
    
    @Email(message = "Invalid email format")
    String email
) {}

public record UserUpdateDTO(
    String username,
    
    @Email(message = "Invalid email format")
    String email
) {}

public record UserQueryDTO(
    String username,
    String email,
    Integer current,
    Integer size
) {}
```

**VO (Response layer):**
```java
public record UserVO(
    Long id,
    String username,
    String email,
    LocalDateTime createTime
) {}

public record UserDetailVO(
    Long id,
    String username,
    String email,
    LocalDateTime createTime,
    LocalDateTime updateTime,
    List<RoleVO> roles
) {}
```

## 11. Module-Specific Database Access Rules

- **gateway**: NO database access allowed (use Redis for rate limiting if needed)
- **auth**: NO direct user table access; fetch user info from `system` via Feign; can access token/session tables if needed
- **system**: Primary module for user, role, menu, dept, and permission management
- **business modules**: Each business module manages its own domain tables; cross-module data access via Feign only

## 12. File and Resource Organization

### 12.1 Mapper XML Placement

- All MyBatis XML mappers must be in `src/main/resources/mapper/`
- Organize by module: `mapper/{module-name}/{Mapper}.xml`
- Example: `mapper/user/UserMapper.xml`, `mapper/role/RoleMapper.xml`

### 12.2 Configuration Files

- Main configuration: `src/main/resources/application.yml`
- Environment-specific: `application-{env}.yml` (dev, test, prod)
- Bootstrap configuration (if needed): `bootstrap.yml`

## 13. Common Pitfalls to Avoid

- Don't use MyBatis 3.5.x native starter with Spring Boot 3.x (use MyBatis-Plus Spring Boot 3 starter)
- Don't forget to configure timezone in MySQL connection URL
- Don't use deprecated `com.mysql:mysql-connector-java` (use `com.mysql:mysql-connector-j` for MySQL 8.x/9.x)
- Don't use MySQL Connector version below 9.1.0 with MySQL 9.x (compatibility issues may occur)
- Don't expose database entities directly in API responses (use DTO/VO conversion with MapStruct)
- Don't perform database operations in gateway module
- Don't use blocking I/O in virtual thread context unnecessarily
- Don't ignore index optimization for frequently queried fields
- Don't forget that MySQL 9.x uses `caching_sha2_password` as default authentication (ensure `allowPublicKeyRetrieval=true` in dev environments)
- Don't mix different business modules' code in the same package (keep User, Role, Menu strictly separated)
- Don't perform entity-to-VO conversion manually; always use MapStruct converters
- Don't forget to add MapStruct processor dependency with `<scope>provided</scope>`
- Don't create converters as utility classes; use `@Mapper(componentModel = "spring")` for Spring integration
- Don't place converter interfaces outside the `converter/` package
- Don't share DTOs, VOs, or entities across different modules; each module should have its own