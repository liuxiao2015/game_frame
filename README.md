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
- ✅ **PR-03**: SPI 可插拔组件框架
- ⏳ **PR-04**: 日志与观测基础设施
- ⏳ **PR-05**: 协议层 (Protobuf) 与消息封装
- ⏳ **PR-06**: Netty 网关最小可运行版本
- ⏳ **PR-07**: 数据层抽象与缓存集成
- ⏳ **PR-08**: 微服务注册发现与配置中心
- ⏳ **PR-09**: 监控指标与健康检查
- ⏳ **PR-10**: 部署脚本与容器化支持

## 🔌 SPI 组件开发指南

game-frame 提供了轻量级的可插拔组件框架，支持通过 Java SPI 机制自动装配组件。开发者可以轻松扩展和集成自定义业务组件。

### 📦 核心概念

**组件生命周期**：每个组件都遵循 `init() -> start() -> stop()` 的标准生命周期
- `init()`: 初始化资源、读取配置、建立依赖关系
- `start()`: 启动服务、开始监听、启动后台线程
- `stop()`: 停止服务、释放资源、清理缓存

**组件顺序**：通过 `getOrder()` 方法定义装配优先级，数值越小优先级越高
- 初始化和启动：按 order 从小到大执行
- 停止：按 order 从大到小执行（逆序）

### 🛠️ 开发新组件

#### 1. 实现 Component 接口

```java
package com.game.business.component;

import com.game.common.component.Component;
import com.game.common.component.ComponentException;

public class MyBusinessComponent implements Component {
    @Override
    public int getOrder() {
        return 200; // 数值越小优先级越高，建议使用100的倍数
    }

    @Override
    public void init() throws ComponentException {
        // 组件初始化逻辑
    }

    @Override
    public void start() throws ComponentException {
        // 组件启动逻辑
    }

    @Override
    public void stop() throws ComponentException {
        // 组件停止逻辑
    }
}
```

#### 2. 创建 SPI 声明文件

在 `src/main/resources/META-INF/services/` 目录下创建文件：
```
com.game.common.component.Component
```

文件内容为组件的完整类名：
```
com.game.business.component.MyBusinessComponent
```

#### 3. 组件优先级规范

推荐的 order 值分配：
- **0-99**: 核心基础组件（日志、配置、监控等）
- **100-199**: 中间件组件（数据库、缓存、消息队列等）
- **200-299**: 网络组件（网关、RPC、协议处理等）
- **300-399**: 业务基础组件（认证、权限、通用服务等）
- **400+**: 具体业务组件

### 🚀 运行演示

#### 编译项目
```bash
mvn -q -DskipTests package
```

#### 运行 SPI 演示
```bash
# 编译 launcher 模块
mvn -q -DskipTests -pl launcher -am package

# 在 IDE 中运行 LauncherSpiDemo.main() 方法
# 或使用命令行：
java -cp "launcher/target/classes:common/target/classes" com.game.launcher.LauncherSpiDemo
```

演示程序会自动：
1. 扫描并加载所有 SPI 组件
2. 按优先级顺序初始化和启动组件
3. 注册 JVM 关闭钩子确保优雅停止
4. 输出详细的生命周期日志

### 📝 注意事项

- **包命名规范**: 组件建议放在 `*.component` 包下，实现类放在 `*.component.impl` 包下
- **异常处理**: 组件方法应抛出 `ComponentException`，提供清晰的错误信息
- **日志记录**: 使用 JDK Logger，避免引入第三方日志框架
- **资源管理**: 确保在 `stop()` 方法中正确释放所有资源
- **线程安全**: 组件应设计为线程安全，支持并发访问

## 📄 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件
