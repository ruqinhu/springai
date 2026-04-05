# Spring AI Shopping Guide - Agent Guide

这是一份面向所有 AI Agent 的导航地图。不要在这个文件中添加长篇大论，请只做**指南针**使用。详情请参考 `docs/` 目录。

## 快速链接
- [系统架构总览](docs/ARCHITECTURE.md) — 了解双向量库架构与领域分层防腐规则。
- [开发启动指南](docs/DEVELOPMENT.md) — 构建、测试、基础运行环境的预置信息。

## 构建与机器验证命令
我们将架构约定交由自动化管线执行。当需要重构或添加新特性时，务必**动手前先验证**：

```shell
# ✅ 全量验证门禁 (核心操作)
bash scripts/validate.sh

# 如果只单独运行某一项：
mvn compile      # 构建项目检查语法
mvn test -Dtest=ArchitectureConstraintsTest  # 运行架构合规 lint
mvn test         # 运行全量功能测试
```

## 核心分层规则 (ArchUnit-Enforced)
详细原理见架构总览，此处仅列出不可违背的底层方向（**严禁底层依赖上层**）：
- **Layer 0 (`domain/`)**: 纯领域定义与基础类型，无内部依赖项。
- **Layer 1 (`config/`, `repository/`)**: 配置与基础设施层，依赖 Layer 0。
- **Layer 2 (`agent/`, `service/`)**: 核心业务与推断逻辑，依赖 Layer 0-1。
- **Layer 3 (`controller/`)**: 接口层，依赖 Layer 0-2。彼此不得反向或循环引用。

## Prompt 质量与规范标准
- 系统内的 Agent prompt (`src/main/resources/prompts/`) 必须使用 **XML Tags** `(<role>, <rules>, <thinking>)`。
- 不要使用过于机器人的腔调（例如“好的，我知道了”），风格向人类高级专家靠拢。
- 业务逻辑更新要优先采用“思维链推演”（Chain of Thought）而非单一判断。

## Agent 工作流规范 (Harness Workflow)
为了保证“马有缰绳”，作为参与本项目开发的代码生成 Agent（Qoder 等），每次接收中等复杂度以上的任务时，**必须强制遵守以下记录闭环**：
1. **任务登记 (Task Checkpoint)**：在写代码前，先在 `harness/tasks/` 下创建该任务的小型计划簿（可以在执行计划的 artifact 外同步备份摘要），明确你要改哪些层。
2. **故障追溯 (Execution Trace)**：如果你在修改代码后跑 `scripts/validate.sh` 遇到了失败报错，不要仅仅在对话里隐式修复。你必须把失败的特征（尤其是架构越权报错）记录进 `harness/trace/failures.md`，用于防止你在长对话中“遗忘”。
3. **经验沉淀 (Memory Storage)**：如果排查出了对项目很有价值的规避措施，在任务完成前，将其凝练写入 `harness/memory/lessons.md`，留作程序记忆。下一个 Agent 启动时会加载此文件。