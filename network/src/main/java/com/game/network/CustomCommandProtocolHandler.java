package com.game.network;

import com.game.network.dispatcher.CommandDispatcher;
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
 * 支持自定义 CommandDispatcher 的指令协议处理器。
 *
 * <p>与标准的 CommandProtocolHandler 类似，但允许注入自定义的 CommandDispatcher， 用于支持额外的指令处理器（如存储相关的指令）。
 *
 * <p>功能包括：
 *
 * <ul>
 *   <li>解析行文本协议为 CommandMessage
 *   <li>创建并管理 Session 会话
 *   <li>通过注入的 CommandDispatcher 分发指令到对应处理器
 *   <li>处理连接建立和断开事件
 *   <li>处理空闲超时事件
 * </ul>
 *
 * <p>协议格式：cmd [k=v]...
 *
 * @author game-frame
 * @since 1.0.0
 */
public final class CustomCommandProtocolHandler extends SimpleChannelInboundHandler<String> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CustomCommandProtocolHandler.class);

  /** 指令分发器 */
  private final CommandDispatcher dispatcher;

  /** 会话对象 */
  private Session session;

  /**
   * 构造指令协议处理器。
   *
   * @param dispatcher 自定义的指令分发器
   */
  public CustomCommandProtocolHandler(CommandDispatcher dispatcher) {
    this.dispatcher = dispatcher;
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    // 创建会话
    session = new Session(ctx.channel());

    // 获取客户端地址信息
    InetSocketAddress remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();
    String clientAddress = remoteAddress.getAddress().getHostAddress();
    int clientPort = remoteAddress.getPort();

    LOGGER.info(
        "客户端连接建立 - 地址: {}:{}, traceId: {}",
        clientAddress,
        clientPort,
        session.getTraceId());

    super.channelActive(ctx);
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    if (session != null) {
      LOGGER.info("客户端连接断开 - traceId: {}", session.getTraceId());
    }

    super.channelInactive(ctx);
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, String message) throws Exception {
    if (session == null) {
      LOGGER.warn("会话未初始化，忽略消息: {}", message);
      return;
    }

    // 设置 traceId 到 MDC
    session.ensureTraceId();

    LOGGER.debug("收到消息 - 内容: '{}'", message);

    try {
      // 处理特殊的退出指令
      if ("quit".equalsIgnoreCase(message.trim()) || "exit".equalsIgnoreCase(message.trim())) {
        LOGGER.info("收到退出指令，关闭连接");
        ctx.close();
        return;
      }

      // 解析指令消息
      CommandMessage request = CommandMessage.parse(message);
      if (request == null) {
        LOGGER.warn("无法解析的消息格式，内容: '{}'", message);
        session.sendText("error: invalid_message_format");
        return;
      }

      LOGGER.debug(
          "解析指令成功 - 指令: {}, 参数: {}", request.getCommand(), request.getParams());

      // 分发指令到处理器
      dispatcher.dispatch(request, session);

    } catch (Exception e) {
      LOGGER.error("处理消息时发生异常", e);
      session.sendText("error: message_processing_failed");
    }
  }

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    if (evt instanceof IdleStateEvent idleStateEvent) {
      IdleState state = idleStateEvent.state();

      if (session != null) {
        session.ensureTraceId();
      }

      if (state == IdleState.READER_IDLE) {
        // 读空闲 - 客户端长时间未发送数据，断开连接
        LOGGER.warn("客户端读空闲超时，断开连接 - traceId: {}", 
            session != null ? session.getTraceId() : "unknown");
        ctx.close();

      } else if (state == IdleState.WRITER_IDLE) {
        // 写空闲 - 服务器长时间未发送数据，发送心跳
        LOGGER.debug("服务器写空闲，发送心跳 - traceId: {}", 
            session != null ? session.getTraceId() : "unknown");
        ctx.writeAndFlush("ping\n");
      }
    }

    super.userEventTriggered(ctx, evt);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    String traceId = session != null ? session.getTraceId() : "unknown";

    LOGGER.error("连接异常 - traceId: " + traceId, cause);
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