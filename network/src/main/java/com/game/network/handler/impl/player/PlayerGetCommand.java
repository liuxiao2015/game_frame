package com.game.network.handler.impl.player;

import com.game.network.handler.CommandHandler;
import com.game.network.protocol.CommandMessage;
import com.game.network.session.Session;
import com.game.storage.entity.Player;
import com.game.storage.repository.PlayerRepository;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 玩家查询指令处理器。
 *
 * <p>根据玩家 ID 查询玩家信息。
 *
 * <p>请求格式：player-get id=<id> [seq=<sequence>]
 *
 * <p>响应格式：
 *
 * <ul>
 *   <li>成功：ok id=<id> name=<name> level=<level> [seq=<sequence>]
 *   <li>不存在：not_found id=<id> [seq=<sequence>]
 *   <li>失败：err msg=<error_message> [seq=<sequence>]
 * </ul>
 *
 * <p>使用示例：
 *
 * <pre>
 * 请求: player-get id=1 seq=2
 * 响应: ok id=1 name=Alice level=3 seq=2
 * </pre>
 *
 * @author game-frame
 * @since 1.0.0
 */
public final class PlayerGetCommand implements CommandHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(PlayerGetCommand.class);

  /** 玩家仓储 */
  private final PlayerRepository playerRepository;

  /**
   * 构造器。
   *
   * @param playerRepository 玩家仓储
   */
  public PlayerGetCommand(PlayerRepository playerRepository) {
    this.playerRepository = playerRepository;
  }

  @Override
  public void handle(CommandMessage request, Session session) {
    String seq = request.getSeq();

    try {
      // 解析参数
      String idStr = request.getParam("id");

      // 参数验证
      if (idStr == null || idStr.trim().isEmpty()) {
        sendErrorResponse(session, "id parameter is required", seq);
        return;
      }

      long id;
      try {
        id = Long.parseLong(idStr);
        if (id <= 0) {
          sendErrorResponse(session, "id must be greater than 0", seq);
          return;
        }
      } catch (NumberFormatException e) {
        sendErrorResponse(session, "id must be a valid long integer", seq);
        return;
      }

      LOGGER.info("处理 player-get 指令 - ID: {}, seq: {}", id, seq);

      // 查询玩家
      Optional<Player> playerOpt = playerRepository.findById(id);

      if (playerOpt.isPresent()) {
        // 发送成功响应
        sendSuccessResponse(session, playerOpt.get(), seq);
        LOGGER.info("player-get 指令处理完成 - 玩家: {}", playerOpt.get());
      } else {
        // 发送不存在响应
        sendNotFoundResponse(session, id, seq);
        LOGGER.info("player-get 指令处理完成 - 玩家不存在: ID={}", id);
      }

    } catch (Exception e) {
      LOGGER.error("player-get 指令处理异常", e);
      sendErrorResponse(session, "internal_server_error", seq);
    }
  }

  /**
   * 发送成功响应。
   *
   * @param session 会话对象
   * @param player 查询到的玩家对象
   * @param seq 序列号
   */
  private void sendSuccessResponse(Session session, Player player, String seq) {
    CommandMessage.Builder responseBuilder =
        CommandMessage.builder("ok")
            .param("id", String.valueOf(player.getId()))
            .param("name", player.getName())
            .param("level", String.valueOf(player.getLevel()));

    if (seq != null) {
      responseBuilder.seq(seq);
    }

    session.sendMessage(responseBuilder.build());
  }

  /**
   * 发送不存在响应。
   *
   * @param session 会话对象
   * @param id 查询的玩家 ID
   * @param seq 序列号
   */
  private void sendNotFoundResponse(Session session, long id, String seq) {
    CommandMessage.Builder responseBuilder =
        CommandMessage.builder("not_found").param("id", String.valueOf(id));

    if (seq != null) {
      responseBuilder.seq(seq);
    }

    session.sendMessage(responseBuilder.build());
  }

  /**
   * 发送错误响应。
   *
   * @param session 会话对象
   * @param message 错误消息
   * @param seq 序列号
   */
  private void sendErrorResponse(Session session, String message, String seq) {
    CommandMessage.Builder responseBuilder = CommandMessage.builder("err").param("msg", message);

    if (seq != null) {
      responseBuilder.seq(seq);
    }

    session.sendMessage(responseBuilder.build());
  }
}