package com.game.common.config;

/**
 * 配置接口，提供只读的配置访问能力。
 *
 * <p>此接口定义了统一的配置访问方法，支持字符串、整数、布尔值等常用数据类型的获取。 配置值可以来自多个层级的配置文件，并支持占位符解析和环境变量覆盖。
 *
 * <p>使用示例：
 *
 * <pre>{@code
 * Config config = PropertyResolver.load("prod");
 * String appName = config.getString("app.name");
 * int serverPort = config.getInt("server.port", 8080);
 * boolean debugEnabled = config.getBoolean("debug.enabled", false);
 * }</pre>
 *
 * @author game-frame
 * @since 1.0.0
 */
public interface Config {

  /**
   * 获取字符串类型的配置值。
   *
   * @param key 配置键名
   * @return 配置值，如果键不存在则返回 null
   */
  String getString(String key);

  /**
   * 获取字符串类型的配置值，支持默认值。
   *
   * @param key 配置键名
   * @param defaultValue 默认值，当键不存在时返回此值
   * @return 配置值或默认值
   */
  String getString(String key, String defaultValue);

  /**
   * 获取整数类型的配置值。
   *
   * @param key 配置键名
   * @return 配置值
   * @throws ConfigException 如果键不存在或值无法转换为整数
   */
  int getInt(String key) throws ConfigException;

  /**
   * 获取整数类型的配置值，支持默认值。
   *
   * @param key 配置键名
   * @param defaultValue 默认值，当键不存在时返回此值
   * @return 配置值或默认值
   * @throws ConfigException 如果值无法转换为整数
   */
  int getInt(String key, int defaultValue) throws ConfigException;

  /**
   * 获取布尔类型的配置值。
   *
   * @param key 配置键名
   * @return 配置值
   * @throws ConfigException 如果键不存在或值无法转换为布尔值
   */
  boolean getBoolean(String key) throws ConfigException;

  /**
   * 获取布尔类型的配置值，支持默认值。
   *
   * @param key 配置键名
   * @param defaultValue 默认值，当键不存在时返回此值
   * @return 配置值或默认值
   * @throws ConfigException 如果值无法转换为布尔值
   */
  boolean getBoolean(String key, boolean defaultValue) throws ConfigException;

  /**
   * 检查配置键是否存在。
   *
   * @param key 配置键名
   * @return 如果键存在则返回 true，否则返回 false
   */
  boolean containsKey(String key);
}
