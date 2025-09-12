package com.game.common.component.impl;

import com.game.common.component.Component;
import com.game.common.component.ComponentException;
import java.util.logging.Logger;

/**
 * 日志组件示例实现。<br>
 * 这是一个通过 SPI 自动装配的示例组件，主要用于演示组件框架的生命周期管理。<br>
 * 在初始化、启动、停止阶段都会输出相应的日志信息，便于验证组件框架的正确性。
 *
 * <p>此组件的主要功能：
 *
 * <ul>
 *   <li>在 init() 阶段输出初始化日志
 *   <li>在 start() 阶段输出启动日志
 *   <li>在 stop() 阶段输出停止日志
 *   <li>演示组件生命周期的完整流程
 * </ul>
 *
 * <p>装配顺序设置为 100，属于较早启动的基础组件。<br>
 * 后续可以添加更多业务组件，通过调整 order 值来控制启动顺序。
 *
 * @author game-frame
 * @since 1.0.0
 */
public class LoggingComponent implements Component {

  private static final Logger LOGGER = Logger.getLogger(LoggingComponent.class.getName());

  /** 组件装配顺序，基础日志组件优先级较高 */
  private static final int ORDER = 100;

  /** 组件初始化时间戳 */
  private long initTime;

  /** 组件启动时间戳 */
  private long startTime;

  /**
   * 获取组件装配顺序。 日志组件作为基础组件，优先级设置为 100，会较早进行初始化和启动。
   *
   * @return 组件装配顺序 100
   */
  @Override
  public int getOrder() {
    return ORDER;
  }

  /**
   * 组件初始化方法。 记录组件初始化时间，输出初始化日志信息。 在实际业务组件中，这里可以进行配置读取、资源分配等初始化工作。
   *
   * @throws ComponentException 当初始化过程中发生错误时抛出
   */
  @Override
  public void init() throws ComponentException {
    try {
      initTime = System.currentTimeMillis();
      LOGGER.info("========================================");
      LOGGER.info("日志组件开始初始化...");
      LOGGER.info(String.format("组件类名: %s", this.getClass().getName()));
      LOGGER.info(String.format("装配顺序: %d", ORDER));
      LOGGER.info(String.format("初始化时间: %d", initTime));

      // 模拟初始化工作
      Thread.sleep(10);

      LOGGER.info("日志组件初始化完成！");
      LOGGER.info("========================================");
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ComponentException("日志组件初始化被中断", e);
    } catch (Exception e) {
      throw new ComponentException("日志组件初始化失败", e);
    }
  }

  /**
   * 组件启动方法。 记录组件启动时间，输出启动日志信息。 在实际业务组件中，这里可以启动服务、开始监听端口等。
   *
   * @throws ComponentException 当启动过程中发生错误时抛出
   */
  @Override
  public void start() throws ComponentException {
    try {
      startTime = System.currentTimeMillis();
      LOGGER.info("========================================");
      LOGGER.info("日志组件开始启动...");
      LOGGER.info(String.format("启动时间: %d", startTime));
      LOGGER.info(String.format("初始化耗时: %d ms", startTime - initTime));

      // 模拟启动工作
      Thread.sleep(10);

      LOGGER.info("日志组件启动完成！组件已开始提供服务");
      LOGGER.info("========================================");
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ComponentException("日志组件启动被中断", e);
    } catch (Exception e) {
      throw new ComponentException("日志组件启动失败", e);
    }
  }

  /**
   * 组件停止方法。 输出停止日志信息，记录组件运行时长。 在实际业务组件中，这里可以关闭服务、释放资源、清理缓存等。
   *
   * @throws ComponentException 当停止过程中发生错误时抛出
   */
  @Override
  public void stop() throws ComponentException {
    try {
      long stopTime = System.currentTimeMillis();
      LOGGER.info("========================================");
      LOGGER.info("日志组件开始停止...");
      LOGGER.info(String.format("停止时间: %d", stopTime));
      LOGGER.info(String.format("运行时长: %d ms", stopTime - startTime));

      // 模拟停止工作
      Thread.sleep(10);

      LOGGER.info("日志组件停止完成！资源已释放");
      LOGGER.info("========================================");
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ComponentException("日志组件停止被中断", e);
    } catch (Exception e) {
      throw new ComponentException("日志组件停止失败", e);
    }
  }
}
