package com.game.network;

import com.game.common.observability.TraceContext;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.net.InetSocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Echo 消息处理器。
 *
 * <p>此处理器实现了简单的 Echo 功能，将接收到的消息原样返回给客户端。同时提供以下功能：
 *
 * <ul>
 *   <li>每个连接自动生成 traceId 并注入 MDC
 *   <li>记录连接建立和断开日志
 *   <li>记录消息收发日志
 *   <li>异常处理和日志记录
 * </ul>
 *
 * <p>日志格式包含 traceId，便于跟踪和调试。traceId 会在连接建立时生成，连接断开时清理。
 *
 * <p>协议说明：
 *
 * <ul>
 *   <li>客户端发送的每行消息都会被原样回复
 *   <li>支持多行消息，每行独立处理
 *   <li>连接建立时会发送欢迎消息
 * </ul>
 *
 * @author game-frame
 * @since 1.0.0
 */
public class EchoHandler extends SimpleChannelInboundHandler<String> {

  private static final Logger LOGGER = LoggerFactory.getLogger(EchoHandler.class);

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    // 为每个连接生成独立的 traceId
    String traceId = TraceContext.generateTraceId();
    TraceContext.put(traceId);

    // 获取客户端地址信息
    InetSocketAddress remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();
    String clientAddress = remoteAddress.getAddress().getHostAddress();
    int clientPort = remoteAddress.getPort();

    LOGGER.info("新连接建立 - 客户端地址: {}:{}", clientAddress, clientPort);

    // 发送欢迎消息
    String welcomeMessage =
        String.format(
            "欢迎连接到 Game Frame 服务器! (traceId: %s)\n"
                + "您可以输入任何文本，服务器将原样回显。\n"
                + "输入 'quit' 或 'exit' 可断开连接。\n",
            traceId);

    ctx.writeAndFlush(welcomeMessage);

    super.channelActive(ctx);
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    LOGGER.info("连接断开");

    // 清理 MDC 中的 traceId
    TraceContext.clear();

    super.channelInactive(ctx);
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
    // 记录接收到的消息
    LOGGER.info("接收消息: {}", msg);

    // 处理退出命令
    if ("quit".equalsIgnoreCase(msg.trim()) || "exit".equalsIgnoreCase(msg.trim())) {
      LOGGER.info("客户端请求断开连接");
      ctx.writeAndFlush("再见!\n").addListener(future -> ctx.close());
      return;
    }

    // Echo 功能：原样返回消息
    String response = "Echo: " + msg + "\n";
    ctx.writeAndFlush(response);

    // 记录发送的响应
    LOGGER.info("发送响应: {}", response.trim());
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    LOGGER.error("连接处理异常", cause);

    // 关闭连接
    ctx.close();
  }
}
