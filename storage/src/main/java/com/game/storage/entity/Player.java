package com.game.storage.entity;

/**
 * 玩家实体类。
 *
 * <p>表示数据库中的玩家信息，包含基础属性如姓名、等级等。
 *
 * @author game-frame
 * @since 1.0.0
 */
public class Player {

  /** 玩家 ID */
  private Long id;

  /** 玩家姓名 */
  private String name;

  /** 玩家等级 */
  private Integer level;

  /** 默认构造器 */
  public Player() {}

  /**
   * 构造器。
   *
   * @param name 玩家姓名
   * @param level 玩家等级
   */
  public Player(String name, Integer level) {
    this.name = name;
    this.level = level;
  }

  /**
   * 完整构造器。
   *
   * @param id 玩家 ID
   * @param name 玩家姓名
   * @param level 玩家等级
   */
  public Player(Long id, String name, Integer level) {
    this.id = id;
    this.name = name;
    this.level = level;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getLevel() {
    return level;
  }

  public void setLevel(Integer level) {
    this.level = level;
  }

  @Override
  public String toString() {
    return String.format("Player{id=%d, name='%s', level=%d}", id, name, level);
  }
}