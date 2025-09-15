package com.game.network.handler.impl.player;

import com.game.network.handler.CommandHandler;
import com.game.network.protocol.CommandMessage;
import com.game.network.session.Session;
import com.game.storage.entity.Player;
import com.game.storage.repository.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 玩家保存指令处理器。
 *
 * <p>处理玩家数据保存请求，支持新建和更新操作。
 *
 * <p>请求格式：player-save name=<name> level=<level> [seq=<sequence>]
 *
 * <p>响应格式：
 *
 * <ul>
 *   <li>成功：ok id=<id> name=<name> level=<level> [seq=<sequence>]
 *   <li>失败：err msg=<error_message> [seq=<sequence>]
 * </ul>
 *
 * <p>使用示例：
 *
 * <pre>
 * 请求: player-save name=Alice level=3 seq=1
 * 响应: ok id=1 name=Alice level=3 seq=1
 * </pre>
 *
 * @author game-frame
 * @since 1.0.0
 */
public final class PlayerSaveCommand implements CommandHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(PlayerSaveCommand.class);

  /** 玩家仓储 */
  private final PlayerRepository playerRepository;

  /**
   * 构造器。
   *
   * @param playerRepository 玩家仓储
   */
  public PlayerSaveCommand(PlayerRepository playerRepository) {
    this.playerRepository = playerRepository;
  }

  @Override
  public void handle(CommandMessage request, Session session) {
    String seq = request.getSeq();

    try {
      // 解析参数
      String name = request.getParam("name");
      String levelStr = request.getParam("level");

      // 参数验证
      if (name == null || name.trim().isEmpty()) {
        sendErrorResponse(session, "name parameter is required", seq);
        return;
      }

      if (levelStr == null || levelStr.trim().isEmpty()) {
        sendErrorResponse(session, "level parameter is required", seq);
        return;
      }

      int level;
      try {
        level = Integer.parseInt(levelStr);
        if (level < 1) {
          sendErrorResponse(session, "level must be greater than 0", seq);
          return;
        }
      } catch (NumberFormatException e) {
        sendErrorResponse(session, "level must be a valid integer", seq);
        return;
      }

      LOGGER.info("处理 player-save 指令 - 姓名: {}, 等级: {}, seq: {}", name, level, seq);

      // 保存玩家
      Player player = new Player(name.trim(), level);
      Player savedPlayer = playerRepository.save(player);

      // 发送成功响应
      sendSuccessResponse(session, savedPlayer, seq);

      LOGGER.info("player-save 指令处理完成 - 玩家: {}", savedPlayer);

    } catch (Exception e) {
      LOGGER.error("player-save 指令处理异常", e);
      sendErrorResponse(session, "internal_server_error", seq);
    }
  }

  /**
   * 发送成功响应。
   *
   * @param session 会话对象
   * @param player 保存的玩家对象
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
