/**
 * 网络模块（network）：
 *
 * <p>基于 Netty 的高性能 TCP 网络通信模块，提供以下功能：
 *
 * <ul>
 *   <li>NettyServerComponent - Netty TCP 服务器组件，实现 Component 接口，支持 SPI 自动装配
 *   <li>NettyServerInitializer - 通道管道初始化器，配置行分隔文本协议处理链
 *   <li>EchoHandler - Echo 消息处理器，演示简单的消息回显功能
 * </ul>
 *
 * <p>主要特性：
 *
 * <ul>
 *   <li>支持"行分隔"文本协议，便于 telnet/nc 调试
 *   <li>每连接自动生成 traceId 并注入 MDC
 *   <li>完整的连接与消息级日志记录
 *   <li>优雅关闭机制，确保资源正确释放
 *   <li>可配置的服务器端口（默认 7001）
 * </ul>
 *
 * <p>使用方式：
 *
 * <pre>{@code
 * // 通过 SPI 自动装配（推荐）
 * ComponentManager manager = new ComponentManager();
 * manager.loadFromSpi();
 * manager.initAll();
 * manager.startAll();
 *
 * // 手动注册
 * ComponentManager manager = new ComponentManager();
 * manager.register(new NettyServerComponent());
 * manager.initAll();
 * manager.startAll();
 * }</pre>
 *
 * <p>测试验证：
 *
 * <pre>{@code
 * # 使用 telnet 连接测试
 * telnet localhost 7001
 *
 * # 使用 nc 连接测试
 * nc localhost 7001
 *
 * # 发送消息测试 Echo 功能
 * > hello world
 * < Echo: hello world
 * }</pre>
 *
 * @author game-frame
 * @since 1.0.0
 */
package com.game.network;
