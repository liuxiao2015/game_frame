package com.game.common.observability;

import java.util.UUID;
import org.slf4j.MDC;

/** Trace 上下文工具类：用于在日志中注入与获取 traceId. 使用 SLF4J 的 MDC 机制，在同一线程内自动透传. */
public final class TraceContext {
  public static final String TRACE_ID_KEY = "traceId";

  private TraceContext() {
    // 工具类不允许实例化.
  }

  /**
   * 生成一个短格式的 traceId.
   *
   * @return 短 traceId（去除连字符的小写 UUID 前 16 位）
   */
  public static String generateTraceId() {
    String uuid = UUID.randomUUID().toString().replace("-", "").toLowerCase();
    return uuid.substring(0, 16);
  }

  /**
   * 将 traceId 放入 MDC.
   *
   * @param traceId 追踪标识
   */
  public static void put(String traceId) {
    if (traceId != null && !traceId.isEmpty()) {
      MDC.put(TRACE_ID_KEY, traceId);
    }
  }

  /** 获取当前线程中的 traceId. */
  public static String get() {
    return MDC.get(TRACE_ID_KEY);
  }

  /** 清理 MDC 中的 traceId，防止线程复用导致的污染. */
  public static void clear() {
    MDC.remove(TRACE_ID_KEY);
  }
}
