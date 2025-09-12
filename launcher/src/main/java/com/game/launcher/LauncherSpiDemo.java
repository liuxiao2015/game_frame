package com.game.launcher;

import com.game.common.component.ComponentException;
import com.game.common.component.ComponentManager;
import java.util.logging.Logger;

/**
 * SPI 组件框架演示启动器。
 *
 * <p>此类演示如何使用组件管理器进行 SPI 自动装配和生命周期管理。程序启动时会自动加载所有 SPI 组件，按顺序初始化和启动，并在 JVM 退出时优雅停止。
 *
 * <p>演示流程：
 *
 * <ol>
 *   <li>创建组件管理器实例
 *   <li>通过 SPI 加载所有组件
 *   <li>初始化所有组件
 *   <li>启动所有组件
 *   <li>注册 JVM 关闭钩子，确保程序退出时优雅停止组件
 *   <li>等待用户输入或程序结束
 * </ol>
 *
 * <p>运行方式：
 *
 * <ul>
 *   <li>在 IDE 中直接运行 main 方法
 *   <li>或者使用 Maven 命令：<code>mvn -q -DskipTests -pl launcher -am package</code>，然后在 IDE 中运行此类的 main
 *       方法
 * </ul>
 *
 * @author game-frame
 * @since 1.0.0
 */
public final class LauncherSpiDemo {

  private static final Logger LOGGER = Logger.getLogger(LauncherSpiDemo.class.getName());

  /** 组件管理器实例 */
  private static ComponentManager componentManager;

  /** 私有构造器，防止实例化。 工具类不应被实例化，符合阿里巴巴 Java 开发规范。 */
  private LauncherSpiDemo() {
    // 工具类不允许实例化
  }

  /**
   * 程序主入口。 演示 SPI 组件框架的完整生命周期管理流程。
   *
   * @param args 命令行参数（当前未使用）
   */
  public static void main(String[] args) {
    LOGGER.info("========================================");
    LOGGER.info("SPI 组件框架演示程序启动");
    LOGGER.info("========================================");

    try {
      // 1. 创建组件管理器
      componentManager = new ComponentManager();
      LOGGER.info("组件管理器创建完成");

      // 2. 加载 SPI 组件
      LOGGER.info("开始加载 SPI 组件...");
      componentManager.loadFromSpi();
      LOGGER.info(String.format("SPI 组件加载完成，共加载 %d 个组件", componentManager.getComponentCount()));

      // 3. 初始化所有组件
      LOGGER.info("开始初始化所有组件...");
      componentManager.initAll();
      LOGGER.info("所有组件初始化完成");

      // 4. 启动所有组件
      LOGGER.info("开始启动所有组件...");
      componentManager.startAll();
      LOGGER.info("所有组件启动完成");

      // 5. 注册 JVM 关闭钩子
      registerShutdownHook();
      LOGGER.info("JVM 关闭钩子注册完成");

      LOGGER.info("========================================");
      LOGGER.info("SPI 组件框架演示程序运行中");
      LOGGER.info("所有组件已成功启动，按 Ctrl+C 或关闭 IDE 退出程序");
      LOGGER.info("========================================");

      // 6. 保持程序运行，等待用户中断
      keepRunning();

    } catch (ComponentException e) {
      LOGGER.severe(String.format("组件框架运行失败: %s", e.getMessage()));
      e.printStackTrace();
      System.exit(1);
    } catch (Exception e) {
      LOGGER.severe(String.format("程序运行发生未知错误: %s", e.getMessage()));
      e.printStackTrace();
      System.exit(1);
    }
  }

  /** 注册 JVM 关闭钩子。 确保在程序退出时能够优雅地停止所有组件，释放资源。 */
  private static void registerShutdownHook() {
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  LOGGER.info("========================================");
                  LOGGER.info("检测到程序退出信号，开始优雅关闭组件...");
                  LOGGER.info("========================================");

                  try {
                    if (componentManager != null) {
                      componentManager.stopAll();
                      LOGGER.info("所有组件已优雅停止");
                    }
                  } catch (Exception e) {
                    LOGGER.severe(String.format("组件停止过程中发生错误: %s", e.getMessage()));
                    e.printStackTrace();
                  }

                  LOGGER.info("========================================");
                  LOGGER.info("SPI 组件框架演示程序已退出");
                  LOGGER.info("========================================");
                },
                "ComponentShutdownHook"));
  }

  /** 保持程序运行状态。 程序会持续运行，直到接收到中断信号（如 Ctrl+C）或 JVM 关闭。 在实际的服务器应用中，这里通常是启动网络服务监听或其他长期运行的任务。 */
  private static void keepRunning() {
    try {
      // 使用 Object.wait() 让主线程保持等待状态
      // 这比使用 Thread.sleep() 更高效，且能被中断信号唤醒
      Object lock = new Object();
      synchronized (lock) {
        lock.wait();
      }
    } catch (InterruptedException e) {
      LOGGER.info("主线程接收到中断信号，程序即将退出");
      Thread.currentThread().interrupt();
    }
  }
}
