/**
 * 网络模块（network）：
 *
 * <p>基于 Netty 的高性能 TCP 网络通信模块，提供轻量级指令路由框架，支持以下功能：
 *
 * <ul>
 *   <li>NettyServerComponent - Netty TCP 服务器组件，实现 Component 接口，支持 SPI 自动装配
 *   <li>NettyServerInitializer - 通道管道初始化器，配置指令协议处理链
 *   <li>CommandProtocolHandler - 指令协议处理器，支持指令路由和会话管理
 *   <li>CommandMessage - 不可变指令消息对象，支持行文本协议解析
 *   <li>Session - 会话管理类，提供 traceId 注入和属性存储
 *   <li>CommandDispatcher - 指令分发器，支持业务线程池和路由功能
 *   <li>CommandHandler - 指令处理器接口，包含内置指令实现（echo、time、sum、ping）
 * </ul>
 *
 * <p>主要特性：
 *
 * <ul>
 *   <li>支持"行文本"指令协议，格式：cmd k=v k=v...
 *   <li>指令路由框架，支持动态注册处理器
 *   <li>每连接自动生成 traceId 并注入 MDC
 *   <li>会话级上下文管理和属性存储
 *   <li>业务线程池，IO 线程与处理线程解耦
 *   <li>心跳检测与空闲超时处理（ping/pong）
 *   <li>完整的连接与指令级日志记录
 *   <li>优雅关闭机制，确保资源正确释放
 *   <li>可配置的服务器端口和线程池参数
 * </ul>
 *
 * <p>指令协议示例：
 *
 * <pre>{@code
 * # 发送指令
 * echo msg=hello seq=1
 * time seq=2
 * sum a=10 b=20 seq=3
 * ping seq=4
 * 
 * # 接收响应
 * echo msg=hello seq=1
 * time timestamp=1703123456789 datetime=2023-12-20T15:30:56 seq=2
 * sum a=10 b=20 result=30 seq=3
 * pong seq=4
 * }</pre>
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
 * # 使用 LauncherProtocolDemo 启动服务器
 * java -cp ... com.game.launcher.LauncherProtocolDemo
 *
 * # 使用 telnet 连接测试
 * telnet localhost 7001
 *
 * # 使用 nc 连接测试
 * nc localhost 7001
 *
 * # 发送指令测试功能
 * > echo msg=hello seq=1
 * < echo msg=hello seq=1
 * > time seq=2
 * < time timestamp=1703123456789 datetime=2023-12-20T15:30:56 seq=2
 * }</pre>
 *
 * @author game-frame
 * @since 1.0.0
 */
package com.game.network;
