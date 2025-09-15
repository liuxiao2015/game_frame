package com.game.network.handler.impl;

import com.game.network.handler.CommandHandler;
import com.game.network.protocol.CommandMessage;
import com.game.network.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sum 指令处理器。
 *
 * <p>计算两个数字的和。
 *
 * <p>请求格式：sum a=<number1> b=<number2> [seq=<sequence>]
 *
 * <p>响应格式：sum a=<number1> b=<number2> result=<sum> [seq=<sequence>]
 *
 * <p>使用示例：
 *
 * <pre>
 * 请求: sum a=10 b=20 seq=3
 * 响应: sum a=10 b=20 result=30 seq=3
 * </pre>
 *
 * @author game-frame
 * @since 1.0.0
 */
public final class SumCommandHandler implements CommandHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(SumCommandHandler.class);

  @Override
  public void handle(CommandMessage request, Session session) {
    String aStr = request.getParam("a");
    String bStr = request.getParam("b");
    String seq = request.getSeq();

    LOGGER.info("处理 sum 指令 - a: {}, b: {}, seq: {}", aStr, bStr, seq);

    // 参数验证
    if (aStr == null || aStr.isEmpty()) {
      sendErrorResponse(session, seq, "参数 a 不能为空");
      return;
    }

    if (bStr == null || bStr.isEmpty()) {
      sendErrorResponse(session, seq, "参数 b 不能为空");
      return;
    }

    try {
      // 解析数字
      long a = Long.parseLong(aStr);
      long b = Long.parseLong(bStr);
      long result = a + b;

      // 构建响应
      CommandMessage.Builder responseBuilder = CommandMessage.builder("sum")
          .param("a", aStr)
          .param("b", bStr)
          .param("result", String.valueOf(result));

      if (seq != null) {
        responseBuilder.seq(seq);
      }

      CommandMessage response = responseBuilder.build();
      session.sendMessage(response);

      LOGGER.debug("sum 指令处理完成 - a: {}, b: {}, result: {}", a, b, result);

    } catch (NumberFormatException e) {
      LOGGER.warn("sum 指令参数格式错误 - a: {}, b: {}", aStr, bStr);
      sendErrorResponse(session, seq, "参数必须是有效的数字");
    }
  }

  /**
   * 发送错误响应。
   *
   * @param session 会话对象
   * @param seq 序列号
   * @param message 错误消息
   */
  private void sendErrorResponse(Session session, String seq, String message) {
    try {
      CommandMessage.Builder responseBuilder = CommandMessage.builder("error")
          .param("code", "INVALID_PARAMETER")
          .param("message", message);

      if (seq != null) {
        responseBuilder.seq(seq);
      }

      CommandMessage response = responseBuilder.build();
      session.sendMessage(response);
    } catch (Exception e) {
      LOGGER.error("发送错误响应失败", e);
    }
  }
}