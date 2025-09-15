package com.game.network.dispatcher;

import com.game.common.config.Config;
import com.game.network.handler.CommandHandler;
import com.game.network.protocol.CommandMessage;
import com.game.network.session.Session;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 指令分发器，负责将指令路由到对应的处理器。
 *
 * <p>主要功能：
 *
 * <ul>
 *   <li>指令路由 - 根据指令名分发到对应处理器
 *   <li>业务线程池 - 将指令处理从 IO 线程解耦
 *   <li>异常处理 - 统一的异常处理和错误响应
 *   <li>性能监控 - 记录处理时间和错误统计
 * </ul>
 *
 * <p>线程池配置：
 *
 * <ul>
 *   <li>business.thread.core - 核心线程数，默认 4
 *   <li>business.thread.max - 最大线程数，默认 16
 *   <li>business.thread.keepalive - 空闲线程存活时间（秒），默认 60
 *   <li>business.thread.queue - 队列大小，默认 1000
 * </ul>
 *
 * <p>使用示例：
 *
 * <pre>{@code
 * CommandDispatcher dispatcher = new CommandDispatcher(config);
 * dispatcher.registerHandler("echo", new EchoCommandHandler());
 * dispatcher.registerHandler("time", new TimeCommandHandler());
 * 
 * // 分发指令
 * CommandMessage request = CommandMessage.parse("echo msg=hello seq=1");
 * dispatcher.dispatch(request, session);
 * }</pre>
 *
 * @author game-frame
 * @since 1.0.0
 */
public final class CommandDispatcher {

  private static final Logger LOGGER = LoggerFactory.getLogger(CommandDispatcher.class);

  /** 默认核心线程数 */
  private static final int DEFAULT_CORE_THREADS = 4;

  /** 默认最大线程数 */
  private static final int DEFAULT_MAX_THREADS = 16;

  /** 默认空闲线程存活时间（秒） */
  private static final int DEFAULT_KEEPALIVE_SECONDS = 60;

  /** 默认队列大小 */
  private static final int DEFAULT_QUEUE_SIZE = 1000;

  /** 指令处理器映射表 */
  private final ConcurrentMap<String, CommandHandler> handlers = new ConcurrentHashMap<>();

  /** 业务线程池 */
  private final ExecutorService businessExecutor;

  /** 配置对象 */
  private final Config config;

  /**
   * 构造指令分发器。
   *
   * @param config 配置对象
   */
  public CommandDispatcher(Config config) {
    this.config = config;
    this.businessExecutor = createBusinessExecutor();

    LOGGER.info("指令分发器已初始化 - 线程池配置: core={}, max={}, queue={}",
        getThreadPoolCoreSize(), getThreadPoolMaxSize(), getThreadPoolQueueSize());
  }

  /**
   * 创建业务线程池。
   *
   * @return ExecutorService
   */
  private ExecutorService createBusinessExecutor() {
    int coreSize = getThreadPoolCoreSize();
    int maxSize = getThreadPoolMaxSize();
    int keepAliveSeconds = getThreadPoolKeepAliveSeconds();
    int queueSize = getThreadPoolQueueSize();

    ThreadFactory threadFactory = new BusinessThreadFactory();

    return new ThreadPoolExecutor(
        coreSize,
        maxSize,
        keepAliveSeconds,
        TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(queueSize),
        threadFactory,
        new ThreadPoolExecutor.CallerRunsPolicy() // 队列满时在调用线程执行
    );
  }

  /**
   * 获取线程池核心大小。
   */
  private int getThreadPoolCoreSize() {
    try {
      return config.getInt("business.thread.core", DEFAULT_CORE_THREADS);
    } catch (Exception e) {
      LOGGER.warn("获取业务线程池核心大小配置失败，使用默认值: {}", DEFAULT_CORE_THREADS, e);
      return DEFAULT_CORE_THREADS;
    }
  }

  /**
   * 获取线程池最大大小。
   */
  private int getThreadPoolMaxSize() {
    try {
      return config.getInt("business.thread.max", DEFAULT_MAX_THREADS);
    } catch (Exception e) {
      LOGGER.warn("获取业务线程池最大大小配置失败，使用默认值: {}", DEFAULT_MAX_THREADS, e);
      return DEFAULT_MAX_THREADS;
    }
  }

  /**
   * 获取线程池空闲时间。
   */
  private int getThreadPoolKeepAliveSeconds() {
    try {
      return config.getInt("business.thread.keepalive", DEFAULT_KEEPALIVE_SECONDS);
    } catch (Exception e) {
      LOGGER.warn("获取业务线程池空闲时间配置失败，使用默认值: {}", DEFAULT_KEEPALIVE_SECONDS, e);
      return DEFAULT_KEEPALIVE_SECONDS;
    }
  }

  /**
   * 获取线程池队列大小。
   */
  private int getThreadPoolQueueSize() {
    try {
      return config.getInt("business.thread.queue", DEFAULT_QUEUE_SIZE);
    } catch (Exception e) {
      LOGGER.warn("获取业务线程池队列大小配置失败，使用默认值: {}", DEFAULT_QUEUE_SIZE, e);
      return DEFAULT_QUEUE_SIZE;
    }
  }

  /**
   * 注册指令处理器。
   *
   * @param command 指令名
   * @param handler 处理器
   */
  public void registerHandler(String command, CommandHandler handler) {
    if (command == null || command.isEmpty()) {
      throw new IllegalArgumentException("指令名不能为空");
    }
    if (handler == null) {
      throw new IllegalArgumentException("处理器不能为 null");
    }

    CommandHandler old = handlers.put(command, handler);
    if (old != null) {
      LOGGER.warn("指令处理器被覆盖: {}", command);
    } else {
      LOGGER.info("注册指令处理器: {}", command);
    }
  }

  /**
   * 注销指令处理器。
   *
   * @param command 指令名
   * @return 被注销的处理器，如果不存在则返回 null
   */
  public CommandHandler unregisterHandler(String command) {
    CommandHandler handler = handlers.remove(command);
    if (handler != null) {
      LOGGER.info("注销指令处理器: {}", command);
    }
    return handler;
  }

  /**
   * 分发指令到对应处理器。
   *
   * @param request 请求消息
   * @param session 会话对象
   */
  public void dispatch(CommandMessage request, Session session) {
    if (request == null) {
      LOGGER.warn("收到空的请求消息");
      return;
    }

    String command = request.getCommand();
    CommandHandler handler = handlers.get(command);

    if (handler == null) {
      handleUnknownCommand(request, session);
      return;
    }

    // 提交到业务线程池执行
    businessExecutor.submit(() -> executeCommand(request, session, handler));
  }

  /**
   * 执行指令处理。
   *
   * @param request 请求消息
   * @param session 会话对象
   * @param handler 处理器
   */
  private void executeCommand(CommandMessage request, Session session, CommandHandler handler) {
    // 确保 MDC 中有正确的 traceId
    session.ensureTraceId();

    long startTime = System.currentTimeMillis();
    String command = request.getCommand();

    try {
      LOGGER.debug("开始处理指令: {}", command);
      handler.handle(request, session);
      
      long duration = System.currentTimeMillis() - startTime;
      LOGGER.debug("指令处理完成: {}, 耗时: {}ms", command, duration);

    } catch (Exception e) {
      long duration = System.currentTimeMillis() - startTime;
      LOGGER.error("指令处理异常: {}, 耗时: {}ms", command, duration, e);
      
      handleCommandError(request, session, e);
    }
  }

  /**
   * 处理未知指令。
   *
   * @param request 请求消息
   * @param session 会话对象
   */
  private void handleUnknownCommand(CommandMessage request, Session session) {
    String command = request.getCommand();
    LOGGER.warn("收到未知指令: {}", command);

    try {
      CommandMessage response = CommandMessage.builder("error")
          .param("code", "UNKNOWN_COMMAND")
          .param("message", "未知指令: " + command)
          .seq(request.getSeq())
          .build();
      
      session.sendMessage(response);
    } catch (Exception e) {
      LOGGER.error("发送错误响应失败", e);
    }
  }

  /**
   * 处理指令执行错误。
   *
   * @param request 请求消息
   * @param session 会话对象
   * @param error 异常
   */
  private void handleCommandError(CommandMessage request, Session session, Exception error) {
    try {
      CommandMessage response = CommandMessage.builder("error")
          .param("code", "COMMAND_ERROR")
          .param("message", "指令执行失败: " + error.getMessage())
          .seq(request.getSeq())
          .build();
      
      session.sendMessage(response);
    } catch (Exception e) {
      LOGGER.error("发送错误响应失败", e);
    }
  }

  /**
   * 获取已注册的指令数量。
   *
   * @return 指令数量
   */
  public int getHandlerCount() {
    return handlers.size();
  }

  /**
   * 检查指令是否已注册。
   *
   * @param command 指令名
   * @return 是否已注册
   */
  public boolean hasHandler(String command) {
    return handlers.containsKey(command);
  }

  /**
   * 关闭分发器，释放资源。
   */
  public void shutdown() {
    LOGGER.info("正在关闭指令分发器...");
    
    businessExecutor.shutdown();
    try {
      if (!businessExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
        LOGGER.warn("业务线程池未能在 10 秒内优雅关闭，强制关闭");
        businessExecutor.shutdownNow();
      }
    } catch (InterruptedException e) {
      LOGGER.warn("等待业务线程池关闭时被中断");
      businessExecutor.shutdownNow();
      Thread.currentThread().interrupt();
    }

    LOGGER.info("指令分发器已关闭");
  }

  /** 业务线程工厂。 */
  private static class BusinessThreadFactory implements ThreadFactory {
    private final AtomicInteger threadNumber = new AtomicInteger(1);

    @Override
    public Thread newThread(Runnable r) {
      Thread t = new Thread(r, "business-" + threadNumber.getAndIncrement());
      t.setDaemon(false);
      t.setPriority(Thread.NORM_PRIORITY);
      return t;
    }
  }
}