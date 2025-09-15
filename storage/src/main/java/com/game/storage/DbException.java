package com.game.storage;

/**
 * 数据库操作统一运行时异常封装。
 *
 * <p>用于封装数据库操作过程中发生的各种异常，提供统一的错误处理机制。
 *
 * <p>此异常类继承自 RuntimeException，支持以下使用场景：
 *
 * <ul>
 *   <li>数据源配置异常
 *   <li>连接池创建失败
 *   <li>SQL 执行异常
 *   <li>结果集处理错误
 *   <li>事务处理异常
 * </ul>
 *
 * @author game-frame
 * @since 1.0.0
 */
public class DbException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * 构造一个新的数据库异常，使用指定的详细消息。
   *
   * @param message 详细消息，描述异常发生的原因
   */
  public DbException(String message) {
    super(message);
  }

  /**
   * 构造一个新的数据库异常，使用指定的详细消息和原因。
   *
   * @param message 详细消息，描述异常发生的原因
   * @param cause 引起此异常的底层原因异常
   */
  public DbException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * 构造一个新的数据库异常，使用指定的原因。 详细消息会被设置为 cause.toString()。
   *
   * @param cause 引起此异常的底层原因异常
   */
  public DbException(Throwable cause) {
    super(cause);
  }
}
