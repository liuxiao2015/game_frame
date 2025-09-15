package com.game.storage.repository;

import com.game.storage.entity.Player;
import java.util.Optional;

/**
 * 玩家仓储接口。
 *
 * <p>定义玩家数据访问的标准接口，支持基础的 CRUD 操作。
 *
 * @author game-frame
 * @since 1.0.0
 */
public interface PlayerRepository {

  /**
   * 保存玩家信息。
   *
   * <p>如果玩家 ID 为空，则插入新记录并返回生成的 ID； 如果玩家 ID 不为空，则更新现有记录。
   *
   * @param player 玩家对象
   * @return 保存后的玩家对象（包含生成的 ID）
   * @throws com.game.storage.DbException 数据库操作异常
   */
  Player save(Player player);

  /**
   * 根据 ID 查找玩家。
   *
   * @param id 玩家 ID
   * @return 玩家对象，如果不存在则返回 Optional.empty()
   * @throws com.game.storage.DbException 数据库操作异常
   */
  Optional<Player> findById(Long id);

  /**
   * 根据姓名查找玩家。
   *
   * @param name 玩家姓名
   * @return 玩家对象，如果不存在则返回 Optional.empty()
   * @throws com.game.storage.DbException 数据库操作异常
   */
  Optional<Player> findByName(String name);

  /**
   * 删除玩家。
   *
   * @param id 玩家 ID
   * @return 是否删除成功
   * @throws com.game.storage.DbException 数据库操作异常
   */
  boolean deleteById(Long id);
}
