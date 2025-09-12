package com.game.common.component;

/**
 * 组件框架统一异常类。 用于封装组件初始化、启动、停止过程中发生的各种异常，提供统一的错误处理机制。
 *
 * <p>此异常类继承自 RuntimeException，支持以下使用场景：
 *
 * <ul>
 *   <li>组件初始化失败
 *   <li>组件启动异常
 *   <li>组件停止错误
 *   <li>SPI 加载失败
 *   <li>组件依赖关系错误
 * </ul>
 *
 * @author game-frame
 * @since 1.0.0
 */
public class ComponentException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * 构造一个新的组件异常，使用指定的详细消息。
   *
   * @param message 详细消息，描述异常发生的原因
   */
  public ComponentException(String message) {
    super(message);
  }

  /**
   * 构造一个新的组件异常，使用指定的详细消息和原因。
   *
   * @param message 详细消息，描述异常发生的原因
   * @param cause 引起此异常的底层原因异常
   */
  public ComponentException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * 构造一个新的组件异常，使用指定的原因。 详细消息会被设置为 cause.toString()。
   *
   * @param cause 引起此异常的底层原因异常
   */
  public ComponentException(Throwable cause) {
    super(cause);
  }
}
