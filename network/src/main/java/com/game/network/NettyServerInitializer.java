package com.game.network;

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
 * Netty 服务器通道初始化器。
 *
 * <p>此类负责配置 Netty 的 ChannelPipeline，设置消息处理链。采用"行分隔"文本协议，支持指令路由框架。
 *
 * <p>Pipeline 配置：
 *
 * <ol>
 *   <li>IdleStateHandler - 空闲状态检测器，支持心跳机制
 *   <li>LineBasedFrameDecoder - 基于换行符的帧解码器，最大长度 8192 字节
 *   <li>StringDecoder - 字符串解码器，使用 UTF-8 编码
 *   <li>StringEncoder - 字符串编码器，使用 UTF-8 编码
 *   <li>CommandProtocolHandler - 指令协议处理器，支持指令路由和会话管理
 * </ol>
 *
 * <p>协议说明：
 *
 * <ul>
 *   <li>消息以换行符（\n 或 \r\n）结尾
 *   <li>消息内容使用 UTF-8 编码
 *   <li>单条消息最大长度 8192 字节
 *   <li>指令格式：cmd [k=v]...
 * </ul>
 *
 * <p>空闲检测配置：
 *
 * <ul>
 *   <li>读空闲时间：60 秒（客户端 60 秒内无数据发送则断开连接）
 *   <li>写空闲时间：30 秒（服务器 30 秒内无数据发送则发送心跳 ping）
 *   <li>全空闲时间：0（禁用）
 * </ul>
 *
 * @author game-frame
 * @since 1.0.0
 */
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {

  /** 最大帧长度 - 8KB */
  private static final int MAX_FRAME_LENGTH = 8192;

  /** 读空闲时间（秒） - 客户端超过此时间无数据发送则断开连接 */
  private static final int READER_IDLE_TIME_SECONDS = 60;

  /** 写空闲时间（秒） - 服务器超过此时间无数据发送则发送心跳 */
  private static final int WRITER_IDLE_TIME_SECONDS = 30;

  /** 全空闲时间（秒） - 0 表示禁用 */
  private static final int ALL_IDLE_TIME_SECONDS = 0;

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

    // 5. 指令协议处理器，支持指令路由和会话管理
    pipeline.addLast("commandHandler", new CommandProtocolHandler());
  }
}
