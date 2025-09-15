package com.game.storage.repository;

import com.game.storage.DbException;
import com.game.storage.JdbcTemplate;
import com.game.storage.entity.Player;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于 JDBC 的玩家仓储实现。
 *
 * <p>使用 JdbcTemplate 进行数据库操作，支持 H2 和 MySQL 数据库。
 *
 * @author game-frame
 * @since 1.0.0
 */
public class JdbcPlayerRepository implements PlayerRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(JdbcPlayerRepository.class);

  /** JDBC 模板 */
  private final JdbcTemplate jdbcTemplate;

  /**
   * 构造器。
   *
   * @param jdbcTemplate JDBC 模板
   */
  public JdbcPlayerRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
    initializeTable();
  }

  /** 初始化数据库表结构。 */
  private void initializeTable() {
    try {
      String createTableSql =
          """
          CREATE TABLE IF NOT EXISTS players (
              id BIGINT AUTO_INCREMENT PRIMARY KEY,
              name VARCHAR(100) NOT NULL,
              level INT NOT NULL DEFAULT 1,
              created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
          )
          """;

      jdbcTemplate.update(createTableSql);
      LOGGER.info("玩家表初始化完成");
    } catch (Exception e) {
      LOGGER.error("初始化玩家表失败", e);
      throw new DbException("初始化玩家表失败", e);
    }
  }

  @Override
  public Player save(Player player) {
    if (player == null) {
      throw new IllegalArgumentException("玩家对象不能为空");
    }

    try {
      if (player.getId() == null) {
        // 插入新记录
        return insertPlayer(player);
      } else {
        // 更新现有记录
        return updatePlayer(player);
      }
    } catch (Exception e) {
      LOGGER.error("保存玩家失败: {}", player, e);
      throw new DbException("保存玩家失败", e);
    }
  }

  /**
   * 插入新玩家记录。
   *
   * @param player 玩家对象
   * @return 包含生成 ID 的玩家对象
   */
  private Player insertPlayer(Player player) {
    String insertSql = "INSERT INTO players (name, level) VALUES (?, ?)";
    long generatedId =
        jdbcTemplate.updateAndReturnKey(insertSql, player.getName(), player.getLevel());

    Player savedPlayer = new Player(generatedId, player.getName(), player.getLevel());
    LOGGER.info("插入新玩家成功: {}", savedPlayer);
    return savedPlayer;
  }

  /**
   * 更新现有玩家记录。
   *
   * @param player 玩家对象
   * @return 更新后的玩家对象
   */
  private Player updatePlayer(Player player) {
    String updateSql = "UPDATE players SET name = ?, level = ? WHERE id = ?";
    int updatedRows =
        jdbcTemplate.update(updateSql, player.getName(), player.getLevel(), player.getId());

    if (updatedRows == 0) {
      throw new DbException("玩家不存在，无法更新: ID=" + player.getId());
    }

    LOGGER.info("更新玩家成功: {}", player);
    return player;
  }

  @Override
  public Optional<Player> findById(Long id) {
    if (id == null) {
      return Optional.empty();
    }

    try {
      String selectSql = "SELECT id, name, level FROM players WHERE id = ?";
      Player player =
          jdbcTemplate.queryOne(
              selectSql,
              rs -> new Player(rs.getLong("id"), rs.getString("name"), rs.getInt("level")),
              id);

      return Optional.ofNullable(player);
    } catch (Exception e) {
      LOGGER.error("根据 ID 查找玩家失败: {}", id, e);
      throw new DbException("查找玩家失败", e);
    }
  }

  @Override
  public Optional<Player> findByName(String name) {
    if (name == null || name.trim().isEmpty()) {
      return Optional.empty();
    }

    try {
      String selectSql = "SELECT id, name, level FROM players WHERE name = ?";
      Player player =
          jdbcTemplate.queryOne(
              selectSql,
              rs -> new Player(rs.getLong("id"), rs.getString("name"), rs.getInt("level")),
              name);

      return Optional.ofNullable(player);
    } catch (Exception e) {
      LOGGER.error("根据姓名查找玩家失败: {}", name, e);
      throw new DbException("查找玩家失败", e);
    }
  }

  @Override
  public boolean deleteById(Long id) {
    if (id == null) {
      return false;
    }

    try {
      String deleteSql = "DELETE FROM players WHERE id = ?";
      int deletedRows = jdbcTemplate.update(deleteSql, id);
      boolean deleted = deletedRows > 0;

      if (deleted) {
        LOGGER.info("删除玩家成功: ID={}", id);
      } else {
        LOGGER.warn("玩家不存在，无法删除: ID={}", id);
      }

      return deleted;
    } catch (Exception e) {
      LOGGER.error("删除玩家失败: ID={}", id, e);
      throw new DbException("删除玩家失败", e);
    }
  }
}
