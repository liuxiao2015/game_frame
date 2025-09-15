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
- ✅ **PR-04**: 日志与观测基础设施
- ✅ **PR-05**: 配置中心与环境分层
- ⏳ **PR-06**: 协议层 (Protobuf) 与消息封装
- ⏳ **PR-07**: Netty 网关最小可运行版本
- ⏳ **PR-08**: 数据层抽象与缓存集成
- ⏳ **PR-09**: 微服务注册发现与扩展
- ⏳ **PR-10**: 监控指标与健康检查
- ⏳ **PR-11**: 部署脚本与容器化支持

## 🔧 配置中心与环境分层

game-frame 提供了轻量级的配置中心能力，支持多环境分层加载与覆盖，满足开发、测试、生产等不同环境的配置需求。

### 🌟 核心特性

- **多环境支持**: 支持 dev/test/prod 三种标准环境，默认为 dev
- **分层覆盖**: 配置按优先级覆盖，确保灵活性和安全性
- **环境变量映射**: 支持通过环境变量覆盖配置，便于容器化部署
- **占位符解析**: 支持 `${key:default}` 语法的配置引用
- **零依赖实现**: 仅使用 JDK 标准能力，无需引入第三方库

### 🎯 环境选择方式

环境选择按以下优先级顺序：

1. **JVM 系统属性**（最高优先级）
   ```bash
   java -Denv=prod YourApp
   ```

2. **环境变量**
   ```bash
   export GAME_ENV=prod
   java YourApp
   ```

3. **默认环境**：dev

### 📁 配置文件约定

配置文件统一放置在 `common/src/main/resources/config/` 目录下：

```
common/src/main/resources/config/
├── application.properties        # 默认配置（所有环境共享）
├── application-dev.properties    # 开发环境配置
├── application-test.properties   # 测试环境配置
└── application-prod.properties   # 生产环境配置
```

### 🔄 配置覆盖顺序

配置值按以下优先级覆盖（数字越大优先级越高）：

```
1. 默认配置文件 (application.properties)
   ↓
2. 环境配置文件 (application-{env}.properties)
   ↓  
3. JVM 系统属性 (-Dkey=value)
   ↓
4. 环境变量 (GAME_*)  ← 最高优先级
```

### 🌍 环境变量映射规则

环境变量到配置键的映射规则：

1. 必须以 `GAME_` 开头
2. 移除前缀后转换为小写
3. 下划线 `_` 转换为点号 `.`

**映射示例**：
```bash
GAME_SERVER_PORT=9000        → server.port=9000
GAME_DATABASE_URL=xxx        → database.url=xxx
GAME_LOGGING_LEVEL_ROOT=WARN → logging.level.root=WARN
```

### 💻 使用示例

#### 基本用法

```java
import com.game.common.config.Config;
import com.game.common.config.PropertyResolver;

// 加载当前环境配置（自动检测环境）
Config config = PropertyResolver.load();

// 加载指定环境配置
Config config = PropertyResolver.load("prod");

// 读取配置值
String appName = config.getString("app.name");
int serverPort = config.getInt("server.port", 8080);
boolean debugEnabled = config.getBoolean("debug.enabled", false);
```

#### 运行演示

```bash
# 编译项目
mvn -q -DskipTests package

# 默认环境运行（dev）
java -cp "launcher/target/classes:common/target/classes" com.game.launcher.LauncherConfigDemo

# 指定生产环境
java -Denv=prod -cp "launcher/target/classes:common/target/classes" com.game.launcher.LauncherConfigDemo

# 环境变量覆盖端口
GAME_SERVER_PORT=9000 java -cp "launcher/target/classes:common/target/classes" com.game.launcher.LauncherConfigDemo
```

### 🚀 后续扩展建议

当前实现保留了良好的扩展空间，未来可以考虑：

- **YAML/TOML 支持**: 扩展支持更现代的配置格式
- **集中式配置**: 集成 Nacos、Consul 等配置中心
- **热加载机制**: 支持配置文件变更时自动重载
- **配置加密**: 敏感配置的加密存储和解密
- **配置验证**: 配置项的类型和取值范围验证
- **多数据源**: 支持数据库、远程 HTTP 等配置源

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
- **日志记录**: 使用 SLF4J 统一日志门面，支持结构化日志和 traceId 追踪
- **资源管理**: 确保在 `stop()` 方法中正确释放所有资源
- **线程安全**: 组件应设计为线程安全，支持并发访问

## 📊 日志与观测基础

game-frame 提供了统一的日志门面和轻量级观测能力，支持结构化日志、分布式追踪和 JVM 指标监控。

### 🔧 依赖说明

框架采用 SLF4J 作为统一日志门面，默认使用 Logback 实现：

- **SLF4J 2.0.13**: 统一日志接口，支持参数化日志和 MDC
- **Logback 1.5.6**: 高性能日志实现，支持配置热加载

### 📝 默认日志格式

框架提供开箱即用的结构化日志配置 (`common/src/main/resources/logback.xml`)：

```
%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{traceId:-}] %logger{36} - %msg%n
```

输出示例：
```
2025-09-12 02:35:48.757 [main] INFO  [b6518f83daa0489b] c.game.launcher.LauncherLoggingDemo - 程序启动
2025-09-12 02:36:18.907 [metrics-reporter] INFO  [] c.g.c.observability.MetricsReporter - jvm.metrics heapUsedMB=9 heapCommittedMB=254 heapMaxMB=4000 threadCount=7
```

### 🏷️ TraceId 和 MDC 使用

使用 `TraceContext` 工具类进行分布式追踪：

```java
// 生成新的 traceId
String traceId = TraceContext.generateTraceId();

// 放入 MDC，当前线程的所有日志都会包含此 traceId
TraceContext.put(traceId);

// 获取当前线程的 traceId
String currentTraceId = TraceContext.get();

// 清理 MDC，防止线程复用污染
TraceContext.clear();
```

### 📈 JVM 指标监控

使用 `MetricsReporter` 进行轻量级 JVM 监控：

```java
// 创建指标上报器（可配置采集周期）
MetricsReporter reporter = new MetricsReporter(Duration.ofSeconds(30));

// 启动定期采集
reporter.start();

// 应用关闭时停止采集
reporter.stop();
```

**可配置项**：
- 采集周期：默认 30 秒，最小 5 秒
- 输出指标：堆内存使用/提交/最大值、线程数量

### 🚀 使用演示

运行日志与观测功能演示：

```bash
# 编译项目
mvn -q -DskipTests -pl launcher -am package

# 在 IDE 中运行 LauncherLoggingDemo.main() 方法
# 或使用命令行：
mvn dependency:copy-dependencies -pl launcher
java -cp "launcher/target/classes:launcher/target/dependency/*" com.game.launcher.LauncherLoggingDemo
```

### 🔧 自定义配置

项目可通过自定义 `logback.xml` 覆盖默认配置：

1. 在具体模块的 `src/main/resources/` 下创建 `logback.xml`
2. 参考 `common` 模块的默认配置进行定制
3. 支持多种 Appender：Console、File、RollingFile、Async 等

## 📦 协议指令对接存储（PR-09）

game-frame 提供了协议指令与存储抽象的完整集成，支持玩家数据的持久化操作。

### 🌟 核心功能

- **指令路由集成**：基于 PR-07 的指令协议框架，支持 echo、time、sum、ping 等基础指令
- **存储抽象集成**：基于 PR-08 的存储框架，支持 H2 内存数据库和 MySQL 数据库
- **玩家数据管理**：新增 player-save、player-get 指令，支持玩家信息的保存和查询
- **依赖注入**：通过构造函数注入 PlayerRepository，实现存储层解耦
- **优雅关闭**：支持数据源和 Netty 服务器的优雅关闭

### 🎯 支持的指令

#### 基础指令
- `echo msg=<message> [seq=<seq>]` - 回显消息
- `time [seq=<seq>]` - 获取服务器时间  
- `sum a=<num1> b=<num2> [seq=<seq>]` - 计算两数之和
- `ping [seq=<seq>]` - 心跳检测

#### 玩家指令
- `player-save name=<name> level=<level> [seq=<seq>]` - 保存玩家信息
- `player-get id=<id> [seq=<seq>]` - 查询玩家信息

### 🚀 运行演示

#### 编译和启动
```bash
# 编译项目
mvn -q -DskipTests -pl launcher -am package

# 在 IDE 中运行 LauncherProtocolWithStorageDemo.main() 方法
# 启动后服务器将监听 7001 端口
```

#### 测试指令

使用 telnet 或 nc 连接服务器进行测试：

```bash
# 连接服务器
telnet localhost 7001

# 测试基础指令
echo msg=hello seq=1
time seq=2
sum a=10 b=20 seq=3
ping seq=4

# 测试玩家指令
player-save name=Alice level=3 seq=5
# 响应: ok id=1 name=Alice level=3 seq=5

player-get id=1 seq=6  
# 响应: ok id=1 name=Alice level=3 seq=6

player-get id=999 seq=7
# 响应: not_found id=999 seq=7
```

### 🔄 MySQL 数据库切换

默认使用 H2 内存数据库，支持切换到 MySQL：

#### 1. 添加 MySQL 驱动依赖

在父项目或相关模块的 `pom.xml` 中添加：

```xml
<dependency>
  <groupId>mysql</groupId>
  <artifactId>mysql-connector-j</artifactId>
  <version>8.2.0</version>
</dependency>
```

#### 2. 修改数据库配置

在 `common/src/main/resources/config/application-prod.properties` 中配置：

```properties
# MySQL 数据库配置
db.driver=com.mysql.cj.jdbc.Driver
db.url=jdbc:mysql://localhost:3306/game?useSSL=false&serverTimezone=UTC
db.username=your_username
db.password=your_password

# 连接池配置（可选）
db.pool.size=10
```

#### 3. 启动 MySQL 环境

```bash
# 使用生产环境配置启动
java -Denv=prod -cp "launcher/target/classes:launcher/target/dependency/*" \
  com.game.launcher.LauncherProtocolWithStorageDemo
```

### 📋 错误处理

系统提供统一的错误处理机制：

- **参数缺失**：`err msg=name parameter is required`
- **类型错误**：`err msg=level must be a valid integer`  
- **数据库异常**：`err msg=internal_server_error`
- **玩家不存在**：`not_found id=<id>`

### 🏗️ 架构设计

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Client        │    │   Network        │    │   Storage       │
│   (telnet/nc)   │───▶│   - Command      │───▶│   - Repository  │
│                 │    │     Dispatcher   │    │   - JDBC        │
│                 │    │   - Handlers     │    │   - DataSource  │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

- **Network 层**：负责协议解析、指令路由、会话管理
- **Storage 层**：负责数据访问、事务管理、连接池管理
- **依赖注入**：通过构造函数注入实现层间解耦

## 📄 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件
