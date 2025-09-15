package com.game.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 轻量级 JDBC 辅助工具。
 *
 * <p>提供常用的数据库操作方法，简化 JDBC 操作并统一异常处理。
 *
 * <p>主要功能：
 *
 * <ul>
 *   <li>查询操作：query、queryOne
 *   <li>更新操作：update
 *   <li>批量更新：batchUpdate
 *   <li>统一异常处理
 *   <li>资源自动管理
 * </ul>
 *
 * <p>使用示例：
 *
 * <pre>{@code
 * JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
 *
 * // 查询操作
 * List<User> users = jdbcTemplate.query(
 *     "SELECT * FROM users WHERE age > ?",
 *     rs -> {
 *         User user = new User();
 *         user.setId(rs.getLong("id"));
 *         user.setName(rs.getString("name"));
 *         return user;
 *     },
 *     18
 * );
 *
 * // 更新操作
 * int rows = jdbcTemplate.update("UPDATE users SET name = ? WHERE id = ?", "新名称", 1L);
 * }</pre>
 *
 * @author game-frame
 * @since 1.0.0
 */
public class JdbcTemplate {

  private static final Logger LOGGER = LoggerFactory.getLogger(JdbcTemplate.class);

  private final DataSource dataSource;

  /**
   * 构造 JDBC 模板。
   *
   * @param dataSource 数据源
   */
  public JdbcTemplate(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  /**
   * 执行查询操作，返回结果列表。
   *
   * @param <T> 结果类型
   * @param sql SQL 语句
   * @param rowMapper 行映射器
   * @param args SQL 参数
   * @return 查询结果列表
   * @throws DbException 如果查询失败
   */
  public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
    List<T> result = new ArrayList<>();

    try (Connection connection = dataSource.getConnection();
        PreparedStatement stmt = connection.prepareStatement(sql)) {

      setParameters(stmt, args);

      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          result.add(rowMapper.mapRow(rs));
        }
      }

      LOGGER.debug("查询执行成功，返回 {} 条记录：{}", result.size(), sql);
      return result;

    } catch (SQLException e) {
      LOGGER.error("查询执行失败：{}", sql, e);
      throw new DbException("查询执行失败：" + sql, e);
    }
  }

  /**
   * 执行查询操作，返回单个结果。
   *
   * @param <T> 结果类型
   * @param sql SQL 语句
   * @param rowMapper 行映射器
   * @param args SQL 参数
   * @return 查询结果，如果没有结果则返回 null
   * @throws DbException 如果查询失败或结果超过一条
   */
  public <T> T queryOne(String sql, RowMapper<T> rowMapper, Object... args) {
    List<T> result = query(sql, rowMapper, args);

    if (result.isEmpty()) {
      return null;
    }

    if (result.size() > 1) {
      throw new DbException("期望查询结果为单条记录，但实际返回 " + result.size() + " 条记录：" + sql);
    }

    return result.get(0);
  }

  /**
   * 执行更新操作（INSERT、UPDATE、DELETE）。
   *
   * @param sql SQL 语句
   * @param args SQL 参数
   * @return 受影响的行数
   * @throws DbException 如果更新失败
   */
  public int update(String sql, Object... args) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement stmt = connection.prepareStatement(sql)) {

      setParameters(stmt, args);
      int rows = stmt.executeUpdate();

      LOGGER.debug("更新执行成功，影响 {} 行：{}", rows, sql);
      return rows;

    } catch (SQLException e) {
      LOGGER.error("更新执行失败：{}", sql, e);
      throw new DbException("更新执行失败：" + sql, e);
    }
  }

  /**
   * 执行批量更新操作。
   *
   * @param sql SQL 语句
   * @param batchArgs 批量参数，每个元素为一组参数
   * @return 每批次受影响的行数数组
   * @throws DbException 如果批量更新失败
   */
  public int[] batchUpdate(String sql, List<Object[]> batchArgs) {
    if (batchArgs == null || batchArgs.isEmpty()) {
      return new int[0];
    }

    try (Connection connection = dataSource.getConnection();
        PreparedStatement stmt = connection.prepareStatement(sql)) {

      for (Object[] args : batchArgs) {
        setParameters(stmt, args);
        stmt.addBatch();
      }

      int[] result = stmt.executeBatch();
      LOGGER.debug("批量更新执行成功，批量大小：{}，SQL：{}", batchArgs.size(), sql);
      return result;

    } catch (SQLException e) {
      LOGGER.error("批量更新执行失败：{}", sql, e);
      throw new DbException("批量更新执行失败：" + sql, e);
    }
  }

  /**
   * 执行更新操作并返回生成的主键。
   *
   * @param sql SQL 语句
   * @param args SQL 参数
   * @return 生成的主键值
   * @throws DbException 如果更新失败或无法获取主键
   */
  public long updateAndReturnKey(String sql, Object... args) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement stmt =
            connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      setParameters(stmt, args);
      int rows = stmt.executeUpdate();

      if (rows == 0) {
        throw new DbException("更新失败，没有行被影响：" + sql);
      }

      try (ResultSet rs = stmt.getGeneratedKeys()) {
        if (rs.next()) {
          long key = rs.getLong(1);
          LOGGER.debug("更新执行成功，影响 {} 行，生成主键：{}，SQL：{}", rows, key, sql);
          return key;
        } else {
          throw new DbException("无法获取生成的主键：" + sql);
        }
      }

    } catch (SQLException e) {
      LOGGER.error("更新执行失败：{}", sql, e);
      throw new DbException("更新执行失败：" + sql, e);
    }
  }

  /**
   * 设置 PreparedStatement 的参数。
   *
   * @param stmt PreparedStatement 实例
   * @param args 参数数组
   * @throws SQLException 如果设置参数失败
   */
  private void setParameters(PreparedStatement stmt, Object... args) throws SQLException {
    if (args != null) {
      for (int i = 0; i < args.length; i++) {
        stmt.setObject(i + 1, args[i]);
      }
    }
  }
}
