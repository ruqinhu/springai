# 开发与验证快速起步指南

本文档介绍如何在本地开发环境中快速启动本应用程序并验证业务功能的正确性。所有的代码变更在推送前都应通过如下环节。

## 1. 基础环境
- **Java**: JDK 17 及以上。
- **中间件**: MySQL 8+、Redis 6+、Elasticsearch 8+（均可使用 Docker 快速拉起）。

## 2. 三步强制验证法 (The 3-Step Verification)
Agent 及人类开发者在重构任何代码后，应采用如下防腐流水线进行自我检查，而非依赖简单的 `mvn compile`。

### Step A: 编译与架构拦截 (Compile & ArchLint)
```bash
# 执行整个机器合规管道（包含了语法检查和 ArchUnit 拦截）
bash scripts/validate.sh
```

### Step B: 单元与集成测试 (Test)
```bash
# 执行所有业务逻辑测验
mvn test
```

### Step C: 端到端功能测试 (Verify)
对于重大重构，应该验证 API 层结果：
```bash
# 启动项目
mvn spring-boot:run

# 通过 curl 或其它脚本访问 /chat 等核心链路，验证大模型的正常召回和流式处理。
```

## 3. Linter 原则
- 如果项目中的测试或 `lint-arch` (架构规则) 报错，**永远不要试图去修改测试用例以“兼容”非法的代码结构**。
- 将你的业务逻辑妥善降级至 Service 或隔离组件内。
