package com.game.network;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Netty 服务器通道初始化器。
 *
 * <p>此类负责配置 Netty 的 ChannelPipeline，设置消息处理链。采用"行分隔"文本协议，便于使用 telnet 或 nc 进行调试。
 *
 * <p>Pipeline 配置：
 *
 * <ol>
 *   <li>LineBasedFrameDecoder - 基于换行符的帧解码器，最大长度 8192 字节
 *   <li>StringDecoder - 字符串解码器，使用 UTF-8 编码
 *   <li>StringEncoder - 字符串编码器，使用 UTF-8 编码
 *   <li>EchoHandler - Echo 消息处理器
 * </ol>
 *
 * <p>协议说明：
 *
 * <ul>
 *   <li>消息以换行符（\n 或 \r\n）结尾
 *   <li>消息内容使用 UTF-8 编码
 *   <li>单条消息最大长度 8192 字节
 * </ul>
 *
 * @author game-frame
 * @since 1.0.0
 */
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {

  /** 最大帧长度 - 8KB */
  private static final int MAX_FRAME_LENGTH = 8192;

  @Override
  protected void initChannel(SocketChannel ch) throws Exception {
    ChannelPipeline pipeline = ch.pipeline();

    // 1. 基于换行符的帧解码器，防止粘包/拆包问题
    pipeline.addLast("frameDecoder", new LineBasedFrameDecoder(MAX_FRAME_LENGTH));

    // 2. 字符串解码器，将字节流转换为字符串
    pipeline.addLast("stringDecoder", new StringDecoder(StandardCharsets.UTF_8));

    // 3. 字符串编码器，将字符串转换为字节流
    pipeline.addLast("stringEncoder", new StringEncoder(StandardCharsets.UTF_8));

    // 4. Echo 消息处理器
    pipeline.addLast("echoHandler", new EchoHandler());
  }
}
