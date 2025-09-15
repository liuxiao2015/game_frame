package com.game.network.session;

import com.game.common.observability.TraceContext;
import com.game.network.protocol.CommandMessage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 会话管理类，基于 Netty Channel 实现。
 *
 * <p>提供以下功能：
 *
 * <ul>
 *   <li>TraceId 注入与获取，支持跨线程传递
 *   <li>远端地址获取
 *   <li>属性存取，支持会话级数据存储
 *   <li>消息发送，支持 CommandMessage 和字符串
 *   <li>连接状态检查
 * </ul>
 *
 * <p>使用示例：
 *
 * <pre>{@code
 * // 创建会话
 * Session session = new Session(channel);
 * 
 * // 获取会话信息
 * String traceId = session.getTraceId();
 * String remoteAddress = session.getRemoteAddress();
 * 
 * // 存取属性
 * session.setAttribute("userId", "12345");
 * String userId = session.getAttribute("userId");
 * 
 * // 发送消息
 * CommandMessage response = CommandMessage.builder("echo")
 *     .param("msg", "hello")
 *     .seq("1")
 *     .build();
 * session.sendMessage(response);
 * }</pre>
 *
 * @author game-frame
 * @since 1.0.0
 */
public final class Session {

  private static final Logger LOGGER = LoggerFactory.getLogger(Session.class);

  /** 会话属性键：traceId */
  private static final String ATTR_TRACE_ID = "session.traceId";

  /** 底层 Netty Channel */
  private final Channel channel;

  /** 会话属性存储 */
  private final ConcurrentMap<String, Object> attributes = new ConcurrentHashMap<>();

  /** 会话创建时间 */
  private final long createTime;

  /**
   * 构造会话实例。
   *
   * @param channel Netty Channel
   */
  public Session(Channel channel) {
    if (channel == null) {
      throw new IllegalArgumentException("Channel 不能为 null");
    }
    this.channel = channel;
    this.createTime = System.currentTimeMillis();

    // 初始化 traceId
    initializeTraceId();

    LOGGER.debug("会话已创建 - 远程地址: {}, traceId: {}", getRemoteAddress(), getTraceId());
  }

  /**
   * 初始化 traceId。
   */
  private void initializeTraceId() {
    String traceId = TraceContext.generateTraceId();
    setAttribute(ATTR_TRACE_ID, traceId);
    // 同时设置到 MDC 中
    TraceContext.put(traceId);
  }

  /**
   * 获取会话的 traceId。
   *
   * @return traceId
   */
  public String getTraceId() {
    return (String) getAttribute(ATTR_TRACE_ID);
  }

  /**
   * 确保当前线程的 MDC 中包含会话的 traceId。
   *
   * <p>在跨线程处理时调用，确保日志中包含正确的 traceId。
   */
  public void ensureTraceId() {
    String traceId = getTraceId();
    if (traceId != null) {
      TraceContext.put(traceId);
    }
  }

  /**
   * 获取远程地址。
   *
   * @return 远程地址字符串，格式：ip:port
   */
  public String getRemoteAddress() {
    InetSocketAddress remoteAddress = (InetSocketAddress) channel.remoteAddress();
    if (remoteAddress == null) {
      return "unknown";
    }
    return remoteAddress.getAddress().getHostAddress() + ":" + remoteAddress.getPort();
  }

  /**
   * 获取远程 IP 地址。
   *
   * @return IP 地址
   */
  public String getRemoteIp() {
    InetSocketAddress remoteAddress = (InetSocketAddress) channel.remoteAddress();
    if (remoteAddress == null) {
      return "unknown";
    }
    return remoteAddress.getAddress().getHostAddress();
  }

  /**
   * 获取远程端口。
   *
   * @return 端口号
   */
  public int getRemotePort() {
    InetSocketAddress remoteAddress = (InetSocketAddress) channel.remoteAddress();
    if (remoteAddress == null) {
      return -1;
    }
    return remoteAddress.getPort();
  }

  /**
   * 设置会话属性。
   *
   * @param key 属性键
   * @param value 属性值
   */
  public void setAttribute(String key, Object value) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("属性键不能为空");
    }
    if (value == null) {
      attributes.remove(key);
    } else {
      attributes.put(key, value);
    }
  }

  /**
   * 获取会话属性。
   *
   * @param key 属性键
   * @param <T> 属性值类型
   * @return 属性值，如果不存在则返回 null
   */
  @SuppressWarnings("unchecked")
  public <T> T getAttribute(String key) {
    if (key == null || key.isEmpty()) {
      return null;
    }
    return (T) attributes.get(key);
  }

  /**
   * 获取会话属性，支持默认值。
   *
   * @param key 属性键
   * @param defaultValue 默认值
   * @param <T> 属性值类型
   * @return 属性值，如果不存在则返回默认值
   */
  @SuppressWarnings("unchecked")
  public <T> T getAttribute(String key, T defaultValue) {
    T value = getAttribute(key);
    return value != null ? value : defaultValue;
  }

  /**
   * 移除会话属性。
   *
   * @param key 属性键
   * @param <T> 属性值类型
   * @return 被移除的属性值，如果不存在则返回 null
   */
  @SuppressWarnings("unchecked")
  public <T> T removeAttribute(String key) {
    if (key == null || key.isEmpty()) {
      return null;
    }
    return (T) attributes.remove(key);
  }

  /**
   * 检查是否包含指定属性。
   *
   * @param key 属性键
   * @return 是否包含
   */
  public boolean hasAttribute(String key) {
    return key != null && !key.isEmpty() && attributes.containsKey(key);
  }

  /**
   * 发送命令消息。
   *
   * @param message 命令消息
   * @return ChannelFuture
   */
  public ChannelFuture sendMessage(CommandMessage message) {
    if (message == null) {
      throw new IllegalArgumentException("消息不能为 null");
    }
    return sendText(message.toLine());
  }

  /**
   * 发送文本消息。
   *
   * @param text 文本内容（会自动添加换行符）
   * @return ChannelFuture
   */
  public ChannelFuture sendText(String text) {
    if (text == null) {
      throw new IllegalArgumentException("文本内容不能为 null");
    }

    if (!isActive()) {
      LOGGER.warn("尝试向已关闭的会话发送消息: {}", text);
      return channel.newFailedFuture(new IllegalStateException("会话已关闭"));
    }

    // 确保以换行符结尾
    String message = text.endsWith("\n") ? text : text + "\n";
    return channel.writeAndFlush(message);
  }

  /**
   * 检查连接是否活跃。
   *
   * @return 是否活跃
   */
  public boolean isActive() {
    return channel.isActive();
  }

  /**
   * 关闭会话连接。
   *
   * @return ChannelFuture
   */
  public ChannelFuture close() {
    LOGGER.debug("会话关闭 - 远程地址: {}, traceId: {}", getRemoteAddress(), getTraceId());
    return channel.close();
  }

  /**
   * 获取会话创建时间。
   *
   * @return 创建时间戳（毫秒）
   */
  public long getCreateTime() {
    return createTime;
  }

  /**
   * 获取会话存活时间。
   *
   * @return 存活时间（毫秒）
   */
  public long getUptime() {
    return System.currentTimeMillis() - createTime;
  }

  /**
   * 获取底层 Channel。
   *
   * <p>仅在必要时使用，推荐使用 Session 提供的方法。
   *
   * @return Netty Channel
   */
  public Channel getChannel() {
    return channel;
  }

  @Override
  public String toString() {
    return "Session{"
        + "remoteAddress='"
        + getRemoteAddress()
        + '\''
        + ", traceId='"
        + getTraceId()
        + '\''
        + ", active="
        + isActive()
        + ", uptime="
        + getUptime()
        + "ms}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Session session = (Session) o;
    return channel.equals(session.channel);
  }

  @Override
  public int hashCode() {
    return channel.hashCode();
  }
}