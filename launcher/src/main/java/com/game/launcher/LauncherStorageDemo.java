package com.game.launcher;

import com.game.common.config.Config;
import com.game.common.config.ConfigException;
import com.game.common.config.PropertyResolver;
import com.game.storage.DataSourceProvider;
import com.game.storage.DbException;
import com.game.storage.JdbcTemplate;
import java.util.List;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 * 存储模块演示程序。
 *
 * <p>展示如何使用存储模块进行数据库操作，包括：
 *
 * <ul>
 *   <li>数据源配置与创建
 *   <li>基础 CRUD 操作
 *   <li>连接池管理
 *   <li>异常处理
 * </ul>
 *
 * <p>启动方式：
 *
 * <pre>
 * java com.game.launcher.LauncherStorageDemo
 * </pre>
 *
 * @author game-frame
 * @since 1.0.0
 */
public final class LauncherStorageDemo {

  private static final Logger LOGGER = Logger.getLogger(LauncherStorageDemo.class.getName());

  /** 私有构造器，防止实例化 */
  private LauncherStorageDemo() {
    // 工具类不允许实例化
  }

  /**
   * 程序主入口。
   *
   * <p>演示存储模块的完整功能。
   *
   * @param args 命令行参数（当前未使用）
   */
  public static void main(String[] args) {
    LOGGER.info("========================================");
    LOGGER.info("存储模块演示程序启动");
    LOGGER.info("========================================");

    DataSourceProvider dataSourceProvider = null;

    try {
      // 1. 加载配置
      Config config = PropertyResolver.load();
      LOGGER.info("配置加载成功");

      // 2. 创建数据源
      dataSourceProvider = new DataSourceProvider(config);
      DataSource dataSource = dataSourceProvider.get();
      LOGGER.info("数据源创建成功");

      // 3. 创建 JDBC 模板
      JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
      LOGGER.info("JDBC 模板创建成功");

      // 4. 演示数据库操作
      demonstrateDatabaseOperations(jdbcTemplate);

      LOGGER.info("========================================");
      LOGGER.info("存储模块演示程序运行完成");
      LOGGER.info("========================================");

    } catch (ConfigException e) {
      LOGGER.severe(String.format("配置加载失败: %s", e.getMessage()));
      e.printStackTrace();
      System.exit(1);
    } catch (DbException e) {
      LOGGER.severe(String.format("数据库操作失败: %s", e.getMessage()));
      e.printStackTrace();
      System.exit(1);
    } catch (Exception e) {
      LOGGER.severe(String.format("未知错误: %s", e.getMessage()));
      e.printStackTrace();
      System.exit(1);
    } finally {
      // 5. 关闭数据源
      if (dataSourceProvider != null) {
        dataSourceProvider.shutdown();
      }
    }
  }

  /**
   * 演示数据库操作。
   *
   * @param jdbcTemplate JDBC 模板
   */
  private static void demonstrateDatabaseOperations(JdbcTemplate jdbcTemplate) {
    LOGGER.info("开始演示数据库操作...");

    // 创建示例表
    createSampleTable(jdbcTemplate);

    // 插入测试数据
    insertSampleData(jdbcTemplate);

    // 查询数据
    querySampleData(jdbcTemplate);

    // 更新数据
    updateSampleData(jdbcTemplate);

    // 再次查询验证更新
    querySampleData(jdbcTemplate);

    LOGGER.info("数据库操作演示完成");
  }

  /**
   * 创建示例表。
   *
   * @param jdbcTemplate JDBC 模板
   */
  private static void createSampleTable(JdbcTemplate jdbcTemplate) {
    String createTableSql =
        """
        CREATE TABLE IF NOT EXISTS demo_users (
            id BIGINT AUTO_INCREMENT PRIMARY KEY,
            name VARCHAR(100) NOT NULL,
            email VARCHAR(200) NOT NULL,
            age INT NOT NULL,
            created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
        """;

    jdbcTemplate.update(createTableSql);
    LOGGER.info("示例表创建成功");
  }

  /**
   * 插入示例数据。
   *
   * @param jdbcTemplate JDBC 模板
   */
  private static void insertSampleData(JdbcTemplate jdbcTemplate) {
    String insertSql = "INSERT INTO demo_users (name, email, age) VALUES (?, ?, ?)";

    // 插入单条记录
    long userId1 = jdbcTemplate.updateAndReturnKey(insertSql, "张三", "zhangsan@example.com", 25);
    LOGGER.info(String.format("插入用户成功，ID: %d", userId1));

    // 批量插入
    List<Object[]> batchArgs =
        List.of(
            new Object[] {"李四", "lisi@example.com", 30},
            new Object[] {"王五", "wangwu@example.com", 28},
            new Object[] {"赵六", "zhaoliu@example.com", 35});

    int[] batchResult = jdbcTemplate.batchUpdate(insertSql, batchArgs);
    LOGGER.info(String.format("批量插入完成，影响行数: %d", batchResult.length));
  }

  /**
   * 查询示例数据。
   *
   * @param jdbcTemplate JDBC 模板
   */
  private static void querySampleData(JdbcTemplate jdbcTemplate) {
    String selectSql = "SELECT id, name, email, age FROM demo_users ORDER BY id";

    List<String> users =
        jdbcTemplate.query(
            selectSql,
            rs -> {
              return String.format(
                  "ID: %d, 姓名: %s, 邮箱: %s, 年龄: %d",
                  rs.getLong("id"), rs.getString("name"), rs.getString("email"), rs.getInt("age"));
            });

    LOGGER.info(String.format("查询到 %d 个用户:", users.size()));
    for (String user : users) {
      LOGGER.info("  " + user);
    }

    // 单条查询演示
    String singleUser =
        jdbcTemplate.queryOne(
            "SELECT name FROM demo_users WHERE age > ? LIMIT 1", rs -> rs.getString("name"), 30);

    if (singleUser != null) {
      LOGGER.info(String.format("年龄大于30的用户: %s", singleUser));
    }
  }

  /**
   * 更新示例数据。
   *
   * @param jdbcTemplate JDBC 模板
   */
  private static void updateSampleData(JdbcTemplate jdbcTemplate) {
    String updateSql = "UPDATE demo_users SET age = age + 1 WHERE age < ?";
    int updatedRows = jdbcTemplate.update(updateSql, 30);
    LOGGER.info(String.format("更新操作完成，影响 %d 行", updatedRows));
  }
}
