package com.game.common.component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 组件管理器。 负责组件的注册、SPI 自动装配、生命周期管理等核心功能。 支持按照组件 order 进行有序的初始化、启动和停止操作。
 *
 * <p>主要功能： - 手动注册组件 register() - SPI 自动装配 loadFromSpi() - 批量初始化 initAll() - 批量启动 startAll() - 批量停止
 * stopAll()
 *
 * <p>组件执行顺序： - 初始化和启动：按照 getOrder() 从小到大执行 - 停止：按照 getOrder() 从大到小执行（逆序）
 *
 * @author game-frame
 * @since 1.0.0
 */
public final class ComponentManager {

  private static final Logger LOGGER = Logger.getLogger(ComponentManager.class.getName());

  /** 已注册的组件列表 */
  private final List<Component> components = new ArrayList<>();

  /** 组件是否已初始化标志 */
  private boolean initialized = false;

  /** 组件是否已启动标志 */
  private boolean started = false;

  /**
   * 手动注册组件。 将指定组件添加到管理器中，后续可通过 initAll/startAll 等方法统一管理。
   *
   * @param component 要注册的组件实例，不能为 null
   * @throws ComponentException 当组件为 null 或已启动后尝试注册时抛出
   */
  public void register(Component component) {
    if (component == null) {
      throw new ComponentException("注册的组件不能为 null");
    }
    if (started) {
      throw new ComponentException("组件已启动，无法再注册新组件");
    }

    components.add(component);
    LOGGER.info(
        String.format(
            "已注册组件: %s, order: %d", component.getClass().getSimpleName(), component.getOrder()));
  }

  /**
   * 通过 SPI 机制自动装配组件。 使用 Java ServiceLoader 扫描 META-INF/services 目录下的组件配置文件， 自动实例化并注册所有实现了 Component
   * 接口的组件。
   *
   * @throws ComponentException 当 SPI 加载过程中发生错误时抛出
   */
  public void loadFromSpi() {
    if (started) {
      throw new ComponentException("组件已启动，无法再加载 SPI 组件");
    }

    try {
      ServiceLoader<Component> serviceLoader = ServiceLoader.load(Component.class);
      int loadedCount = 0;

      for (Component component : serviceLoader) {
        register(component);
        loadedCount++;
      }

      LOGGER.info(String.format("通过 SPI 成功加载 %d 个组件", loadedCount));
    } catch (Exception e) {
      throw new ComponentException("SPI 组件加载失败", e);
    }
  }

  /**
   * 初始化所有已注册的组件。 按照组件的 getOrder() 返回值从小到大的顺序依次调用 init() 方法。 如果任何组件初始化失败，整个初始化过程将中断。
   *
   * @throws ComponentException 当任何组件初始化失败时抛出
   */
  public void initAll() {
    if (initialized) {
      LOGGER.warning("组件已初始化，跳过重复初始化");
      return;
    }

    // 按照 order 从小到大排序
    List<Component> sortedComponents = new ArrayList<>(components);
    sortedComponents.sort(Comparator.comparingInt(Component::getOrder));

    LOGGER.info(String.format("开始初始化 %d 个组件", sortedComponents.size()));

    for (Component component : sortedComponents) {
      try {
        LOGGER.info(
            String.format(
                "正在初始化组件: %s (order: %d)",
                component.getClass().getSimpleName(), component.getOrder()));
        component.init();
        LOGGER.info(String.format("组件初始化完成: %s", component.getClass().getSimpleName()));
      } catch (Exception e) {
        throw new ComponentException(
            String.format("组件初始化失败: %s", component.getClass().getSimpleName()), e);
      }
    }

    initialized = true;
    LOGGER.info("所有组件初始化完成");
  }

  /**
   * 启动所有已初始化的组件。 按照组件的 getOrder() 返回值从小到大的顺序依次调用 start() 方法。 启动前会自动检查组件是否已初始化，未初始化则先执行初始化。
   *
   * @throws ComponentException 当任何组件启动失败时抛出
   */
  public void startAll() {
    if (!initialized) {
      LOGGER.info("组件未初始化，先执行初始化");
      initAll();
    }

    if (started) {
      LOGGER.warning("组件已启动，跳过重复启动");
      return;
    }

    // 按照 order 从小到大排序
    List<Component> sortedComponents = new ArrayList<>(components);
    sortedComponents.sort(Comparator.comparingInt(Component::getOrder));

    LOGGER.info(String.format("开始启动 %d 个组件", sortedComponents.size()));

    for (Component component : sortedComponents) {
      try {
        LOGGER.info(
            String.format(
                "正在启动组件: %s (order: %d)",
                component.getClass().getSimpleName(), component.getOrder()));
        component.start();
        LOGGER.info(String.format("组件启动完成: %s", component.getClass().getSimpleName()));
      } catch (Exception e) {
        throw new ComponentException(
            String.format("组件启动失败: %s", component.getClass().getSimpleName()), e);
      }
    }

    started = true;
    LOGGER.info("所有组件启动完成");
  }

  /**
   * 停止所有已启动的组件。 按照组件的 getOrder() 返回值从大到小的顺序（逆序）依次调用 stop() 方法。
   * 即使某个组件停止失败，也会继续尝试停止其他组件，确保尽可能多的资源被释放。
   */
  public void stopAll() {
    if (!started) {
      LOGGER.warning("组件未启动，无需停止");
      return;
    }

    // 按照 order 从大到小排序（逆序停止）
    List<Component> sortedComponents = new ArrayList<>(components);
    sortedComponents.sort(Comparator.comparingInt(Component::getOrder).reversed());

    LOGGER.info(String.format("开始停止 %d 个组件", sortedComponents.size()));

    for (Component component : sortedComponents) {
      try {
        LOGGER.info(
            String.format(
                "正在停止组件: %s (order: %d)",
                component.getClass().getSimpleName(), component.getOrder()));
        component.stop();
        LOGGER.info(String.format("组件停止完成: %s", component.getClass().getSimpleName()));
      } catch (Exception e) {
        // 停止过程中出现异常不中断流程，继续停止其他组件
        LOGGER.log(
            Level.WARNING, String.format("组件停止失败: %s", component.getClass().getSimpleName()), e);
      }
    }

    started = false;
    initialized = false;
    LOGGER.info("所有组件停止完成");
  }

  /**
   * 获取已注册组件的数量。
   *
   * @return 已注册组件的数量
   */
  public int getComponentCount() {
    return components.size();
  }

  /**
   * 检查组件是否已初始化。
   *
   * @return 如果组件已初始化返回 true，否则返回 false
   */
  public boolean isInitialized() {
    return initialized;
  }

  /**
   * 检查组件是否已启动。
   *
   * @return 如果组件已启动返回 true，否则返回 false
   */
  public boolean isStarted() {
    return started;
  }
}
