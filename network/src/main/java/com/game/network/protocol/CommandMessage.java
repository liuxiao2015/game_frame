package com.game.network.protocol;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 指令消息对象，不可变设计。
 *
 * <p>表示解析后的行文本协议消息，格式：cmd arg1=val1 arg2=val2
 *
 * <p>主要特性：
 *
 * <ul>
 *   <li>不可变对象设计，线程安全
 *   <li>支持可选的 seq 参数用于客户端关联请求响应
 *   <li>参数键值对存储，值不含空格
 *   <li>提供便捷的参数访问方法
 * </ul>
 *
 * <p>使用示例：
 *
 * <pre>{@code
 * // 解析行文本消息
 * CommandMessage cmd = CommandMessage.parse("echo msg=hello seq=1");
 *
 * // 访问命令和参数
 * String command = cmd.getCommand(); // "echo"
 * String message = cmd.getParam("msg"); // "hello"
 * String seq = cmd.getParam("seq"); // "1"
 * }</pre>
 *
 * @author game-frame
 * @since 1.0.0
 */
public final class CommandMessage {

  /** 指令名 */
  private final String command;

  /** 参数映射表（不可变） */
  private final Map<String, String> params;

  /**
   * 私有构造器。
   *
   * @param command 指令名
   * @param params 参数映射表
   */
  private CommandMessage(String command, Map<String, String> params) {
    this.command = command;
    this.params = Collections.unmodifiableMap(new HashMap<>(params));
  }

  /**
   * 从行文本解析指令消息。
   *
   * <p>格式：cmd [k=v]...
   *
   * <p>示例：
   *
   * <ul>
   *   <li>echo msg=hello seq=1
   *   <li>time seq=2
   *   <li>sum a=10 b=20 seq=3
   *   <li>ping
   * </ul>
   *
   * @param line 行文本
   * @return 解析后的指令消息
   * @throws IllegalArgumentException 如果格式不正确
   */
  public static CommandMessage parse(String line) {
    if (line == null || line.trim().isEmpty()) {
      throw new IllegalArgumentException("命令行不能为空");
    }

    String trimmedLine = line.trim();
    String[] parts = trimmedLine.split("\\s+");

    if (parts.length == 0) {
      throw new IllegalArgumentException("命令行格式错误：" + line);
    }

    String command = parts[0];
    if (!isValidCommand(command)) {
      throw new IllegalArgumentException("无效的命令名：" + command);
    }

    Map<String, String> params = new HashMap<>();

    // 解析参数：k=v 格式
    for (int i = 1; i < parts.length; i++) {
      String part = parts[i];
      int equalIndex = part.indexOf('=');

      if (equalIndex <= 0 || equalIndex == part.length() - 1) {
        throw new IllegalArgumentException("参数格式错误：" + part + "，应为 key=value 格式");
      }

      String key = part.substring(0, equalIndex);
      String value = part.substring(equalIndex + 1);

      if (!isValidParamKey(key)) {
        throw new IllegalArgumentException("无效的参数名：" + key);
      }

      params.put(key, value);
    }

    return new CommandMessage(command, params);
  }

  /**
   * 创建指令消息构建器。
   *
   * @param command 指令名
   * @return 构建器实例
   */
  public static Builder builder(String command) {
    return new Builder(command);
  }

  /**
   * 检查命令名是否有效。
   *
   * @param command 命令名
   * @return 是否有效
   */
  private static boolean isValidCommand(String command) {
    if (command == null || command.isEmpty()) {
      return false;
    }
    // 命令名只能包含小写字母、数字、短横线和下划线
    return command.matches("[a-z0-9_-]+");
  }

  /**
   * 检查参数名是否有效。
   *
   * @param key 参数名
   * @return 是否有效
   */
  private static boolean isValidParamKey(String key) {
    if (key == null || key.isEmpty()) {
      return false;
    }
    // 参数名只能包含字母、数字、短横线和下划线
    return key.matches("[a-zA-Z0-9_-]+");
  }

  /**
   * 获取指令名。
   *
   * @return 指令名
   */
  public String getCommand() {
    return command;
  }

  /**
   * 获取所有参数。
   *
   * @return 参数映射表（不可变）
   */
  public Map<String, String> getParams() {
    return params;
  }

  /**
   * 获取指定参数值。
   *
   * @param key 参数名
   * @return 参数值，如果不存在则返回 null
   */
  public String getParam(String key) {
    return params.get(key);
  }

  /**
   * 获取指定参数值，支持默认值。
   *
   * @param key 参数名
   * @param defaultValue 默认值
   * @return 参数值，如果不存在则返回默认值
   */
  public String getParam(String key, String defaultValue) {
    return params.getOrDefault(key, defaultValue);
  }

  /**
   * 检查是否包含指定参数。
   *
   * @param key 参数名
   * @return 是否包含
   */
  public boolean hasParam(String key) {
    return params.containsKey(key);
  }

  /**
   * 获取序列号（seq 参数）。
   *
   * @return 序列号，如果不存在则返回 null
   */
  public String getSeq() {
    return getParam("seq");
  }

  /**
   * 转换为行文本格式。
   *
   * @return 行文本
   */
  public String toLine() {
    StringBuilder sb = new StringBuilder(command);

    for (Map.Entry<String, String> entry : params.entrySet()) {
      sb.append(' ').append(entry.getKey()).append('=').append(entry.getValue());
    }

    return sb.toString();
  }

  @Override
  public String toString() {
    return "CommandMessage{command='" + command + "', params=" + params + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    CommandMessage that = (CommandMessage) o;
    return command.equals(that.command) && params.equals(that.params);
  }

  @Override
  public int hashCode() {
    return command.hashCode() * 31 + params.hashCode();
  }

  /** 指令消息构建器。 */
  public static final class Builder {
    private final String command;
    private final Map<String, String> params = new HashMap<>();

    private Builder(String command) {
      if (!isValidCommand(command)) {
        throw new IllegalArgumentException("无效的命令名：" + command);
      }
      this.command = command;
    }

    /**
     * 添加参数。
     *
     * @param key 参数名
     * @param value 参数值
     * @return 构建器实例
     */
    public Builder param(String key, String value) {
      if (!isValidParamKey(key)) {
        throw new IllegalArgumentException("无效的参数名：" + key);
      }
      if (value == null) {
        throw new IllegalArgumentException("参数值不能为 null");
      }
      if (value.contains(" ")) {
        throw new IllegalArgumentException("参数值不能包含空格：" + value);
      }
      params.put(key, value);
      return this;
    }

    /**
     * 设置序列号。
     *
     * @param seq 序列号
     * @return 构建器实例
     */
    public Builder seq(String seq) {
      return param("seq", seq);
    }

    /**
     * 构建指令消息。
     *
     * @return 指令消息实例
     */
    public CommandMessage build() {
      return new CommandMessage(command, params);
    }
  }
}
