package com.game.common.component;

/**
 * 组件生命周期接口。 定义了组件的初始化、启动、停止生命周期方法，以及组件装配顺序。 所有可插拔组件都应实现此接口，并通过 SPI 机制进行自动装配。
 *
 * <p>组件生命周期调用顺序：
 * <ol>
 *   <li>init() - 组件初始化，准备资源和配置</li>
 *   <li>start() - 组件启动，开始提供服务</li>
 *   <li>stop() - 组件停止，释放资源和清理</li>
 * </ol>
 *
 * <p>组件装配顺序通过 getOrder() 方法定义，数值越小优先级越高。 初始化和启动按照 order 从小到大执行，停止时按照 order 从大到小执行。
 *
 * @author game-frame
 * @since 1.0.0
 */
public interface Component {

  /**
   * 获取组件装配顺序。 数值越小优先级越高，会优先进行初始化和启动。 停止时按照相反顺序执行（order 大的先停止）。
   *
   * @return 组件装配顺序，建议使用 100 的倍数便于后续插入新组件
   */
  int getOrder();

  /**
   * 组件初始化方法。 在此方法中进行资源分配、配置读取、依赖关系建立等初始化工作。 此方法在组件启动前调用，且整个生命周期中只会调用一次。
   *
   * @throws ComponentException 当初始化过程中发生错误时抛出
   */
  void init() throws ComponentException;

  /**
   * 组件启动方法。 在此方法中启动服务、开始监听端口、启动后台线程等。 此方法在组件初始化完成后调用，标志着组件开始正式提供服务。
   *
   * @throws ComponentException 当启动过程中发生错误时抛出
   */
  void start() throws ComponentException;

  /**
   * 组件停止方法。 在此方法中停止服务、关闭连接、释放资源、清理缓存等。 此方法在应用关闭时调用，确保组件优雅地停止服务。
   *
   * @throws ComponentException 当停止过程中发生错误时抛出
   */
  void stop() throws ComponentException;
}
