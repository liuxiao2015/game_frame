package com.game.launcher;

import com.game.common.component.ComponentException;
import com.game.common.component.ComponentManager;
import java.util.logging.Logger;

/**
 * 指令协议框架演示启动器。
 *
 * <p>此类演示如何启动基于指令路由的网络服务器，展示轻量级指令协议框架的完整功能：
 *
 * <ul>
 *   <li>行文本协议解析（cmd k=v k=v...）
 *   <li>指令路由分发到对应处理器
 *   <li>会话管理（Session）与 traceId 注入
 *   <li>业务线程池解耦 IO 线程
 *   <li>心跳检测与空闲超时处理
 *   <li>示例指令：echo、time、sum、ping
 * </ul>
 *
 * <p>启动后可通过 telnet 或 nc 连接测试：
 *
 * <pre>{@code
 * # 连接服务器
 * telnet localhost 7001
 * 
 * # 测试指令
 * echo msg=hello seq=1
 * time seq=2
 * sum a=10 b=20 seq=3
 * ping seq=4
 * }</pre>
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
public final class LauncherProtocolDemo {

  private static final Logger LOGGER = Logger.getLogger(LauncherProtocolDemo.class.getName());

  /** 组件管理器实例 */
  private static ComponentManager componentManager;

  /** 私有构造器，防止实例化。 工具类不应被实例化，符合阿里巴巴 Java 开发规范。 */
  private LauncherProtocolDemo() {
    // 工具类不允许实例化
  }

  /**
   * 程序主入口。 演示指令协议框架的完整功能。
   *
   * @param args 命令行参数（当前未使用）
   */
  public static void main(String[] args) {
    LOGGER.info("========================================");
    LOGGER.info("指令协议框架演示程序启动");
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
      LOGGER.info("指令协议框架演示程序运行中");
      LOGGER.info("服务器已启动，支持以下指令：");
      LOGGER.info("  echo msg=<message> [seq=<seq>]     - 回显消息");
      LOGGER.info("  time [seq=<seq>]                   - 获取服务器时间");
      LOGGER.info("  sum a=<num1> b=<num2> [seq=<seq>]  - 计算两数之和");
      LOGGER.info("  ping [seq=<seq>]                   - 心跳检测");
      LOGGER.info("");
      LOGGER.info("连接方式：telnet localhost 7001");
      LOGGER.info("示例指令：echo msg=hello seq=1");
      LOGGER.info("退出指令：quit 或 exit");
      LOGGER.info("");
      LOGGER.info("按 Ctrl+C 或关闭 IDE 退出程序");
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
                  LOGGER.info("指令协议框架演示程序已退出");
                  LOGGER.info("========================================");
                },
                "ComponentShutdownHook"));
  }

  /** 保持程序运行状态。 程序会持续运行，直到接收到中断信号（如 Ctrl+C）或 JVM 关闭。 指令协议服务器会持续监听客户端连接并处理指令请求。 */
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