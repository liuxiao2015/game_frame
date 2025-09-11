# 贡献指南 (Contributing Guide)

欢迎为 game-frame 项目贡献代码！本文档将帮助您了解开发规范、工具使用和最佳实践。

## 📋 开发环境要求

- **JDK**: 17 或更高版本
- **Maven**: 3.8+ 推荐
- **IDE**: IntelliJ IDEA 2023+ 或 Eclipse 2023+ (推荐 IntelliJ IDEA)
- **操作系统**: Windows、macOS、Linux 均可

## 🛠️ 本地开发流程

### 1. 克隆代码库
```bash
git clone https://github.com/liuxiao2015/game_frame.git
cd game_frame
```

### 2. 验证环境
```bash
# 检查 Java 版本
java -version

# 检查 Maven 版本  
mvn -version

# 验证项目编译
mvn -q -DskipTests package
```

### 3. 代码格式化 (必须执行)
```bash
# 自动格式化所有 Java 代码
mvn spotless:apply

# 验证格式是否符合规范
mvn spotless:check
```

### 4. 代码规范检查
```bash
# 执行 Checkstyle 检查
mvn checkstyle:check

# 生成详细的 Checkstyle 报告
mvn checkstyle:checkstyle
# 报告位置: target/site/checkstyle.html
```

### 5. 完整验证 (提交前必须通过)
```bash
# 一键执行编译 + 格式检查 + 代码规范检查
mvn -DskipTests verify

# 如果有测试，执行完整验证
mvn clean verify
```

## 📝 代码规范

### 基础规范
本项目严格遵循 **阿里巴巴 Java 开发规约**，主要包括：

#### 命名规范
- **包名**: 全小写，点分隔，如 `com.game.services.gateway`
- **类名**: 大驼峰命名，如 `PlayerService`、`GameConfig`
- **方法名**: 小驼峰命名，如 `getUserInfo()`、`processLogin()`
- **变量名**: 小驼峰命名，如 `playerId`、`maxConnections`
- **常量名**: 全大写下划线分隔，如 `MAX_RETRY_COUNT`、`DEFAULT_TIMEOUT`

#### 注释规范 ⭐
**特别要求：所有公共类、方法、字段必须提供完整的中文注释**

```java
/**
 * 玩家登录服务类
 * 负责处理玩家登录、注销和会话管理功能
 * 
 * @author 开发者姓名
 * @since 1.0.0
 */
public class PlayerLoginService {
    
    /** 默认会话超时时间（毫秒） */
    private static final long DEFAULT_SESSION_TIMEOUT = 30 * 60 * 1000L;
    
    /**
     * 处理玩家登录请求
     * 
     * @param username 用户名，不能为空
     * @param password 密码，不能为空  
     * @return 登录结果，包含会话信息和用户数据
     * @throws IllegalArgumentException 当用户名或密码为空时抛出
     * @throws AuthenticationException 当认证失败时抛出
     */
    public LoginResult processLogin(String username, String password) {
        // 实现代码...
    }
}
```

#### 代码格式
- **缩进**: 2 个空格（不使用 Tab）
- **行长度**: 最大 120 字符
- **换行符**: 统一使用 LF (`\n`)
- **文件编码**: UTF-8
- **文件末尾**: 必须有空行

### 特殊规则说明

#### 占位类处理
对于当前的占位类（如 `PlaceholderGateway`），允许以下例外：
- 私有构造器可以不抛出异常
- 可以使用工具类的设计模式

```java
/**
 * 占位类：网关服务（Gateway）模块
 * 本模块将用于承载基于 Netty 的网络层实现（后续 PR 引入），
 * 当前占位类仅用于保证工程编译通过与包结构清晰。
 */
public final class PlaceholderGateway {
    
    private PlaceholderGateway() {
        // 工具类/占位类不允许实例化，符合阿里巴巴开发规范
    }
}
```

## 🔍 问题排查

### 常见 Spotless 格式化问题
```bash
# 问题：格式不符合规范
# 解决：执行自动格式化
mvn spotless:apply
```

### 常见 Checkstyle 问题

#### 1. 缺少类注释
```
错误: Missing a Javadoc comment.
解决: 为类添加完整的中文 Javadoc 注释
```

#### 2. 方法过长
```
错误: Method length is XXX lines (max allowed is 150).
解决: 将长方法拆分为多个较小的方法
```

#### 3. 魔法数字
```
错误: '100' is a magic number.
解决: 将数字定义为有意义的常量
```

#### 4. 导入顺序
```
错误: Wrong order for 'java.util.List' import.
解决: 执行 mvn spotless:apply 自动修复
```

### Checkstyle 报告查看
```bash
# 生成详细报告
mvn checkstyle:checkstyle

# 查看报告
# 文件位置: target/site/checkstyle.html
# 或命令行查看: target/checkstyle-result.xml
```

## 🚀 提交流程

### 1. 开发前检查
- [ ] 拉取最新代码: `git pull origin main`
- [ ] 创建功能分支: `git checkout -b feature/your-feature-name`

### 2. 开发中验证
- [ ] 定期格式化: `mvn spotless:apply`
- [ ] 定期检查: `mvn -DskipTests verify`

### 3. 提交前验证 (必须全部通过)
- [ ] 代码格式化: `mvn spotless:apply`
- [ ] 完整验证: `mvn clean verify`
- [ ] 检查 Git 状态: `git status`
- [ ] 提交变更: `git add . && git commit -m "feat: 功能描述"`

### 4. 推送与 PR
- [ ] 推送分支: `git push origin feature/your-feature-name`
- [ ] 创建 Pull Request
- [ ] 等待 CI 检查通过
- [ ] 代码审查与合并

## 📚 IDE 配置推荐

### IntelliJ IDEA
1. 安装 `Checkstyle-IDEA` 插件
2. 导入项目的 `checkstyle.xml` 配置
3. 启用 `EditorConfig` 支持 (通常默认开启)
4. 配置自动导入优化：`Settings → Editor → General → Auto Import`

### Eclipse
1. 安装 `Checkstyle Plugin for Eclipse`
2. 导入 `checkstyle.xml` 配置
3. 安装 `EditorConfig Eclipse Plugin`

## ❓ 获取帮助

- **技术问题**: 提交 GitHub Issue
- **代码规范疑问**: 参考 [阿里巴巴 Java 开发手册](https://github.com/alibaba/p3c)
- **Maven 插件**: [Spotless 文档](https://github.com/diffplug/spotless/tree/main/plugin-maven)

---

感谢您的贡献！遵循这些规范将确保代码库的高质量与一致性。