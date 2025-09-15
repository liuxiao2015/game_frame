package com.game.launcher;

import com.game.common.config.Config;
import com.game.common.config.ConfigException;
import com.game.common.config.PropertyResolver;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 配置中心演示启动器。
 *
 * <p>此类演示配置中心的多环境分层加载与覆盖功能，包括：
 *
 * <ul>
 *   <li>环境自动检测（系统属性/环境变量）
 *   <li>配置文件分层加载（default → env → system props → env vars）
 *   <li>环境变量映射（GAME_* → 配置键）
 *   <li>占位符解析（${key:default}）
 * </ul>
 *
 * <p>运行方式示例：
 *
 * <pre>
 * # 默认环境（dev）
 * java LauncherConfigDemo
 *
 * # 指定环境
 * java -Denv=prod LauncherConfigDemo
 *
 * # 环境变量覆盖
 * GAME_SERVER_PORT=9000 java LauncherConfigDemo
 *
 * # 系统属性覆盖
 * java -Dserver.port=9001 LauncherConfigDemo
 * </pre>
 *
 * @author game-frame
 * @since 1.0.0
 */
public final class LauncherConfigDemo {

  private static final Logger LOGGER = Logger.getLogger(LauncherConfigDemo.class.getName());

  /** 私有构造器，防止实例化 */
  private LauncherConfigDemo() {
    // 工具类不允许实例化
  }

  /**
   * 程序主入口。
   *
   * <p>演示配置中心的完整功能，包括环境检测、配置加载和覆盖优先级验证。
   *
   * @param args 命令行参数（当前未使用）
   */
  public static void main(String[] args) {
    LOGGER.info("========================================");
    LOGGER.info("配置中心演示程序启动");
    LOGGER.info("========================================");

    try {
      // 1. 加载配置
      LOGGER.info("开始加载配置...");
      Config config = PropertyResolver.load();
      LOGGER.info("配置加载完成");

      // 2. 显示当前环境信息
      displayEnvironmentInfo(config);

      // 3. 显示核心配置项
      displayCoreConfiguration(config);

      // 4. 演示覆盖优先级
      demonstrateOverridePriority();

      // 5. 演示环境变量映射
      demonstrateEnvironmentVariableMapping();

      LOGGER.info("========================================");
      LOGGER.info("配置中心演示程序运行完成");
      LOGGER.info("========================================");

    } catch (ConfigException e) {
      LOGGER.severe(String.format("配置加载失败: %s", e.getMessage()));
      e.printStackTrace();
      System.exit(1);
    } catch (Exception e) {
      LOGGER.severe(String.format("程序运行发生未知错误: %s", e.getMessage()));
      e.printStackTrace();
      System.exit(1);
    }
  }

  /**
   * 显示当前环境信息。
   *
   * @param config 配置实例
   */
  private static void displayEnvironmentInfo(Config config) {
    LOGGER.info("========== 环境信息 ==========");

    // 检测环境来源
    String envFromSysProp = System.getProperty("env");
    String envFromEnvVar = System.getenv("GAME_ENV");

    if (envFromSysProp != null) {
      LOGGER.info(String.format("环境来源: JVM 系统属性 (env=%s)", envFromSysProp));
    } else if (envFromEnvVar != null) {
      LOGGER.info(String.format("环境来源: 环境变量 (GAME_ENV=%s)", envFromEnvVar));
    } else {
      LOGGER.info("环境来源: 默认值 (dev)");
    }

    // 通过接口方法获取当前环境
    String currentEnv = config.getEnvironment();
    LOGGER.info(String.format("当前环境: %s", currentEnv));
  }

  /**
   * 显示核心配置项。
   *
   * @param config 配置实例
   * @throws ConfigException 如果配置访问失败
   */
  private static void displayCoreConfiguration(Config config) throws ConfigException {
    LOGGER.info("========== 核心配置 ==========");

    // 应用信息
    String appName = config.getString("app.name", "unknown");
    String appVersion = config.getString("app.version", "unknown");
    LOGGER.info(String.format("应用名称: %s", appName));
    LOGGER.info(String.format("应用版本: %s", appVersion));

    // 服务器配置
    int serverPort = config.getInt("server.port", 8080);
    String serverHost = config.getString("server.host", "localhost");
    LOGGER.info(String.format("服务器地址: %s:%d", serverHost, serverPort));

    // 日志配置
    String rootLogLevel = config.getString("logging.level.root", "INFO");
    String gameLogLevel = config.getString("logging.level.com.game", "INFO");
    LOGGER.info(String.format("日志级别: root=%s, game=%s", rootLogLevel, gameLogLevel));

    // 占位符解析示例
    String welcomeMessage = config.getString("welcome.message", "Welcome");
    LOGGER.info(String.format("欢迎信息: %s", welcomeMessage));

    // 可选配置示例
    boolean debugEnabled = config.getBoolean("debug.enabled", false);
    boolean cacheEnabled = config.getBoolean("cache.enabled", true);
    LOGGER.info(String.format("调试模式: %s", debugEnabled ? "启用" : "禁用"));
    LOGGER.info(String.format("缓存功能: %s", cacheEnabled ? "启用" : "禁用"));
  }

  /** 演示配置覆盖优先级。 */
  private static void demonstrateOverridePriority() {
    LOGGER.info("========== 覆盖优先级演示 ==========");
    LOGGER.info("配置覆盖顺序：默认配置 < 环境配置 < 系统属性 < 环境变量");

    // 显示 server.port 的覆盖情况
    String systemPort = System.getProperty("server.port");
    String envPort = System.getenv("GAME_SERVER_PORT");

    if (envPort != null) {
      LOGGER.info(String.format("环境变量覆盖: GAME_SERVER_PORT=%s (最高优先级)", envPort));
    } else if (systemPort != null) {
      LOGGER.info(String.format("系统属性覆盖: -Dserver.port=%s", systemPort));
    } else {
      LOGGER.info("使用配置文件中的端口设置");
    }
  }

  /** 演示环境变量映射规则。 */
  private static void demonstrateEnvironmentVariableMapping() {
    LOGGER.info("========== 环境变量映射演示 ==========");
    LOGGER.info("映射规则: GAME_* → 去前缀 → 小写 → 下划线转点号");

    Map<String, String> envVars = System.getenv();
    boolean hasGameEnvVars = false;

    for (Map.Entry<String, String> entry : envVars.entrySet()) {
      String envKey = entry.getKey();
      String envValue = entry.getValue();

      if (envKey.startsWith("GAME_")) {
        hasGameEnvVars = true;
        String configKey =
            envKey
                .substring(5) // 移除 "GAME_" 前缀
                .toLowerCase()
                .replace('_', '.');

        LOGGER.info(String.format("环境变量映射: %s=%s → %s=%s", envKey, envValue, configKey, envValue));
      }
    }

    if (!hasGameEnvVars) {
      LOGGER.info("当前未设置 GAME_* 环境变量");
      LOGGER.info("示例: GAME_SERVER_PORT=9000 → server.port=9000");
      LOGGER.info("示例: GAME_DATABASE_URL=xxx → database.url=xxx");
    }
  }
}
