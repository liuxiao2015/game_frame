package com.game.launcher;

import com.game.common.config.Config;
import com.game.common.config.ConfigException;
import com.game.common.config.PropertyResolver;
import com.game.network.CustomNettyServerInitializer;
import com.game.network.dispatcher.CommandDispatcher;
import com.game.network.handler.impl.EchoCommandHandler;
import com.game.network.handler.impl.PingCommandHandler;
import com.game.network.handler.impl.SumCommandHandler;
import com.game.network.handler.impl.TimeCommandHandler;
import com.game.network.handler.impl.player.PlayerGetCommand;
import com.game.network.handler.impl.player.PlayerSaveCommand;
import com.game.storage.DataSourceProvider;
import com.game.storage.DbException;
import com.game.storage.JdbcTemplate;
import com.game.storage.repository.JdbcPlayerRepository;
import com.game.storage.repository.PlayerRepository;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 * 协议指令与存储集成演示启动器。
 *
 * <p>此类演示如何将指令路由框架与存储抽象层集成，提供完整的数据持久化功能：
 *
 * <ul>
 *   <li>集成 PR-07 的指令路由功能（echo、time、sum、ping）
 *   <li>集成 PR-08 的存储抽象功能（数据源管理、JDBC 操作）
 *   <li>新增玩家相关指令：player-save、player-get
 *   <li>支持 H2 内存数据库和 MySQL 数据库切换
 *   <li>优雅的资源管理和关闭流程
 * </ul>
 *
 * <p>支持的指令：
 *
 * <ul>
 *   <li>echo msg=&lt;message&gt; [seq=&lt;seq&gt;] - 回显消息
 *   <li>time [seq=&lt;seq&gt;] - 获取服务器时间
 *   <li>sum a=&lt;num1&gt; b=&lt;num2&gt; [seq=&lt;seq&gt;] - 计算两数之和
 *   <li>ping [seq=&lt;seq&gt;] - 心跳检测
 *   <li>player-save name=&lt;name&gt; level=&lt;level&gt; [seq=&lt;seq&gt;] - 保存玩家信息
 *   <li>player-get id=&lt;id&gt; [seq=&lt;seq&gt;] - 查询玩家信息
 * </ul>
 *
 * <p>启动后可通过 telnet 或 nc 连接测试：
 *
 * <pre>{@code
 * # 连接服务器
 * telnet localhost 7001
 *
 * # 测试基础指令
 * echo msg=hello seq=1
 * time seq=2
 * sum a=10 b=20 seq=3
 * ping seq=4
 *
 * # 测试玩家指令
 * player-save name=Alice level=3 seq=5
 * player-get id=1 seq=6
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
public final class LauncherProtocolWithStorageDemo {

  private static final Logger LOGGER =
      Logger.getLogger(LauncherProtocolWithStorageDemo.class.getName());

  /** 默认服务器端口 */
  private static final int DEFAULT_PORT = 7001;

  /** 数据源提供者 */
  private static DataSourceProvider dataSourceProvider;

  /** Netty 服务器相关资源 */
  private static EventLoopGroup bossGroup;

  private static EventLoopGroup workerGroup;
  private static ChannelFuture serverChannelFuture;

  /** 私有构造器，防止实例化。 工具类不应被实例化，符合阿里巴巴 Java 开发规范。 */
  private LauncherProtocolWithStorageDemo() {
    // 工具类不允许实例化
  }

  /**
   * 程序主入口。 演示协议指令与存储集成的完整功能。
   *
   * @param args 命令行参数（当前未使用）
   */
  public static void main(String[] args) {
    LOGGER.info("========================================");
    LOGGER.info("协议指令与存储集成演示程序启动");
    LOGGER.info("========================================");

    try {
      // 1. 加载配置
      Config config = PropertyResolver.load();
      LOGGER.info("配置加载完成");

      // 2. 初始化数据源
      dataSourceProvider = new DataSourceProvider(config);
      DataSource dataSource = dataSourceProvider.get();
      LOGGER.info("数据源初始化完成");

      // 3. 创建 JDBC 模板和玩家仓储
      JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
      PlayerRepository playerRepository = new JdbcPlayerRepository(jdbcTemplate);
      LOGGER.info("玩家仓储初始化完成");

      // 4. 创建指令分发器并注册处理器
      CommandDispatcher dispatcher = new CommandDispatcher(config);
      registerCommandHandlers(dispatcher, playerRepository);
      LOGGER.info("指令分发器初始化完成");

      // 5. 启动 Netty 服务器
      startNettyServer(config, dispatcher);
      LOGGER.info("Netty 服务器启动完成");

      // 6. 注册 JVM 关闭钩子
      registerShutdownHook();
      LOGGER.info("JVM 关闭钩子注册完成");

      LOGGER.info("========================================");
      LOGGER.info("协议指令与存储集成演示程序运行中");
      LOGGER.info("服务器已启动，支持以下指令：");
      LOGGER.info("  基础指令：");
      LOGGER.info("    echo msg=<message> [seq=<seq>]     - 回显消息");
      LOGGER.info("    time [seq=<seq>]                   - 获取服务器时间");
      LOGGER.info("    sum a=<num1> b=<num2> [seq=<seq>]  - 计算两数之和");
      LOGGER.info("    ping [seq=<seq>]                   - 心跳检测");
      LOGGER.info("  玩家指令：");
      LOGGER.info("    player-save name=<name> level=<level> [seq=<seq>] - 保存玩家信息");
      LOGGER.info("    player-get id=<id> [seq=<seq>]                   - 查询玩家信息");
      LOGGER.info("");
      LOGGER.info("连接方式：telnet localhost 7001");
      LOGGER.info("示例指令：player-save name=Alice level=3 seq=1");
      LOGGER.info("退出指令：quit 或 exit");
      LOGGER.info("");
      LOGGER.info("按 Ctrl+C 或关闭 IDE 退出程序");
      LOGGER.info("========================================");

      // 7. 保持程序运行，等待用户中断
      keepRunning();

    } catch (ConfigException e) {
      LOGGER.severe(String.format("配置加载失败: %s", e.getMessage()));
      e.printStackTrace();
      System.exit(1);
    } catch (DbException e) {
      LOGGER.severe(String.format("数据库初始化失败: %s", e.getMessage()));
      e.printStackTrace();
      System.exit(1);
    } catch (Exception e) {
      LOGGER.severe(String.format("程序运行发生未知错误: %s", e.getMessage()));
      e.printStackTrace();
      System.exit(1);
    }
  }

  /**
   * 注册指令处理器。
   *
   * @param dispatcher 指令分发器
   * @param playerRepository 玩家仓储
   */
  private static void registerCommandHandlers(
      CommandDispatcher dispatcher, PlayerRepository playerRepository) {
    // 注册基础指令处理器
    dispatcher.registerHandler("echo", new EchoCommandHandler());
    dispatcher.registerHandler("time", new TimeCommandHandler());
    dispatcher.registerHandler("sum", new SumCommandHandler());
    dispatcher.registerHandler("ping", new PingCommandHandler());

    // 注册玩家指令处理器
    dispatcher.registerHandler("player-save", new PlayerSaveCommand(playerRepository));
    dispatcher.registerHandler("player-get", new PlayerGetCommand(playerRepository));

    LOGGER.info(String.format("已注册 %d 个指令处理器", dispatcher.getHandlerCount()));
  }

  /**
   * 启动 Netty 服务器。
   *
   * @param config 配置对象
   * @param dispatcher 指令分发器
   * @throws Exception 启动异常
   */
  private static void startNettyServer(Config config, CommandDispatcher dispatcher)
      throws Exception {
    // 从配置中读取端口
    int port = config.getInt("server.port", DEFAULT_PORT);

    // 创建线程组
    bossGroup = new NioEventLoopGroup(1);
    workerGroup = new NioEventLoopGroup();

    // 创建服务器引导
    ServerBootstrap bootstrap = new ServerBootstrap();
    bootstrap
        .group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel.class)
        .option(ChannelOption.SO_BACKLOG, 128)
        .childOption(ChannelOption.SO_KEEPALIVE, true)
        .childHandler(new CustomNettyServerInitializer(dispatcher));

    // 绑定端口并启动服务器
    serverChannelFuture = bootstrap.bind(port).sync();

    LOGGER.info(String.format("Netty 服务器启动成功，监听端口: %d", port));
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
                    // 关闭 Netty 服务器
                    if (serverChannelFuture != null) {
                      serverChannelFuture.channel().closeFuture().sync();
                      LOGGER.info("Netty 服务器通道已关闭");
                    }

                    if (workerGroup != null) {
                      workerGroup.shutdownGracefully().sync();
                      LOGGER.info("Netty Worker 线程组已关闭");
                    }

                    if (bossGroup != null) {
                      bossGroup.shutdownGracefully().sync();
                      LOGGER.info("Netty Boss 线程组已关闭");
                    }

                    // 关闭数据源
                    if (dataSourceProvider != null) {
                      dataSourceProvider.shutdown();
                      LOGGER.info("数据源已关闭");
                    }
                  } catch (Exception e) {
                    LOGGER.severe(String.format("组件停止过程中发生错误: %s", e.getMessage()));
                    e.printStackTrace();
                  }

                  LOGGER.info("========================================");
                  LOGGER.info("协议指令与存储集成演示程序已退出");
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