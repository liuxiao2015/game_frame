package com.game.network.handler.impl;

import com.game.network.handler.CommandHandler;
import com.game.network.protocol.CommandMessage;
import com.game.network.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Echo 指令处理器。
 *
 * <p>实现简单的回显功能，将接收到的消息原样返回。
 *
 * <p>请求格式：echo msg=<message> [seq=<sequence>]
 *
 * <p>响应格式：echo msg=<message> [seq=<sequence>]
 *
 * <p>使用示例：
 *
 * <pre>
 * 请求: echo msg=hello seq=1
 * 响应: echo msg=hello seq=1
 * </pre>
 *
 * @author game-frame
 * @since 1.0.0
 */
public final class EchoCommandHandler implements CommandHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(EchoCommandHandler.class);

  @Override
  public void handle(CommandMessage request, Session session) {
    String message = request.getParam("msg", "");
    String seq = request.getSeq();

    LOGGER.info("处理 echo 指令 - 消息: {}, seq: {}", message, seq);

    // 构建响应
    CommandMessage.Builder responseBuilder = CommandMessage.builder("echo").param("msg", message);

    if (seq != null) {
      responseBuilder.seq(seq);
    }

    CommandMessage response = responseBuilder.build();
    session.sendMessage(response);

    LOGGER.debug("echo 指令处理完成");
  }
}
