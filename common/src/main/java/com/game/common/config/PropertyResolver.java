package com.game.common.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 属性解析器，负责加载和解析多层级配置。
 *
 * <p>此类实现了配置中心的核心功能，支持：
 *
 * <ul>
 *   <li>多环境分层加载（default/dev/test/prod）
 *   <li>配置覆盖顺序：默认配置 &lt; 环境配置 &lt; 系统属性 &lt; 环境变量
 *   <li>环境变量映射：GAME_SERVER_PORT → server.port
 *   <li>占位符解析：${key:default} 语法支持
 * </ul>
 *
 * <p>环境选择优先级：
 *
 * <ol>
 *   <li>显式传入的环境参数
 *   <li>JVM 系统属性：-Denv=prod
 *   <li>环境变量：GAME_ENV=prod
 *   <li>默认值：dev
 * </ol>
 *
 * <p>使用示例：
 *
 * <pre>{@code
 * // 加载指定环境的配置
 * Config config = PropertyResolver.load("prod");
 *
 * // 加载当前环境的配置（自动检测）
 * Config config = PropertyResolver.load();
 * }</pre>
 *
 * @author game-frame
 * @since 1.0.0
 */
public final class PropertyResolver {

  /** 默认环境名称 */
  private static final String DEFAULT_ENV = "dev";

  /** 配置文件基础路径 */
  private static final String CONFIG_BASE_PATH = "/config/";

  /** 默认配置文件名 */
  private static final String DEFAULT_CONFIG_FILE = "application.properties";

  /** 环境配置文件名模板 */
  private static final String ENV_CONFIG_FILE_TEMPLATE = "application-%s.properties";

  /** 环境变量前缀 */
  private static final String ENV_VAR_PREFIX = "GAME_";

  /** 占位符正则表达式：${key:default} */
  private static final Pattern PLACEHOLDER_PATTERN =
      Pattern.compile("\\$\\{([^}:]+)(?::([^}]*))?}");

  /** 私有构造器，防止实例化 */
  private PropertyResolver() {
    // 工具类不允许实例化
  }

  /**
   * 加载当前环境的配置。
   *
   * <p>环境检测顺序：系统属性 env → 环境变量 GAME_ENV → 默认值 dev
   *
   * @return 配置实例
   * @throws ConfigException 如果配置加载失败
   */
  public static Config load() throws ConfigException {
    String env = getCurrentEnvironment();
    return load(env);
  }

  /**
   * 加载指定环境的配置。
   *
   * @param env 环境名称（dev/test/prod）
   * @return 配置实例
   * @throws ConfigException 如果配置加载失败
   */
  public static Config load(String env) throws ConfigException {
    if (env == null || env.trim().isEmpty()) {
      env = DEFAULT_ENV;
    }

    try {
      // 1. 加载默认配置
      Properties properties = loadDefaultConfig();

      // 2. 加载环境配置并覆盖
      loadEnvironmentConfig(properties, env);

      // 3. 应用系统属性覆盖
      applySystemProperties(properties);

      // 4. 应用环境变量覆盖
      applyEnvironmentVariables(properties);

      // 5. 解析占位符
      resolvePlaceholders(properties);

      return new ConfigImpl(properties, env);

    } catch (Exception e) {
      throw new ConfigException("配置加载失败，环境: " + env, e);
    }
  }

  /**
   * 获取当前环境名称。
   *
   * @return 环境名称
   */
  private static String getCurrentEnvironment() {
    // 1. 优先使用系统属性
    String env = System.getProperty("env");
    if (env != null && !env.trim().isEmpty()) {
      return env.trim();
    }

    // 2. 使用环境变量
    env = System.getenv("GAME_ENV");
    if (env != null && !env.trim().isEmpty()) {
      return env.trim();
    }

    // 3. 默认环境
    return DEFAULT_ENV;
  }

  /**
   * 加载默认配置文件。
   *
   * @return 配置属性
   * @throws ConfigException 如果加载失败
   */
  private static Properties loadDefaultConfig() throws ConfigException {
    Properties properties = new Properties();
    String configPath = CONFIG_BASE_PATH + DEFAULT_CONFIG_FILE;

    try (InputStream inputStream = PropertyResolver.class.getResourceAsStream(configPath)) {
      if (inputStream != null) {
        properties.load(inputStream);
      }
      // 默认配置文件可以不存在
    } catch (IOException e) {
      throw new ConfigException("加载默认配置文件失败: " + configPath, e);
    }

    return properties;
  }

  /**
   * 加载环境配置文件并覆盖默认配置。
   *
   * @param properties 配置属性
   * @param env 环境名称
   * @throws ConfigException 如果加载失败
   */
  private static void loadEnvironmentConfig(Properties properties, String env)
      throws ConfigException {
    String configFile = String.format(ENV_CONFIG_FILE_TEMPLATE, env);
    String configPath = CONFIG_BASE_PATH + configFile;

    try (InputStream inputStream = PropertyResolver.class.getResourceAsStream(configPath)) {
      if (inputStream != null) {
        Properties envProperties = new Properties();
        envProperties.load(inputStream);
        // 环境配置覆盖默认配置
        properties.putAll(envProperties);
      }
      // 环境配置文件可以不存在
    } catch (IOException e) {
      throw new ConfigException("加载环境配置文件失败: " + configPath, e);
    }
  }

  /**
   * 应用系统属性覆盖。
   *
   * @param properties 配置属性
   */
  private static void applySystemProperties(Properties properties) {
    Properties systemProps = System.getProperties();
    for (String key : systemProps.stringPropertyNames()) {
      // 排除 JVM 内置属性，只处理应用相关的属性
      if (!isJvmProperty(key)) {
        properties.setProperty(key, systemProps.getProperty(key));
      }
    }
  }

  /**
   * 判断是否为 JVM 内置属性。
   *
   * @param key 属性键
   * @return 如果是 JVM 内置属性则返回 true
   */
  private static boolean isJvmProperty(String key) {
    return key.startsWith("java.")
        || key.startsWith("sun.")
        || key.startsWith("os.")
        || key.startsWith("user.")
        || key.startsWith("file.")
        || key.startsWith("line.")
        || key.equals("env"); // 排除我们的环境选择属性
  }

  /**
   * 应用环境变量覆盖。
   *
   * <p>环境变量映射规则：
   *
   * <ul>
   *   <li>前缀必须为 GAME_
   *   <li>去除前缀后转换为小写
   *   <li>下划线转换为点号
   * </ul>
   *
   * <p>示例：GAME_SERVER_PORT → server.port
   *
   * @param properties 配置属性
   */
  private static void applyEnvironmentVariables(Properties properties) {
    Map<String, String> envVars = System.getenv();
    for (Map.Entry<String, String> entry : envVars.entrySet()) {
      String envKey = entry.getKey();
      String envValue = entry.getValue();

      if (envKey.startsWith(ENV_VAR_PREFIX)) {
        // 移除前缀并转换为配置键
        String configKey =
            envKey.substring(ENV_VAR_PREFIX.length()).toLowerCase().replace('_', '.');

        if (!configKey.isEmpty()) {
          properties.setProperty(configKey, envValue);
        }
      }
    }
  }

  /**
   * 解析配置中的占位符。
   *
   * <p>支持 ${key:default} 语法，其中 default 部分为可选的默认值。 占位符解析为单层，不支持嵌套占位符。
   *
   * @param properties 配置属性
   */
  private static void resolvePlaceholders(Properties properties) {
    for (String key : properties.stringPropertyNames()) {
      String value = properties.getProperty(key);
      String resolvedValue = resolvePlaceholder(value, properties);
      if (!value.equals(resolvedValue)) {
        properties.setProperty(key, resolvedValue);
      }
    }
  }

  /**
   * 解析单个值中的占位符。
   *
   * @param value 原始值
   * @param properties 配置属性
   * @return 解析后的值
   */
  private static String resolvePlaceholder(String value, Properties properties) {
    if (value == null) {
      return null;
    }

    Matcher matcher = PLACEHOLDER_PATTERN.matcher(value);
    StringBuffer result = new StringBuffer();

    while (matcher.find()) {
      String placeholderKey = matcher.group(1);
      String defaultValue = matcher.group(2);

      String resolvedValue = properties.getProperty(placeholderKey);
      if (resolvedValue == null) {
        resolvedValue = defaultValue != null ? defaultValue : "";
      }

      matcher.appendReplacement(result, Matcher.quoteReplacement(resolvedValue));
    }

    matcher.appendTail(result);
    return result.toString();
  }

  /** 配置接口的内部实现类。 */
  public static class ConfigImpl implements Config {

    private final Properties properties;
    private final String environment;

    /**
     * 构造配置实现。
     *
     * @param properties 配置属性
     * @param environment 环境名称
     */
    public ConfigImpl(Properties properties, String environment) {
      this.properties = properties;
      this.environment = environment;
    }

    @Override
    public String getString(String key) {
      return properties.getProperty(key);
    }

    @Override
    public String getString(String key, String defaultValue) {
      return properties.getProperty(key, defaultValue);
    }

    @Override
    public int getInt(String key) throws ConfigException {
      String value = getString(key);
      if (value == null) {
        throw new ConfigException("配置键不存在: " + key);
      }
      try {
        return Integer.parseInt(value.trim());
      } catch (NumberFormatException e) {
        throw new ConfigException("配置值无法转换为整数: " + key + "=" + value, e);
      }
    }

    @Override
    public int getInt(String key, int defaultValue) throws ConfigException {
      String value = getString(key);
      if (value == null) {
        return defaultValue;
      }
      try {
        return Integer.parseInt(value.trim());
      } catch (NumberFormatException e) {
        throw new ConfigException("配置值无法转换为整数: " + key + "=" + value, e);
      }
    }

    @Override
    public boolean getBoolean(String key) throws ConfigException {
      String value = getString(key);
      if (value == null) {
        throw new ConfigException("配置键不存在: " + key);
      }
      return parseBoolean(value.trim(), key);
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) throws ConfigException {
      String value = getString(key);
      if (value == null) {
        return defaultValue;
      }
      return parseBoolean(value.trim(), key);
    }

    @Override
    public boolean containsKey(String key) {
      return properties.containsKey(key);
    }

    /**
     * 解析布尔值。
     *
     * @param value 字符串值
     * @param key 配置键（用于错误信息）
     * @return 布尔值
     * @throws ConfigException 如果无法转换
     */
    private boolean parseBoolean(String value, String key) throws ConfigException {
      if ("true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value) || "1".equals(value)) {
        return true;
      } else if ("false".equalsIgnoreCase(value)
          || "no".equalsIgnoreCase(value)
          || "0".equals(value)) {
        return false;
      } else {
        throw new ConfigException("配置值无法转换为布尔值: " + key + "=" + value);
      }
    }

    /**
     * 获取当前配置的环境名称。
     *
     * @return 环境名称
     */
    public String getEnvironment() {
      return environment;
    }
  }
}
