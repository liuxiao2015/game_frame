# game-frame

> 生产可用的 Java 17 游戏服务器框架（多模块 Maven 工程骨架）

[![CI Status](https://github.com/liuxiao2015/game_frame/workflows/CI%20-%20Code%20Quality%20&%20Build/badge.svg)](https://github.com/liuxiao2015/game_frame/actions)
[![Code Style](https://img.shields.io/badge/code%20style-alibaba-brightgreen.svg)](https://github.com/alibaba/p3c)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

## 🏗️ 模块结构

- **common**: 公共模块，承载工具类、SPI 可插拔接口、协议与通用组件
- **services**: 业务服务聚合模块
  - **gateway**: 网络网关服务
  - **login**: 登录鉴权服务
  - **logic**: 核心逻辑服务
  - **scene**: 场景/地图服务
  - **rank**: 排行榜服务
  - **chat**: 聊天服务
  - **pay**: 支付服务
- **launcher**: 一键启动管理模块

## 🚀 快速开始

### 环境要求
- JDK 17+
- Maven 3.8+

### 构建项目
```bash
# 编译项目
mvn -q -DskipTests package

# 运行代码质量检查
mvn -DskipTests verify
```

## 💻 开发规范

### 代码质量标准
- 遵循 **阿里巴巴 Java 开发规约** ([P3C](https://github.com/alibaba/p3c))
- 使用 **Checkstyle** 进行代码规范检查
- 使用 **Spotless** 进行代码格式化 (基于 google-java-format)
- 统一包命名、类命名、注释完整

### 本地开发命令
```bash
# 代码格式化
mvn spotless:apply

# 代码规范检查
mvn checkstyle:check

# 完整验证 (提交前必须执行)
mvn -DskipTests verify
```

### 贡献指南
请阅读 [CONTRIBUTING.md](CONTRIBUTING.md) 了解详细的开发规范和流程。

## 🔄 CI/CD 流程

每次 Push 和 Pull Request 都会自动执行：
- ✅ 代码编译验证
- ✅ Spotless 格式检查
- ✅ Checkstyle 代码规范检查
- ✅ 单元测试执行 (如有)

## 📋 开发路线图

本仓库将逐步通过以下 PR 完善：
- ✅ **PR-02**: 代码规范与 CI 门禁 (Checkstyle、Spotless)
- ⏳ **PR-03**: SPI 可插拔组件框架
- ⏳ **PR-04**: 日志与观测基础设施
- ⏳ **PR-05**: 协议层 (Protobuf) 与消息封装
- ⏳ **PR-06**: Netty 网关最小可运行版本
- ⏳ **PR-07**: 数据层抽象与缓存集成
- ⏳ **PR-08**: 微服务注册发现与配置中心
- ⏳ **PR-09**: 监控指标与健康检查
- ⏳ **PR-10**: 部署脚本与容器化支持

## 📄 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件
