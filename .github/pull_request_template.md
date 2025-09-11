## 📝 变更描述 (Change Description)

<!-- 请简要描述本次 PR 的主要变更内容 -->

### 变更类型 (Change Type)
<!-- 请选择适用的变更类型，删除不适用的选项 -->
- [ ] 🚀 新功能 (feat): 新增功能模块或特性
- [ ] 🐛 问题修复 (fix): 修复 bug 或问题
- [ ] 📝 文档更新 (docs): 仅文档变更
- [ ] 💄 代码格式 (style): 代码格式调整，不影响功能
- [ ] ♻️ 重构 (refactor): 代码重构，不新增功能也不修复问题
- [ ] ⚡ 性能优化 (perf): 性能优化相关变更
- [ ] ✅ 测试 (test): 新增或修改测试用例
- [ ] 🔧 构建/工具 (chore): 构建系统、依赖、工具配置等变更
- [ ] 🔒 安全 (security): 安全相关改进

### 关联 Issue (Related Issues)
<!-- 如有相关 Issue，请填写 Issue 编号 -->
- 关闭 Issue: #(issue 编号)
- 相关 Issue: #(issue 编号)

## 🧪 测试范围 (Testing Scope)

### 测试环境 (Test Environment)
- [ ] 本地开发环境测试通过
- [ ] JDK 版本: Java 17
- [ ] Maven 版本: 3.8+

### 功能测试 (Functional Testing)
<!-- 请描述进行了哪些功能测试 -->
- [ ] 新增功能测试
- [ ] 回归测试 (确保现有功能不受影响)
- [ ] 边界条件测试
- [ ] 异常情况测试

### 代码质量验证 (Code Quality Verification)
- [ ] 编译通过: `mvn -q -DskipTests package`
- [ ] 代码格式检查通过: `mvn spotless:check` 
- [ ] 代码规范检查通过: `mvn checkstyle:check`
- [ ] 完整验证通过: `mvn -DskipTests verify`

## ✅ 自检清单 (Self-Check Checklist)

### 代码质量 (Code Quality)
- [ ] 遵循阿里巴巴 Java 开发规约
- [ ] 所有公共类/方法都有完整的中文注释
- [ ] 没有 TODO 或 FIXME 注释 (或已在 Issue 中跟踪)
- [ ] 删除了无用的代码和注释
- [ ] 导入语句整理完毕，无冗余导入

### 安全性 (Security)
- [ ] 没有硬编码敏感信息 (密码、密钥等)
- [ ] 输入参数进行了合理的校验
- [ ] 异常处理得当，不会泄露敏感信息

### 性能 (Performance)
- [ ] 避免了明显的性能问题
- [ ] 合理使用了缓存和连接池
- [ ] 没有内存泄漏风险

### 兼容性 (Compatibility)
- [ ] 向后兼容，不破坏现有 API
- [ ] 依赖版本变更已充分测试
- [ ] 数据库结构变更 (如有) 已提供迁移方案

## 📸 截图/演示 (Screenshots/Demo)
<!-- 如有 UI 变更或新功能演示，请提供截图或 GIF -->

## 📋 额外说明 (Additional Notes)
<!-- 其他需要说明的内容，如：
- 特殊的部署注意事项
- 配置文件变更说明  
- 已知问题或限制
- 后续计划
-->

---

## 🔍 审查者检查项 (Reviewer Checklist)
<!-- 供代码审查者使用 -->
- [ ] 代码逻辑正确，实现符合需求
- [ ] 代码风格符合项目规范
- [ ] 测试覆盖充分
- [ ] 文档更新完整
- [ ] 没有引入新的技术债务
- [ ] CI 检查全部通过

/assign @reviewer-username
/label ~"代码质量" ~"待审查"