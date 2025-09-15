package com.game.network;

import com.game.common.config.Config;
import com.game.common.config.PropertyResolver;
import com.game.network.dispatcher.CommandDispatcher;
import com.game.network.handler.impl.EchoCommandHandler;
import com.game.network.handler.impl.PingCommandHandler;
import com.game.network.handler.impl.SumCommandHandler;
import com.game.network.handler.impl.TimeCommandHandler;
import com.game.network.protocol.CommandMessage;
import com.game.network.session.Session;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import java.net.InetSocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 指令协议处理器。
 *
 * <p>替换原有的 EchoHandler，实现基于指令路由的消息处理：
 *
 * <ul>
 *   <li>解析行文本协议为 CommandMessage
 *   <li>创建并管理 Session 会话
 *   <li>通过 CommandDispatcher 分发指令到对应处理器
 *   <li>处理连接建立和断开事件
 *   <li>处理空闲超时事件
 * </ul>
 *
 * <p>支持的指令：
 *
 * <ul>
 *   <li>echo - 回显消息
 *   <li>time - 获取服务器时间
 *   <li>sum - 计算两数之和
 *   <li>ping - 心跳检测
 * </ul>
 *
 * <p>协议格式：cmd [k=v]...
 *
 * <p>使用示例：
 *
 * <pre>
 * echo msg=hello seq=1
 * time seq=2
 * sum a=10 b=20 seq=3
 * ping seq=4
 * </pre>
 *
 * @author game-frame
 * @since 1.0.0
 */
public final class CommandProtocolHandler extends SimpleChannelInboundHandler<String> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CommandProtocolHandler.class);

  /** 指令分发器 */
  private final CommandDispatcher dispatcher;

  /** 会话对象 */
  private Session session;

  /**
   * 构造指令协议处理器。
   */
  public CommandProtocolHandler() {
    // 加载配置并创建分发器
    try {
      Config config = PropertyResolver.load();
      this.dispatcher = new CommandDispatcher(config);
    } catch (Exception e) {
      LOGGER.error("初始化指令协议处理器失败", e);
      throw new RuntimeException("指令协议处理器初始化失败", e);
    }

    // 注册内置指令处理器
    registerBuiltinHandlers();
  }

  /**
   * 注册内置指令处理器。
   */
  private void registerBuiltinHandlers() {
    dispatcher.registerHandler("echo", new EchoCommandHandler());
    dispatcher.registerHandler("time", new TimeCommandHandler());
    dispatcher.registerHandler("sum", new SumCommandHandler());
    dispatcher.registerHandler("ping", new PingCommandHandler());

    LOGGER.info("已注册 {} 个内置指令处理器", dispatcher.getHandlerCount());
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    // 创建会话
    session = new Session(ctx.channel());

    // 获取客户端地址信息
    InetSocketAddress remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();
    String clientAddress = remoteAddress.getAddress().getHostAddress();
    int clientPort = remoteAddress.getPort();

    LOGGER.info("新连接建立 - 客户端地址: {}:{}, traceId: {}", 
        clientAddress, clientPort, session.getTraceId());

    // 发送欢迎消息
    String welcomeMessage = String.format(
        "欢迎连接到 Game Frame 指令服务器! (traceId: %s)\n"
            + "支持的指令: echo, time, sum, ping\n"
            + "协议格式: cmd [k=v]...\n"
            + "示例: echo msg=hello seq=1\n"
            + "输入 'quit' 或 'exit' 可断开连接。\n",
        session.getTraceId());

    session.sendText(welcomeMessage);

    super.channelActive(ctx);
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    if (session != null) {
      LOGGER.info("连接断开 - 远程地址: {}, traceId: {}, 存活时间: {}ms", 
          session.getRemoteAddress(), session.getTraceId(), session.getUptime());
    }

    super.channelInactive(ctx);
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
    if (session == null) {
      LOGGER.warn("收到消息但会话为空: {}", msg);
      return;
    }

    // 确保 MDC 中有正确的 traceId
    session.ensureTraceId();

    LOGGER.info("接收消息: {}", msg);

    try {
      // 处理退出命令
      String trimmedMsg = msg.trim();
      if ("quit".equalsIgnoreCase(trimmedMsg) || "exit".equalsIgnoreCase(trimmedMsg)) {
        LOGGER.info("客户端请求断开连接");
        session.sendText("再见!").addListener(future -> ctx.close());
        return;
      }

      // 解析为指令消息
      CommandMessage command = CommandMessage.parse(trimmedMsg);
      
      // 分发到对应处理器
      dispatcher.dispatch(command, session);

    } catch (IllegalArgumentException e) {
      LOGGER.warn("指令解析失败: {}, 错误: {}", msg, e.getMessage());
      
      // 发送错误响应
      try {
        CommandMessage.Builder responseBuilder = CommandMessage.builder("error")
            .param("code", "PARSE_ERROR")
            .param("message", "invalid_format");
        
        // 注意：解析失败时无法获取seq，所以不添加seq参数
        session.sendMessage(responseBuilder.build());
      } catch (Exception ex) {
        LOGGER.error("发送解析错误响应失败", ex);
      }
    } catch (Exception e) {
      LOGGER.error("处理消息异常", e);
    }
  }

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    if (evt instanceof IdleStateEvent) {
      IdleStateEvent event = (IdleStateEvent) evt;
      
      if (event.state() == IdleState.READER_IDLE) {
        LOGGER.warn("客户端读空闲超时，关闭连接 - traceId: {}", 
            session != null ? session.getTraceId() : "unknown");
        ctx.close();
      } else if (event.state() == IdleState.WRITER_IDLE) {
        // 发送心跳
        if (session != null) {
          LOGGER.debug("发送心跳 ping");
          CommandMessage ping = CommandMessage.builder("ping").build();
          session.sendMessage(ping);
        }
      }
    }

    super.userEventTriggered(ctx, evt);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    String traceId = session != null ? session.getTraceId() : "unknown";
    LOGGER.error("连接处理异常 - traceId: {}", traceId, cause);

    // 关闭连接
    ctx.close();
  }

  /**
   * 获取指令分发器。
   *
   * @return 指令分发器
   */
  public CommandDispatcher getDispatcher() {
    return dispatcher;
  }
}