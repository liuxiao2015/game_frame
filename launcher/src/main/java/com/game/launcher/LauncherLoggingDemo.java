package com.game.launcher;

import com.game.common.observability.MetricsReporter;
import com.game.common.observability.TraceContext;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 日志与观测功能演示启动器.
 *
 * <p>此类演示如何使用统一日志门面（SLF4J）与观测工具，包括：
 *
 * <ul>
 *   <li>生成和使用 traceId 进行日志追踪
 *   <li>结构化日志输出与 MDC 透传
 *   <li>JVM 指标定期上报
 *   <li>优雅关闭与资源清理
 * </ul>
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
public final class LauncherLoggingDemo {

  private static final Logger logger = LoggerFactory.getLogger(LauncherLoggingDemo.class);

  /** 指标上报器实例 */
  private static MetricsReporter metricsReporter;

  /** 私有构造器，防止实例化. 工具类不应被实例化，符合阿里巴巴 Java 开发规范. */
  private LauncherLoggingDemo() {
    // 工具类不允许实例化
  }

  /**
   * 程序主入口. 演示日志与观测功能的完整使用流程.
   *
   * @param args 命令行参数（当前未使用）
   */
  public static void main(final String[] args) {
    // 1. 生成 traceId 并放入 MDC
    String traceId = TraceContext.generateTraceId();
    TraceContext.put(traceId);

    logger.info("========================================");
    logger.info("日志与观测功能演示程序启动");
    logger.info("当前 traceId: {}", traceId);
    logger.info("========================================");

    try {
      // 2. 初始化指标上报器（30秒间隔）
      logger.info("初始化 JVM 指标上报器...");
      metricsReporter = new MetricsReporter(Duration.ofSeconds(30));

      // 3. 启动指标上报
      logger.info("启动 JVM 指标定期上报...");
      metricsReporter.start();

      // 4. 注册 JVM 关闭钩子
      registerShutdownHook();
      logger.info("JVM 关闭钩子注册完成");

      // 5. 模拟业务运行
      logger.info("开始模拟业务运行，程序将运行 90 秒...");
      logger.info("期间会每 30 秒输出一次 JVM 指标，请观察日志输出");

      // 睡眠 90 秒，足够看到 3 次指标输出
      Thread.sleep(90000);

      logger.info("业务运行结束，程序即将退出");

    } catch (InterruptedException e) {
      logger.warn("程序被中断: {}", e.getMessage());
      Thread.currentThread().interrupt();
    } catch (Exception e) {
      logger.error("程序运行发生未知错误: {}", e.getMessage(), e);
      System.exit(1);
    } finally {
      // 清理 MDC
      TraceContext.clear();
    }
  }

  /** 注册 JVM 关闭钩子. 确保在程序退出时能够优雅地停止指标上报器，释放资源. */
  private static void registerShutdownHook() {
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  try {
                    logger.info("========================================");
                    logger.info("检测到程序退出信号，开始优雅关闭...");
                    if (metricsReporter != null) {
                      metricsReporter.stop();
                      logger.info("JVM 指标上报器已停止");
                    }
                    logger.info("程序优雅关闭完成");
                    logger.info("========================================");
                  } catch (Exception e) {
                    logger.error("关闭过程中发生错误: {}", e.getMessage(), e);
                  }
                },
                "shutdown-hook"));
  }
}
