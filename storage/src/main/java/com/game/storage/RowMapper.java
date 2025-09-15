package com.game.storage;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 结果集行映射器接口。
 *
 * <p>用于将 ResultSet 中的一行数据映射为 Java 对象。
 *
 * <p>使用示例：
 *
 * <pre>{@code
 * RowMapper<User> userMapper = rs -> {
 *     User user = new User();
 *     user.setId(rs.getLong("id"));
 *     user.setName(rs.getString("name"));
 *     user.setEmail(rs.getString("email"));
 *     return user;
 * };
 * }</pre>
 *
 * @param <T> 映射的目标类型
 * @author game-frame
 * @since 1.0.0
 */
@FunctionalInterface
public interface RowMapper<T> {

  /**
   * 将 ResultSet 中的当前行映射为指定类型的对象。
   *
   * <p>注意事项：
   *
   * <ul>
   *   <li>此方法会在 ResultSet.next() 返回 true 后被调用
   *   <li>实现类不应该调用 ResultSet.next() 方法
   *   <li>ResultSet 的生命周期由调用方管理
   * </ul>
   *
   * @param rs 结果集，已定位到当前行
   * @return 映射后的对象
   * @throws SQLException 如果访问结果集数据时发生错误
   */
  T mapRow(ResultSet rs) throws SQLException;
}
