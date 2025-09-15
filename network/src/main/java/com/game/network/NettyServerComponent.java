package com.game.network;

import com.game.common.component.Component;
import com.game.common.component.ComponentException;
import com.game.common.config.Config;
import com.game.common.config.PropertyResolver;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Netty TCP 服务器组件。
 *
 * <p>此组件实现了基于 Netty 的 TCP 服务器，提供以下功能：
 *
 * <ul>
 *   <li>基于 Netty 的高性能 TCP 服务器
 *   <li>支持"行分隔"文本协议的 Echo 示例
 *   <li>优雅关闭机制
 *   <li>每连接 traceId 注入（MDC）
 *   <li>连接与消息级日志记录
 * </ul>
 *
 * <p>服务器配置从 Config 中读取，支持以下配置项：
 *
 * <ul>
 *   <li>server.port - 服务器端口，默认 7001
 * </ul>
 *
 * <p>使用示例：可通过 telnet 或 nc 命令连接测试：
 *
 * <pre>{@code
 * telnet localhost 7001
 * nc localhost 7001
 * }</pre>
 *
 * @author game-frame
 * @since 1.0.0
 */
public class NettyServerComponent implements Component {

  private static final Logger LOGGER = LoggerFactory.getLogger(NettyServerComponent.class);

  /** 默认服务器端口 */
  private static final int DEFAULT_PORT = 7001;

  /** 配置实例 */
  private Config config;

  /** Boss 线程组，用于接受连接 */
  private EventLoopGroup bossGroup;

  /** Worker 线程组，用于处理 I/O 操作 */
  private EventLoopGroup workerGroup;

  /** 服务器引导 */
  private ServerBootstrap serverBootstrap;

  /** 服务器通道 */
  private ChannelFuture serverChannelFuture;

  /** 服务器端口 */
  private int port;

  /** 默认构造器，用于 SPI 自动装配。 */
  public NettyServerComponent() {
    // SPI 需要无参构造器
  }

  @Override
  public int getOrder() {
    return 1000; // 网络组件在较后的位置启动
  }

  @Override
  public void init() throws ComponentException {
    try {
      LOGGER.info("正在初始化 Netty 服务器组件...");

      // 加载配置
      this.config = PropertyResolver.load();

      // 从配置中读取端口，默认为 7001
      this.port = config.getInt("server.port", DEFAULT_PORT);
      LOGGER.info("服务器端口配置: {}", port);

      // 创建线程组
      this.bossGroup = new NioEventLoopGroup(1); // Boss 线程组使用单线程
      this.workerGroup = new NioEventLoopGroup(); // Worker 线程组使用默认线程数

      // 创建服务器引导
      this.serverBootstrap = new ServerBootstrap();
      this.serverBootstrap
          .group(bossGroup, workerGroup)
          .channel(NioServerSocketChannel.class)
          .option(ChannelOption.SO_BACKLOG, 128)
          .childOption(ChannelOption.SO_KEEPALIVE, true)
          .childHandler(new NettyServerInitializer());

      LOGGER.info("Netty 服务器组件初始化完成");

    } catch (Exception e) {
      throw new ComponentException("Netty 服务器组件初始化失败", e);
    }
  }

  @Override
  public void start() throws ComponentException {
    try {
      LOGGER.info("正在启动 Netty 服务器，端口: {}", port);

      // 绑定端口并开始接受连接
      this.serverChannelFuture = serverBootstrap.bind(port).sync();

      if (serverChannelFuture.isSuccess()) {
        LOGGER.info("Netty 服务器启动成功，监听端口: {}", port);
        LOGGER.info("可使用以下命令进行测试:");
        LOGGER.info("  telnet localhost {}", port);
        LOGGER.info("  nc localhost {}", port);
      } else {
        throw new ComponentException("Netty 服务器绑定端口失败: " + port);
      }

    } catch (Exception e) {
      throw new ComponentException("Netty 服务器启动失败", e);
    }
  }

  @Override
  public void stop() throws ComponentException {
    try {
      LOGGER.info("正在停止 Netty 服务器...");

      // 关闭服务器通道
      if (serverChannelFuture != null) {
        serverChannelFuture.channel().close().sync();
      }

      // 优雅关闭线程组
      if (workerGroup != null) {
        workerGroup.shutdownGracefully().sync();
      }

      if (bossGroup != null) {
        bossGroup.shutdownGracefully().sync();
      }

      LOGGER.info("Netty 服务器停止完成");

    } catch (Exception e) {
      throw new ComponentException("Netty 服务器停止失败", e);
    }
  }
}
