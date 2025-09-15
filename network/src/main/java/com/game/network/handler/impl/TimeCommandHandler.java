package com.game.network.handler.impl;

import com.game.network.handler.CommandHandler;
import com.game.network.protocol.CommandMessage;
import com.game.network.session.Session;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Time 指令处理器。
 *
 * <p>返回当前服务器时间。
 *
 * <p>请求格式：time [seq=<sequence>]
 *
 * <p>响应格式：time timestamp=<timestamp> datetime=<datetime> [seq=<sequence>]
 *
 * <p>使用示例：
 *
 * <pre>
 * 请求: time seq=2
 * 响应: time timestamp=1703123456789 datetime=2023-12-20T15:30:56 seq=2
 * </pre>
 *
 * @author game-frame
 * @since 1.0.0
 */
public final class TimeCommandHandler implements CommandHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(TimeCommandHandler.class);

  /** 日期时间格式化器 */
  private static final DateTimeFormatter DATETIME_FORMATTER = 
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

  @Override
  public void handle(CommandMessage request, Session session) {
    String seq = request.getSeq();

    LOGGER.info("处理 time 指令 - seq: {}", seq);

    // 获取当前时间
    long timestamp = System.currentTimeMillis();
    LocalDateTime dateTime = LocalDateTime.now();
    String formattedDateTime = dateTime.format(DATETIME_FORMATTER);

    // 构建响应
    CommandMessage.Builder responseBuilder = CommandMessage.builder("time")
        .param("timestamp", String.valueOf(timestamp))
        .param("datetime", formattedDateTime);

    if (seq != null) {
      responseBuilder.seq(seq);
    }

    CommandMessage response = responseBuilder.build();
    session.sendMessage(response);

    LOGGER.debug("time 指令处理完成 - timestamp: {}, datetime: {}", timestamp, formattedDateTime);
  }
}