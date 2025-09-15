package com.game.network;

import com.game.network.dispatcher.CommandDispatcher;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * 支持自定义 CommandDispatcher 的 Netty 服务器通道初始化器。
 *
 * <p>此类扩展了标准的 NettyServerInitializer，允许注入自定义的 CommandDispatcher， 用于支持额外的指令处理器（如存储相关的指令）。
 *
 * <p>Pipeline 配置与标准版本相同：
 *
 * <ol>
 *   <li>IdleStateHandler - 空闲状态检测器，支持心跳机制
 *   <li>LineBasedFrameDecoder - 基于换行符的帧解码器，最大长度 8192 字节
 *   <li>StringDecoder - 字符串解码器，使用 UTF-8 编码
 *   <li>StringEncoder - 字符串编码器，使用 UTF-8 编码
 *   <li>CustomCommandProtocolHandler - 支持自定义 CommandDispatcher 的指令协议处理器
 * </ol>
 *
 * @author game-frame
 * @since 1.0.0
 */
public class CustomNettyServerInitializer extends ChannelInitializer<SocketChannel> {

  /** 最大帧长度 - 8KB */
  private static final int MAX_FRAME_LENGTH = 8192;

  /** 读空闲时间（秒） - 客户端超过此时间无数据发送则断开连接 */
  private static final int READER_IDLE_TIME_SECONDS = 60;

  /** 写空闲时间（秒） - 服务器超过此时间无数据发送则发送心跳 */
  private static final int WRITER_IDLE_TIME_SECONDS = 30;

  /** 全空闲时间（秒） - 0 表示禁用 */
  private static final int ALL_IDLE_TIME_SECONDS = 0;

  /** 自定义的指令分发器 */
  private final CommandDispatcher commandDispatcher;

  /**
   * 构造器。
   *
   * @param commandDispatcher 自定义的指令分发器
   */
  public CustomNettyServerInitializer(CommandDispatcher commandDispatcher) {
    this.commandDispatcher = commandDispatcher;
  }

  @Override
  protected void initChannel(SocketChannel ch) throws Exception {
    ChannelPipeline pipeline = ch.pipeline();

    // 1. 空闲状态检测器，支持心跳机制
    pipeline.addLast(
        "idleStateHandler",
        new IdleStateHandler(
            READER_IDLE_TIME_SECONDS,
            WRITER_IDLE_TIME_SECONDS,
            ALL_IDLE_TIME_SECONDS,
            TimeUnit.SECONDS));

    // 2. 基于换行符的帧解码器，防止粘包/拆包问题
    pipeline.addLast("frameDecoder", new LineBasedFrameDecoder(MAX_FRAME_LENGTH));

    // 3. 字符串解码器，将字节流转换为字符串
    pipeline.addLast("stringDecoder", new StringDecoder(StandardCharsets.UTF_8));

    // 4. 字符串编码器，将字符串转换为字节流
    pipeline.addLast("stringEncoder", new StringEncoder(StandardCharsets.UTF_8));

    // 5. 自定义指令协议处理器，使用注入的 CommandDispatcher
    pipeline.addLast("commandHandler", new CustomCommandProtocolHandler(commandDispatcher));
  }
}