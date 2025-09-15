package com.game.network.handler;

import com.game.network.protocol.CommandMessage;
import com.game.network.session.Session;

/**
 * 指令处理器接口。
 *
 * <p>定义指令处理的标准接口，实现类需要处理特定的指令并返回响应。
 *
 * <p>主要特性：
 *
 * <ul>
 *   <li>支持会话级上下文传递
 *   <li>支持异步处理
 *   <li>统一的错误处理机制
 * </ul>
 *
 * <p>使用示例：
 *
 * <pre>{@code
 * public class EchoCommandHandler implements CommandHandler {
 *     @Override
 *     public void handle(CommandMessage request, Session session) {
 *         String message = request.getParam("msg", "");
 *         CommandMessage response = CommandMessage.builder("echo")
 *             .param("msg", message)
 *             .seq(request.getSeq())
 *             .build();
 *         session.sendMessage(response);
 *     }
 * }
 * }</pre>
 *
 * @author game-frame
 * @since 1.0.0
 */
public interface CommandHandler {

  /**
   * 处理指令请求。
   *
   * <p>实现类应该：
   *
   * <ul>
   *   <li>解析请求参数
   *   <li>执行业务逻辑
   *   <li>生成响应消息
   *   <li>通过 session 发送响应
   *   <li>处理异常情况
   * </ul>
   *
   * <p>注意事项：
   *
   * <ul>
   *   <li>此方法会在业务线程池中执行，不要执行阻塞操作
   *   <li>如果需要保持请求响应关联，请使用 request.getSeq() 获取序列号并在响应中回传
   *   <li>异常应该被捕获并转换为错误响应，避免影响其他请求
   * </ul>
   *
   * @param request 请求消息
   * @param session 会话对象
   */
  void handle(CommandMessage request, Session session);
}