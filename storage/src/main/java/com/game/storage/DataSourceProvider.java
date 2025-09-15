package com.game.storage;

import com.game.common.config.Config;
import com.game.common.config.ConfigException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 数据源提供者。
 *
 * <p>基于 Config 构建 HikariDataSource，支持以下配置项：
 *
 * <ul>
 *   <li>db.driver - 数据库驱动类名（可选，H2 演示可缺省）
 *   <li>db.url - 数据库连接 URL（默认：H2 内存数据库）
 *   <li>db.username - 数据库用户名（默认：sa）
 *   <li>db.password - 数据库密码（默认：空）
 *   <li>db.pool.size - 连接池大小（默认：8）
 *   <li>db.init.enabled - 是否启用数据库初始化（默认：true）
 * </ul>
 *
 * <p>使用示例：
 *
 * <pre>{@code
 * Config config = PropertyResolver.load("prod");
 * DataSourceProvider provider = new DataSourceProvider(config);
 * DataSource dataSource = provider.get();
 * // 使用完毕后关闭连接池
 * provider.shutdown();
 * }</pre>
 *
 * @author game-frame
 * @since 1.0.0
 */
public class DataSourceProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceProvider.class);

  /** 默认数据库连接 URL */
  private static final String DEFAULT_DB_URL =
      "jdbc:h2:mem:game;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false";

  /** 默认数据库用户名 */
  private static final String DEFAULT_DB_USERNAME = "sa";

  /** 默认数据库密码 */
  private static final String DEFAULT_DB_PASSWORD = "";

  /** 默认连接池大小 */
  private static final int DEFAULT_POOL_SIZE = 8;

  /** 默认数据库初始化开关 */
  private static final boolean DEFAULT_INIT_ENABLED = true;

  private final HikariDataSource dataSource;

  /**
   * 构造数据源提供者。
   *
   * @param config 配置实例
   * @throws DbException 如果数据源创建失败
   */
  public DataSourceProvider(Config config) {
    try {
      HikariConfig hikariConfig = createHikariConfig(config);
      this.dataSource = new HikariDataSource(hikariConfig);

      LOGGER.info("数据源创建成功，连接池大小: {}", hikariConfig.getMaximumPoolSize());

      // 初始化数据库（如果启用）
      boolean initEnabled = false;
      try {
        initEnabled = config.getBoolean("db.init.enabled", DEFAULT_INIT_ENABLED);
      } catch (ConfigException e) {
        LOGGER.warn("读取数据库初始化配置失败，使用默认值: {}", DEFAULT_INIT_ENABLED, e);
        initEnabled = DEFAULT_INIT_ENABLED;
      }

      if (initEnabled) {
        initializeDatabase();
      }
    } catch (Exception e) {
      throw new DbException("数据源创建失败", e);
    }
  }

  /**
   * 获取数据源实例。
   *
   * @return DataSource 实例
   */
  public DataSource get() {
    return dataSource;
  }

  /**
   * 关闭连接池。
   *
   * <p>释放所有连接和相关资源，应用程序退出前必须调用此方法。
   */
  public void shutdown() {
    if (dataSource != null && !dataSource.isClosed()) {
      LOGGER.info("开始关闭数据库连接池...");
      dataSource.close();
      LOGGER.info("数据库连接池已关闭");
    }
  }

  /**
   * 创建 Hikari 配置。
   *
   * @param config 配置实例
   * @return HikariConfig 实例
   */
  private HikariConfig createHikariConfig(Config config) {
    HikariConfig hikariConfig = new HikariConfig();

    // 设置数据库驱动（可选）
    String driver = config.getString("db.driver");
    if (driver != null && !driver.trim().isEmpty()) {
      hikariConfig.setDriverClassName(driver);
    }

    // 设置数据库连接信息
    hikariConfig.setJdbcUrl(config.getString("db.url", DEFAULT_DB_URL));
    hikariConfig.setUsername(config.getString("db.username", DEFAULT_DB_USERNAME));
    hikariConfig.setPassword(config.getString("db.password", DEFAULT_DB_PASSWORD));

    // 设置连接池配置
    int poolSize = DEFAULT_POOL_SIZE;
    try {
      poolSize = config.getInt("db.pool.size", DEFAULT_POOL_SIZE);
    } catch (ConfigException e) {
      LOGGER.warn("读取连接池大小配置失败，使用默认值: {}", DEFAULT_POOL_SIZE, e);
    }
    hikariConfig.setMaximumPoolSize(poolSize);
    hikariConfig.setMinimumIdle(1);
    hikariConfig.setConnectionTimeout(30000); // 30 秒
    hikariConfig.setIdleTimeout(600000); // 10 分钟
    hikariConfig.setMaxLifetime(1800000); // 30 分钟

    // 设置连接池名称
    hikariConfig.setPoolName("GameDataSource");

    return hikariConfig;
  }

  /**
   * 初始化数据库。
   *
   * <p>执行基本的数据库初始化操作，验证连接可用性。
   */
  private void initializeDatabase() {
    try (Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement()) {

      // 执行简单的查询验证连接
      statement.execute("SELECT 1");
      LOGGER.info("数据库连接验证成功");

    } catch (SQLException e) {
      throw new DbException("数据库初始化失败", e);
    }
  }
}
