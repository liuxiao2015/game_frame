package com.game.common.config;

/**
 * 配置相关异常类。
 *
 * <p>此异常类用于封装配置加载、解析和访问过程中出现的各种错误，包括：
 *
 * <ul>
 *   <li>配置文件读取失败
 *   <li>配置值类型转换错误
 *   <li>必需配置项缺失
 *   <li>占位符解析失败
 * </ul>
 *
 * <p>异常信息应包含足够的上下文信息，便于问题定位和调试。
 *
 * @author game-frame
 * @since 1.0.0
 */
public class ConfigException extends Exception {

  /** 序列化版本 UID */
  private static final long serialVersionUID = 1L;

  /**
   * 构造一个带有详细消息的配置异常。
   *
   * @param message 异常详细消息
   */
  public ConfigException(String message) {
    super(message);
  }

  /**
   * 构造一个带有详细消息和原因的配置异常。
   *
   * @param message 异常详细消息
   * @param cause 引起此异常的原因
   */
  public ConfigException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * 构造一个带有原因的配置异常。
   *
   * @param cause 引起此异常的原因
   */
  public ConfigException(Throwable cause) {
    super(cause);
  }
}
