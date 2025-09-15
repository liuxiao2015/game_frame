package com.game.network.handler.impl;

import com.game.network.handler.CommandHandler;
import com.game.network.protocol.CommandMessage;
import com.game.network.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ping 指令处理器。
 *
 * <p>实现心跳检测功能，响应 ping 请求并返回 pong。
 *
 * <p>请求格式：ping [seq=<sequence>]
 *
 * <p>响应格式：pong [seq=<sequence>]
 *
 * <p>使用示例：
 *
 * <pre>
 * 请求: ping seq=1
 * 响应: pong seq=1
 * </pre>
 *
 * @author game-frame
 * @since 1.0.0
 */
public final class PingCommandHandler implements CommandHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(PingCommandHandler.class);

  @Override
  public void handle(CommandMessage request, Session session) {
    String seq = request.getSeq();

    LOGGER.debug("处理 ping 指令 - seq: {}", seq);

    // 构建 pong 响应
    CommandMessage.Builder responseBuilder = CommandMessage.builder("pong");

    if (seq != null) {
      responseBuilder.seq(seq);
    }

    CommandMessage response = responseBuilder.build();
    session.sendMessage(response);

    LOGGER.debug("ping 指令处理完成，已返回 pong");
  }
}