# Storage Module - Data Access Layer

## Overview

The Storage module provides a scalable data access abstraction layer with unified database integration and resource management. It supports connection pooling and basic CRUD capabilities with H2 in-memory database as the default for local demonstrations, while production environments can switch to any JDBC data source through configuration.

## Features

- **Data Source Abstraction** - Unified data source provider with HikariCP connection pooling
- **JDBC Template** - Lightweight JDBC operations helper with query, update, and batch operations
- **Configuration Integration** - Seamlessly integrates with the existing Config system
- **H2 Database Support** - Built-in H2 in-memory database for local development and testing
- **Connection Pool Management** - Advanced connection pool configuration and lifecycle management
- **Exception Handling** - Unified database exception handling with DbException
- **Resource Management** - Automatic resource cleanup and connection management

## Architecture

### Key Components

#### DbException
- Unified runtime exception for database operations
- Wraps SQL exceptions and configuration errors
- Provides clear error messages with context

#### DataSourceProvider
- Creates and manages HikariCP data source instances
- Integrates with Config system for parameter management
- Supports database initialization and connection validation
- Handles graceful shutdown of connection pools

#### JdbcTemplate
- Lightweight JDBC operations helper
- Supports query, queryOne, update, batchUpdate operations
- Automatic resource management (connections, statements, result sets)
- Unified exception handling and logging

#### RowMapper Interface
- Functional interface for mapping ResultSet rows to Java objects
- Provides type-safe result set processing
- Supports lambda expressions for concise mapping code

## Configuration

The Storage module supports the following configuration parameters:

### Database Connection
```properties
# Database driver class name (optional for H2)
db.driver=org.h2.Driver

# Database connection URL
db.url=jdbc:h2:mem:game;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false

# Database credentials
db.username=sa
db.password=

# Connection pool size
db.pool.size=8

# Database initialization enabled
db.init.enabled=true
```

### Configuration Defaults

| Parameter | Default Value | Description |
|-----------|---------------|-------------|
| `db.driver` | (auto-detected) | Database driver class name |
| `db.url` | `jdbc:h2:mem:game;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false` | H2 in-memory database |
| `db.username` | `sa` | Database username |
| `db.password` | (empty) | Database password |
| `db.pool.size` | `8` | Maximum connection pool size |
| `db.init.enabled` | `true` | Enable database initialization |

## Usage Examples

### Basic Setup

```java
// Load configuration
Config config = PropertyResolver.load();

// Create data source provider
DataSourceProvider dataSourceProvider = new DataSourceProvider(config);
DataSource dataSource = dataSourceProvider.get();

// Create JDBC template
JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

// Don't forget to shutdown
dataSourceProvider.shutdown();
```

### Query Operations

```java
// Query multiple records
List<User> users = jdbcTemplate.query(
    "SELECT * FROM users WHERE age > ?",
    rs -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setAge(rs.getInt("age"));
        return user;
    },
    18
);

// Query single record
User user = jdbcTemplate.queryOne(
    "SELECT * FROM users WHERE id = ?",
    rs -> {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setName(rs.getString("name"));
        return u;
    },
    1L
);
```

### Update Operations

```java
// Insert with generated key
long userId = jdbcTemplate.updateAndReturnKey(
    "INSERT INTO users (name, email, age) VALUES (?, ?, ?)",
    "张三", "zhangsan@example.com", 25
);

// Update records
int updatedRows = jdbcTemplate.update(
    "UPDATE users SET age = ? WHERE id = ?",
    26, userId
);

// Batch operations
List<Object[]> batchArgs = List.of(
    new Object[]{"李四", "lisi@example.com", 30},
    new Object[]{"王五", "wangwu@example.com", 28}
);

int[] batchResults = jdbcTemplate.batchUpdate(
    "INSERT INTO users (name, email, age) VALUES (?, ?, ?)",
    batchArgs
);
```

### Custom Row Mappers

```java
// Simple mapper for single column
List<String> names = jdbcTemplate.query(
    "SELECT name FROM users",
    rs -> rs.getString("name")
);

// Complex mapper for multiple columns
List<UserDto> userDtos = jdbcTemplate.query(
    "SELECT u.*, p.phone FROM users u LEFT JOIN profiles p ON u.id = p.user_id",
    rs -> {
        UserDto dto = new UserDto();
        dto.setId(rs.getLong("id"));
        dto.setName(rs.getString("name"));
        dto.setEmail(rs.getString("email"));
        dto.setPhone(rs.getString("phone"));
        return dto;
    }
);
```

## Database Support

### H2 Database (Default)
- In-memory database for local development
- MySQL compatibility mode
- Automatic schema creation
- No external dependencies required

### Production Databases
The storage module can be configured to work with any JDBC-compatible database:

```properties
# PostgreSQL Example
db.driver=org.postgresql.Driver
db.url=jdbc:postgresql://localhost:5432/gamedb
db.username=gameuser
db.password=gamepass

# MySQL Example
db.driver=com.mysql.cj.jdbc.Driver
db.url=jdbc:mysql://localhost:3306/gamedb?useSSL=false&serverTimezone=UTC
db.username=gameuser
db.password=gamepass
```

## Integration Points

The Storage module integrates seamlessly with the existing architecture:

1. **Config System** - Uses Config interface for all configuration parameters
2. **Logging System** - Uses SLF4J for consistent logging with other modules
3. **Exception Handling** - Follows project-wide exception handling patterns
4. **Resource Management** - Implements proper lifecycle management

## Demo Application

The module includes a comprehensive demo application that demonstrates:

- Data source configuration and creation
- Connection pool management
- Basic CRUD operations (Create, Read, Update, Delete)
- Batch operations
- Error handling
- Resource cleanup

Run the demo with:
```bash
java com.game.launcher.LauncherStorageDemo
```

## Module Structure

```
storage/
├── pom.xml
└── src/main/java/com/game/storage/
    ├── DbException.java           # Database exception handling
    ├── DataSourceProvider.java    # Data source configuration and management
    ├── JdbcTemplate.java         # JDBC operations helper
    └── RowMapper.java            # Result set mapping interface
```

## Dependencies

The Storage module depends on:

- **common** - For Config system integration
- **slf4j-api** - For logging support
- **HikariCP** - High-performance connection pool
- **H2 Database** - In-memory database for local development

## Performance Considerations

- **Connection Pooling** - HikariCP provides high-performance connection pooling
- **Resource Management** - Automatic cleanup of connections, statements, and result sets
- **Batch Operations** - Efficient batch processing for bulk operations
- **Prepared Statements** - Uses prepared statements for better performance and security

## Security Features

- **SQL Injection Protection** - Uses prepared statements with parameter binding
- **Resource Leak Prevention** - Automatic resource cleanup with try-with-resources
- **Connection Pool Security** - Secure connection pool configuration with timeouts
- **Error Information Control** - Controlled error message exposure

## Best Practices

1. **Always use try-with-resources or proper shutdown**
2. **Use prepared statements with parameter binding**
3. **Implement proper error handling**
4. **Configure appropriate connection pool sizes**
5. **Use batch operations for bulk processing**
6. **Test with production-like data volumes**

## Future Enhancements

- **Transaction Management** - Declarative and programmatic transaction support
- **Database Migration** - Schema versioning and migration tools
- **Query Builder** - Fluent API for dynamic query construction
- **ORM Integration** - Optional ORM layer for complex object mapping
- **Monitoring Integration** - Connection pool and query performance metrics