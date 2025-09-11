# game-frame

> 生产可用的 Java 17 游戏服务器框架（多模块 Maven 工程骨架）。

## 模块结构
- common：公共模块，后续承载工具类、SPI 可插拔接口、协议与通用组件。
- services：业务服务聚合模块
  - gateway：网络网关服务
  - login：登录鉴权服务
  - logic：核心逻辑服务
  - scene：场景/地图服务
  - rank：排行榜服务
  - chat：聊天服务
  - pay：支付服务
- launcher：一键启动管理模块

## 构建
```bash
mvn -q -DskipTests package
```

## 开发规范
- 代码与注释遵循阿里巴巴 Java 开发规范；统一包命名、类命名、注释完整。
- 本仓库后续 PR 会逐步引入：
  - PR-02：代码规范与 CI 门禁（p3c、Checkstyle、Spotless）
  - PR-03：SPI 可插拔组件框架
  - PR-04：日志与观测基础
  - PR-05：协议（Protobuf）与顶层 Envelope
  - PR-06：Netty 网关最小可运行
  - ……
