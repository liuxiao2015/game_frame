package com.game.common.observability;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 轻量级 JVM 指标上报器。
 *
 * <ul>
 *   <li>周期性记录堆使用与线程数，便于初步观测。
 *   <li>无第三方依赖，后续可替换为 Micrometer/Prometheus。
 * </ul>
 */
public final class MetricsReporter {
  private static final Logger log = LoggerFactory.getLogger(MetricsReporter.class);

  private final long periodSeconds;
  private volatile ScheduledExecutorService scheduler;

  public MetricsReporter(Duration period) {
    Objects.requireNonNull(period, "period");
    long seconds = Math.max(5, period.toSeconds());
    this.periodSeconds = seconds;
  }

  /** 启动定时采集. */
  public synchronized void start() {
    if (scheduler != null) {
      return;
    }
    scheduler =
        Executors.newSingleThreadScheduledExecutor(
            r -> {
              Thread t = new Thread(r, "metrics-reporter");
              t.setDaemon(true);
              return t;
            });
    log.info("MetricsReporter starting with period={}s", periodSeconds);
    scheduler.scheduleAtFixedRate(this::reportOnce, periodSeconds, periodSeconds, TimeUnit.SECONDS);
  }

  /** 停止采集. */
  public synchronized void stop() {
    if (scheduler == null) {
      return;
    }
    log.info("MetricsReporter stopping");
    scheduler.shutdownNow();
    scheduler = null;
  }

  private void reportOnce() {
    try {
      MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
      MemoryUsage heap = memBean.getHeapMemoryUsage();
      ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
      long usedMb = heap.getUsed() / (1024 * 1024);
      long committedMb = heap.getCommitted() / (1024 * 1024);
      long maxMb = heap.getMax() / (1024 * 1024);
      int threadCount = threadBean.getThreadCount();
      log.info(
          "jvm.metrics heapUsedMB={} heapCommittedMB={} heapMaxMB={} threadCount={}",
          usedMb,
          committedMb,
          maxMb,
          threadCount);
    } catch (Throwable e) {
      log.warn("MetricsReporter report failed", e);
    }
  }
}
